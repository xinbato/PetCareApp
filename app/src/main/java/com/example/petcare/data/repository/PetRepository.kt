package com.example.petcare.data.repository

import android.util.Log
import com.example.petcare.R
import com.example.petcare.data.model.CheckupRecordModel
import com.example.petcare.data.model.MedicalRecordEntryModel
import com.example.petcare.data.model.MedicationModel
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.PetPlaceModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.model.WeightRecordModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class UserProfileData(
    val name: String = "",
    val memberSince: String = "Thành viên từ 2026",
    val rank: String = "Hội viên mới",
    val avatarUri: String? = null,
    val avatarRes: Int? = null,
    val headerBgColorHex: Long = 0xFF338C63,
    val headerBgUri: String? = null,
    val email: String = "",
    val phone: String? = null,
    val dob: String? = null,
    val address: String? = null,
)

data class AccountUser(
    val id: String,
    val name: String,
    val isCurrent: Boolean = false,
)

object PetRepository {
    private const val TAG = "PetRepository"
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private var petsListener: ListenerRegistration? = null
    private var vaccinationsListener: ListenerRegistration? = null
    private var medicationsListener: ListenerRegistration? = null
    private var medicalRecordsListener: ListenerRegistration? = null
    private var weightRecordsListener: ListenerRegistration? = null
    private var checkupRecordsListener: ListenerRegistration? = null
    private var schedulesListener: ListenerRegistration? = null

    private val _userProfile = MutableStateFlow(UserProfileData())
    val userProfile: StateFlow<UserProfileData> = _userProfile.asStateFlow()

    private val _accounts = MutableStateFlow(emptyList<AccountUser>())
    val accounts: StateFlow<List<AccountUser>> = _accounts.asStateFlow()

    private val _pets = MutableStateFlow(emptyList<PetModel>())
    val pets: StateFlow<List<PetModel>> = _pets.asStateFlow()

    private val _vaccinations = MutableStateFlow(emptyList<VaccinationModel>())
    val vaccinations: StateFlow<List<VaccinationModel>> = _vaccinations.asStateFlow()

    private val _weightRecords = MutableStateFlow(emptyList<WeightRecordModel>())
    val weightRecords: StateFlow<List<WeightRecordModel>> = _weightRecords.asStateFlow()

    private val _checkupRecords = MutableStateFlow(emptyList<CheckupRecordModel>())
    val checkupRecords: StateFlow<List<CheckupRecordModel>> = _checkupRecords.asStateFlow()

    private val _medications = MutableStateFlow(emptyList<MedicationModel>())
    val medications: StateFlow<List<MedicationModel>> = _medications.asStateFlow()

    private val _medicalRecords = MutableStateFlow(emptyList<MedicalRecordEntryModel>())
    val medicalRecords: StateFlow<List<MedicalRecordEntryModel>> = _medicalRecords.asStateFlow()

    private val _schedules = MutableStateFlow(emptyList<ScheduleModel>())
    val schedules: StateFlow<List<ScheduleModel>> = _schedules.asStateFlow()

    private val _petPlaces = MutableStateFlow(initialPetPlaces())
    val petPlaces: StateFlow<List<PetPlaceModel>> = _petPlaces.asStateFlow()

    private val _currentSelectedPetId = MutableStateFlow("")
    val currentSelectedPetId: StateFlow<String> = _currentSelectedPetId.asStateFlow()

    init {
        runCatching {
            auth.currentUser?.let { user ->
                updateUserProfile(name = user.displayName ?: user.email, email = user.email)
                attachUser(user.uid)
            }
        }
    }

    fun attachUser(userId: String?) {
        petsListener?.remove()
        vaccinationsListener?.remove()
        medicationsListener?.remove()
        medicalRecordsListener?.remove()
        weightRecordsListener?.remove()
        checkupRecordsListener?.remove()
        schedulesListener?.remove()
        if (userId.isNullOrEmpty()) return

        _pets.value = emptyList()
        _vaccinations.value = emptyList()
        _medications.value = emptyList()
        _medicalRecords.value = emptyList()
        _weightRecords.value = emptyList()
        _checkupRecords.value = emptyList()
        _schedules.value = emptyList()
        _currentSelectedPetId.value = ""

        val userDoc = firestore.collection("users").document(userId)

        userDoc.get().addOnSuccessListener { doc ->
            val resolvedName = if (doc.exists()) {
                val name = doc.getString("name")
                    ?: auth.currentUser?.displayName?.ifBlank { null }
                    ?: auth.currentUser?.email?.substringBefore("@")
                    ?: "Bạn"
                val email = doc.getString("email") ?: auth.currentUser?.email.orEmpty()
                val phone = doc.getString("phone") ?: auth.currentUser?.phoneNumber
                val address = doc.getString("address")
                val dob = doc.getString("dob")
                val avatarUri = doc.getString("avatarUri") ?: auth.currentUser?.photoUrl?.toString()
                _userProfile.update { current ->
                    current.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        address = address,
                        dob = dob,
                        avatarUri = avatarUri ?: current.avatarUri
                    )
                }
                name
            } else {
                val user = auth.currentUser
                val name = user?.displayName?.ifBlank { null }
                    ?: user?.email?.substringBefore("@")
                    ?: "Bạn"
                val email = user?.email.orEmpty()
                val phone = user?.phoneNumber
                val avatarUri = user?.photoUrl?.toString()
                _userProfile.update { current ->
                    current.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        address = null,
                        dob = null,
                        avatarUri = avatarUri
                    )
                }
                name
            }
            _accounts.value = listOf(AccountUser(id = userId, name = resolvedName, isCurrent = true))
        }

        petsListener = userDoc.collection("pets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lắng nghe pets từ Firestore: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val remotePets = snapshot.documents.mapNotNull { doc ->
                    doc.toPetModelSafe()
                }
                _pets.value = remotePets
                if (remotePets.isNotEmpty()) {
                    if (_pets.value.none { it.id == _currentSelectedPetId.value }) {
                        _currentSelectedPetId.value = remotePets.first().id
                    }
                } else {
                    _currentSelectedPetId.value = ""
                }
            }

        vaccinationsListener = userDoc.collection("vaccinations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lắng nghe vaccinations từ Firestore: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { it.toVaccinationModelSafe() }
                _vaccinations.value = items
            }

        medicationsListener = userDoc.collection("medications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lắng nghe medications từ Firestore: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { it.toMedicationModelSafe() }
                _medications.value = items
            }

        medicalRecordsListener = userDoc.collection("medicalRecords")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lắng nghe medicalRecords từ Firestore: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { it.toMedicalRecordSafe() }
                _medicalRecords.value = items
            }

        weightRecordsListener = userDoc.collection("weightRecords")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lắng nghe weightRecords từ Firestore: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { it.toWeightRecordSafe() }
                _weightRecords.value = items
            }

        checkupRecordsListener = userDoc.collection("checkupRecords")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lắng nghe checkupRecords từ Firestore: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { it.toCheckupRecordSafe() }
                _checkupRecords.value = items
            }

        schedulesListener = userDoc.collection("schedules")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lắng nghe schedules từ Firestore: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val items = snapshot.documents.mapNotNull { it.toScheduleModelSafe() }
                _schedules.value = items
            }
    }

    fun detachUser() {
        petsListener?.remove()
        vaccinationsListener?.remove()
        medicationsListener?.remove()
        medicalRecordsListener?.remove()
        weightRecordsListener?.remove()
        checkupRecordsListener?.remove()
        schedulesListener?.remove()
        petsListener = null
        vaccinationsListener = null
        medicationsListener = null
        medicalRecordsListener = null
        weightRecordsListener = null
        checkupRecordsListener = null
        schedulesListener = null
        resetToDefaults()
    }

    fun switchAccount(accountId: String) {
        _accounts.update { list ->
            list.map { it.copy(isCurrent = it.id == accountId) }
        }
        _accounts.value.find { it.id == accountId }?.let { currentAcc ->
            _userProfile.update { it.copy(name = currentAcc.name) }
        }
    }

    fun addAccount(name: String) {
        val newId = "a_${System.currentTimeMillis()}"
        _accounts.update { it + AccountUser(newId, name, isCurrent = false) }
        switchAccount(newId)
    }

    fun updateUserProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        dob: String? = null,
        address: String? = null,
        avatarUri: String? = null,
        avatarRes: Int? = null,
        headerBgColorHex: Long? = null,
        headerBgUri: String? = null,
    ) {
        _userProfile.update { current ->
            var updated = current
            if (name != null) updated = updated.copy(name = name)
            if (email != null) updated = updated.copy(email = email)
            if (phone != null) updated = updated.copy(phone = phone)
            if (dob != null) updated = updated.copy(dob = dob)
            if (address != null) updated = updated.copy(address = address)
            if (avatarUri != null) updated = updated.copy(avatarUri = avatarUri, avatarRes = null)
            if (avatarRes != null) updated = updated.copy(avatarRes = avatarRes, avatarUri = null)
            if (headerBgColorHex != null) updated = updated.copy(headerBgColorHex = headerBgColorHex, headerBgUri = null)
            if (headerBgUri != null) updated = updated.copy(headerBgUri = headerBgUri)
            updated
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val updates = mutableMapOf<String, Any>()
            name?.takeIf { it.isNotBlank() }?.let { updates["name"] = it }
            email?.takeIf { it.isNotBlank() }?.let { updates["email"] = it }
            phone?.takeIf { it.isNotBlank() }?.let { updates["phone"] = it }
            dob?.takeIf { it.isNotBlank() }?.let { updates["dob"] = it }
            address?.takeIf { it.isNotBlank() }?.let { updates["address"] = it }
            avatarUri?.let { updates["avatarUri"] = it }
            if (updates.isNotEmpty()) {
                firestore.collection("users").document(uid).update(updates)
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Lỗi cập nhật userProfile lên Firestore: ${e.message}", e)
                    }
            }
        }
    }

    fun toggleSavePlace(placeId: String) {
        _petPlaces.update { places ->
            places.map { if (it.id == placeId) it.copy(isSaved = !it.isSaved) else it }
        }
    }

    fun selectPet(petId: String) {
        _currentSelectedPetId.value = petId
    }

    fun setCurrentSelectedPetId(petId: String) = selectPet(petId)

    fun getSelectedPet(): PetModel {
        val currentPets = _pets.value
        return currentPets.find { it.id == _currentSelectedPetId.value }
            ?: currentPets.firstOrNull()
            ?: PetModel()
    }

    fun getPetById(id: String): PetModel? = _pets.value.find { it.id == id }

    fun savePet(pet: PetModel) {
        _pets.update { list ->
            val index = list.indexOfFirst { it.id == pet.id }
            if (index != -1) {
                list.toMutableList().apply { set(index, pet) }
            } else {
                list + pet
            }
        }
        if (_currentSelectedPetId.value.isBlank()) {
            _currentSelectedPetId.value = pet.id
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users")
                .document(uid)
                .collection("pets")
                .document(pet.id)
                .set(pet)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi lưu pet (${pet.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun deletePet(id: String) {
        _pets.update { list -> list.filterNot { it.id == id } }
        if (_currentSelectedPetId.value == id) {
            _currentSelectedPetId.value = _pets.value.firstOrNull()?.id.orEmpty()
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users")
                .document(uid)
                .collection("pets")
                .document(id)
                .delete()
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi xóa pet ($id) trên Firestore: ${e.message}", e)
                }
        }
    }

    fun toggleFavorite(id: String) {
        _pets.update { list ->
            list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    fun getVaccinationsForPet(petId: String): List<VaccinationModel> =
        _vaccinations.value.filter { it.petId == petId }

    fun addVaccination(vaccination: VaccinationModel) {
        _vaccinations.update { listOf(vaccination) + it }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("vaccinations").document(vaccination.id).set(vaccination)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi thêm vaccination (${vaccination.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun getMedicationsForPet(petId: String): List<MedicationModel> =
        _medications.value.filter { it.petId == petId }

    fun addMedication(medication: MedicationModel) {
        _medications.update { listOf(medication) + it }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("medications").document(medication.id).set(medication)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi thêm medication (${medication.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun getMedicalRecordsForPet(petId: String): List<MedicalRecordEntryModel> =
        _medicalRecords.value.filter { it.petId == petId }

    fun addMedicalRecord(record: MedicalRecordEntryModel) {
        _medicalRecords.update { listOf(record) + it }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("medicalRecords").document(record.id).set(record)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi thêm medicalRecord (${record.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun getScheduleById(id: String): ScheduleModel? =
        _schedules.value.find { it.id == id }

    fun addSchedule(schedule: ScheduleModel) {
        _schedules.update { it + schedule }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("schedules").document(schedule.id).set(schedule)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi thêm schedule (${schedule.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun updateSchedule(schedule: ScheduleModel) {
        _schedules.update { list ->
            val index = list.indexOfFirst { it.id == schedule.id }
            if (index != -1) {
                list.toMutableList().apply { set(index, schedule) }
            } else {
                list + schedule
            }
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("schedules").document(schedule.id).set(schedule)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi cập nhật schedule (${schedule.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun deleteSchedule(id: String) {
        _schedules.update { list -> list.filterNot { it.id == id } }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("schedules").document(id).delete()
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi xóa schedule ($id) trên Firestore: ${e.message}", e)
                }
        }
    }

    fun getWeightRecordsForPet(petId: String): List<WeightRecordModel> =
        _weightRecords.value.filter { it.petId == petId }

    fun addWeightRecord(record: WeightRecordModel) {
        _weightRecords.update { listOf(record) + it }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("weightRecords").document(record.id).set(record)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi thêm weightRecord (${record.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun getCheckupRecordsForPet(petId: String): List<CheckupRecordModel> =
        _checkupRecords.value.filter { it.petId == petId }

    fun addCheckupRecord(record: CheckupRecordModel) {
        _checkupRecords.update { listOf(record) + it }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("checkupRecords").document(record.id).set(record)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Lỗi thêm checkupRecord (${record.id}) lên Firestore: ${e.message}", e)
                }
        }
    }

    fun resetToDefaults() {
        _userProfile.value = UserProfileData(name = "", email = "", phone = null, address = null, dob = null)
        _accounts.value = emptyList()
        _pets.value = emptyList()
        _vaccinations.value = emptyList()
        _weightRecords.value = emptyList()
        _checkupRecords.value = emptyList()
        _medications.value = emptyList()
        _medicalRecords.value = emptyList()
        _schedules.value = emptyList()
        _petPlaces.value = initialPetPlaces()
        _currentSelectedPetId.value = ""
    }

    private fun initialPetPlaces() = listOf(
        PetPlaceModel(
            id = "p1",
            name = "PawCare Veterinary",
            category = "Thú y",
            subtitle = "Khám tổng quát • Tiêm phòng • Xét nghiệm",
            rating = "4.9",
            reviewCount = 328,
            distance = "1,2 km",
            address = "123 Đường Nam Kỳ Khởi Nghĩa, Quận 3, TP.HCM",
            phone = "0901234567",
            isFeatured = true,
            isSaved = true,
            imageRes = R.drawable.dog,
            lat = 10.7769,
            lng = 106.7009,
        ),
        PetPlaceModel(
            id = "p2",
            name = "An Tâm Pet Clinic",
            category = "Thú y",
            subtitle = "Khám • Siêu âm • Thuốc",
            rating = "4.7",
            reviewCount = 215,
            distance = "2,1 km",
            address = "456 Đường Nguyễn Thị Minh Khai, Quận 3, TP.HCM",
            phone = "0909876543",
            isSaved = false,
            imageRes = R.drawable.cat,
            lat = 10.7800,
            lng = 106.6950,
        ),
        PetPlaceModel(
            id = "p3",
            name = "Pet WOW Spa - Bình Thạnh",
            category = "Spa",
            subtitle = "Tắm sấy • Cắt tỉa lông • Vệ sinh tai móng",
            rating = "4.9",
            reviewCount = 446,
            distance = "3,5 km",
            address = "789 Đường Điện Biên Phủ, Bình Thạnh, TP.HCM",
            phone = "0934567890",
            isSaved = false,
            imageRes = R.drawable.dog,
            lat = 10.8000,
            lng = 106.7100,
        ),
        PetPlaceModel(
            id = "p4",
            name = "Shop Thú Cưng Sài Gòn",
            category = "Cửa hàng",
            subtitle = "Thức ăn • Phụ kiện • Đồ chơi thú cưng",
            rating = "4.3",
            reviewCount = 446,
            distance = "1,8 km",
            address = "321 Đường Lê Văn Sỹ, Quận 3, TP.HCM",
            phone = "0912345678",
            isSaved = false,
            imageRes = R.drawable.cat,
            lat = 10.7900,
            lng = 106.6800,
        ),
        PetPlaceModel(
            id = "p5",
            name = "Phòng Khám Thú Y Gold Care",
            category = "Thú y",
            subtitle = "Cấp cứu 24/7 • Phẫu thuật • Spa",
            rating = "4.9",
            reviewCount = 42,
            distance = "2,8 km",
            address = "555 Đường CMT8, Quận 10, TP.HCM",
            phone = "0988776655",
            isSaved = false,
            imageRes = R.drawable.dog,
            lat = 10.7700,
            lng = 106.6750,
        ),
    )
}

private fun DocumentSnapshot.toPetModelSafe(): PetModel? = runCatching {
    val pet = toObject(PetModel::class.java)?.copy(id = id)
    if (pet != null) {
        val safeIcon = if (pet.iconRes != 0) pet.iconRes else R.drawable.dog
        pet.copy(iconRes = safeIcon)
    } else null
}.getOrElse {
    runCatching {
        PetModel(
            id = id,
            name = getString("name") ?: "Thú cưng",
            species = getString("species") ?: "Chó",
            breed = getString("breed") ?: "",
            dateOfBirth = getString("dateOfBirth") ?: "",
            age = getString("age") ?: "",
            weight = getString("weight") ?: "",
            gender = getString("gender") ?: "Đực",
            iconRes = getLong("iconRes")?.toInt()?.takeIf { it != 0 } ?: R.drawable.dog,
            statusText = getString("statusText") ?: "Bình thường",
            statusDetail = getString("statusDetail") ?: "",
            isFavorite = getBoolean("isFavorite") ?: false,
            temperature = getString("temperature") ?: "",
            activity = getString("activity") ?: "",
            diet = getString("diet") ?: "",
            lastUpdated = getString("lastUpdated") ?: "",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toVaccinationModelSafe(): VaccinationModel? = runCatching {
    toObject(VaccinationModel::class.java)?.copy(id = id)
}.getOrElse {
    runCatching {
        VaccinationModel(
            id = id,
            petId = getString("petId") ?: "",
            title = getString("title") ?: "",
            dateTime = getString("dateTime") ?: "",
            status = getString("status") ?: "Đã tiêm",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toMedicationModelSafe(): MedicationModel? = runCatching {
    toObject(MedicationModel::class.java)?.copy(id = id)
}.getOrElse {
    runCatching {
        MedicationModel(
            id = id,
            petId = getString("petId") ?: "",
            name = getString("name") ?: "",
            dosage = getString("dosage") ?: "",
            scheduleTime = getString("scheduleTime") ?: "",
            quantity = getString("quantity") ?: "",
            status = getString("status") ?: "Đang dùng",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toMedicalRecordSafe(): MedicalRecordEntryModel? = runCatching {
    toObject(MedicalRecordEntryModel::class.java)?.copy(id = id)
}.getOrElse {
    runCatching {
        MedicalRecordEntryModel(
            id = id,
            petId = getString("petId") ?: "",
            petName = getString("petName") ?: "",
            dateTime = getString("dateTime") ?: "",
            activityType = getString("activityType") ?: "",
            description = getString("description") ?: "",
            photoUri = getString("photoUri"),
            emotion = getString("emotion") ?: "Bình thường",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toWeightRecordSafe(): WeightRecordModel? = runCatching {
    toObject(WeightRecordModel::class.java)?.copy(id = id)
}.getOrElse {
    runCatching {
        WeightRecordModel(
            id = id,
            petId = getString("petId") ?: "",
            petName = getString("petName") ?: "",
            weight = getString("weight") ?: "",
            dateTime = getString("dateTime") ?: "",
            status = getString("status") ?: "Ổn định",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toCheckupRecordSafe(): CheckupRecordModel? = runCatching {
    toObject(CheckupRecordModel::class.java)?.copy(id = id)
}.getOrElse {
    runCatching {
        CheckupRecordModel(
            id = id,
            petId = getString("petId") ?: "",
            petName = getString("petName") ?: "",
            title = getString("title") ?: "",
            doctorOrClinic = getString("doctorOrClinic") ?: "",
            dateTime = getString("dateTime") ?: "",
            result = getString("result") ?: "Bình thường",
            status = getString("status") ?: "Hoàn thành",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toScheduleModelSafe(): ScheduleModel? = runCatching {
    toObject(ScheduleModel::class.java)?.copy(id = id)
}.getOrElse {
    runCatching {
        ScheduleModel(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            petId = getString("petId") ?: "",
            petName = getString("petName") ?: "",
            dateText = getString("dateText") ?: "",
            timeText = getString("timeText") ?: "",
            timeOrDateLabel = getString("timeOrDateLabel") ?: "",
            isCompleted = getBoolean("isCompleted") ?: false,
        )
    }.getOrNull()
}
