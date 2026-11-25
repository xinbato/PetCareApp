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
import com.example.petcare.data.model.resolvePetIconRes
import com.example.petcare.firebase.OperationType
import com.example.petcare.firebase.handleFirestoreError
import com.example.petcare.utils.DateUtils
import com.example.petcare.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.Locale

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
    val isAnonymous: Boolean = false,
)

data class AccountUser(
    val id: String,
    val name: String,
    val isCurrent: Boolean = false,
)

object PetRepository {
    private const val TAG = "PetRepository"
    private const val MAX_BATCH_OPS = 450
    private val PET_CHILD_COLLECTIONS = listOf(
        "vaccinations",
        "medications",
        "medicalRecords",
        "weightRecords",
        "checkupRecords",
        "schedules",
    )

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private var attachedUserId: String? = null

    private var petsListener: ListenerRegistration? = null
    private var vaccinationsListener: ListenerRegistration? = null
    private var medicationsListener: ListenerRegistration? = null
    private var medicalRecordsListener: ListenerRegistration? = null
    private var weightRecordsListener: ListenerRegistration? = null
    private var checkupRecordsListener: ListenerRegistration? = null
    private var schedulesListener: ListenerRegistration? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

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
                updateUserProfile(
                    name = user.displayName?.ifBlank { null }
                        ?: if (user.isAnonymous) "Khách" else user.email?.substringBefore("@") ?: "Bạn",
                    email = user.email.orEmpty(),
                    isAnonymous = user.isAnonymous,
                )
                attachUser(user.uid)
            }
        }
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun postMessage(message: String) {
        _userMessage.value = message
    }

    fun attachUser(userId: String?) {
        if (userId.isNullOrEmpty()) return
        if (attachedUserId == userId && petsListener != null) {
            refreshUserHeaderFromAuth()
            return
        }
        removeListeners()
        attachedUserId = userId

        _isLoading.value = true
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
            val currentUser = runCatching { auth.currentUser }.getOrNull()
            val isAnon = currentUser?.isAnonymous ?: false
            if (doc.exists()) {
                val name = doc.getString("name")?.takeIf { it.isNotBlank() }
                    ?: currentUser?.displayName?.ifBlank { null }
                    ?: currentUser?.email?.substringBefore("@")
                    ?: if (isAnon) "Khách" else "Bạn"
                val email = doc.getString("email") ?: currentUser?.email.orEmpty()
                val phone = doc.getString("phone") ?: currentUser?.phoneNumber
                val address = doc.getString("address")
                val dob = doc.getString("dob")
                val rawAvatarUri = doc.getString("avatarUri")?.trim()
                val resolvedAvatarRes = when (rawAvatarUri) {
                    "preset:dog" -> R.drawable.dog
                    "preset:cat" -> R.drawable.cat
                    "preset:rabbit" -> R.drawable.rabbit
                    else -> null
                }
                val resolvedAvatarUri = when {
                    resolvedAvatarRes != null -> null
                    rawAvatarUri == null -> currentUser?.photoUrl?.toString()
                    rawAvatarUri.isEmpty() -> null
                    else -> rawAvatarUri
                }
                val headerBgColorHex = doc.getLong("headerBgColorHex") ?: 0xFF338C63
                val resolvedHeaderBgUri = doc.getString("headerBgUri")?.trim()?.takeIf { it.isNotEmpty() }
                _userProfile.update { current ->
                    current.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        address = address,
                        dob = dob,
                        avatarUri = resolvedAvatarUri,
                        avatarRes = resolvedAvatarRes,
                        headerBgColorHex = headerBgColorHex,
                        headerBgUri = resolvedHeaderBgUri,
                        isAnonymous = isAnon,
                        rank = if (isAnon) "Tài khoản Khách" else "Hội viên",
                    )
                }
                _accounts.value = listOf(AccountUser(id = userId, name = name, isCurrent = true))
            } else {
                refreshUserHeaderFromAuth()
            }
        }.addOnFailureListener { e ->
            handleFirestoreError(e, OperationType.GET, "users/$userId")
            refreshUserHeaderFromAuth()
        }

        petsListener = userDoc.collection("pets")
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/pets")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val remotePets = snapshot.documents.mapNotNull { it.toPetModelSafe() }
                    .sortedBy { it.name.lowercase() }
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
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/vaccinations")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                _vaccinations.value = snapshot.documents.mapNotNull { it.toVaccinationModelSafe() }
                    .sortedWith(compareBy<VaccinationModel> { it.completed }.thenBy { it.scheduledDateMillis })
            }

        medicationsListener = userDoc.collection("medications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/medications")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                _medications.value = snapshot.documents.mapNotNull { it.toMedicationModelSafe() }
                    .sortedWith(compareByDescending<MedicationModel> { it.active }.thenBy { it.reminderHour }.thenBy { it.reminderMinute })
            }

        medicalRecordsListener = userDoc.collection("medicalRecords")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/medicalRecords")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                _medicalRecords.value = snapshot.documents.mapNotNull { it.toMedicalRecordSafe() }
                    .sortedByDescending { it.recordDateMillis }
            }

        weightRecordsListener = userDoc.collection("weightRecords")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/weightRecords")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                _weightRecords.value = snapshot.documents.mapNotNull { it.toWeightRecordSafe() }
                    .sortedByDescending { it.recordedAtMillis }
            }

        checkupRecordsListener = userDoc.collection("checkupRecords")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/checkupRecords")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                _checkupRecords.value = snapshot.documents.mapNotNull { it.toCheckupRecordSafe() }
                    .sortedByDescending { it.recordDateMillis }
            }

        schedulesListener = userDoc.collection("schedules")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$userId/schedules")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                _schedules.value = snapshot.documents.mapNotNull { it.toScheduleModelSafe() }
            }
    }

    private fun refreshUserHeaderFromAuth() {
        val user = runCatching { auth.currentUser }.getOrNull() ?: return
        val isAnon = user.isAnonymous
        val resolvedName = user.displayName?.ifBlank { null }
            ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: if (isAnon) "Khách" else "Bạn"
        _userProfile.update { current ->
            current.copy(
                name = resolvedName,
                email = user.email.orEmpty(),
                phone = user.phoneNumber ?: current.phone,
                avatarUri = user.photoUrl?.toString() ?: current.avatarUri,
                isAnonymous = isAnon,
                rank = if (isAnon) "Tài khoản Khách" else "Hội viên",
            )
        }
        _accounts.value = listOf(AccountUser(id = user.uid, name = resolvedName, isCurrent = true))
    }

    private fun removeListeners() {
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
    }

    fun detachUser() {
        attachedUserId = null
        removeListeners()
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
        isAnonymous: Boolean? = null,
        clearAvatar: Boolean = false,
        clearHeaderBgUri: Boolean = false,
    ) {
        val clearBgUri = clearHeaderBgUri || (headerBgColorHex != null && headerBgUri == null)
        _userProfile.update { current ->
            var updated = current
            if (name != null) updated = updated.copy(name = name)
            if (email != null) updated = updated.copy(email = email)
            if (phone != null) updated = updated.copy(phone = phone)
            if (dob != null) updated = updated.copy(dob = dob)
            if (address != null) updated = updated.copy(address = address)
            if (clearAvatar) updated = updated.copy(avatarUri = null, avatarRes = null)
            if (avatarUri != null) updated = updated.copy(avatarUri = avatarUri, avatarRes = null)
            if (avatarRes != null) updated = updated.copy(avatarRes = avatarRes, avatarUri = null)
            if (headerBgColorHex != null) updated = updated.copy(headerBgColorHex = headerBgColorHex, headerBgUri = null)
            if (clearBgUri) updated = updated.copy(headerBgUri = null)
            if (headerBgUri != null) updated = updated.copy(headerBgUri = headerBgUri)
            if (isAnonymous != null) {
                updated = updated.copy(
                    isAnonymous = isAnonymous,
                    rank = if (isAnonymous) "Tài khoản Khách" else "Hội viên",
                )
            }
            updated
        }
        runCatching {
            val currentUser = auth.currentUser ?: return@runCatching
            val uid = currentUser.uid
            name?.trim()?.take(80)?.takeIf { it.isNotEmpty() }?.let { cleanName ->
                currentUser.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(cleanName).build()
                )
            }
            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            name?.trim()?.take(80)?.takeIf { it.isNotEmpty() }?.let { updates["name"] = it }
            email?.trim()?.take(120)?.takeIf { it.isNotEmpty() }?.let { updates["email"] = it }
            phone?.trim()?.take(30)?.let { updates["phone"] = it }
            dob?.trim()?.take(30)?.let { updates["dob"] = it }
            address?.trim()?.take(200)?.let { updates["address"] = it }
            when {
                clearAvatar -> updates["avatarUri"] = ""
                avatarRes == R.drawable.dog -> updates["avatarUri"] = "preset:dog"
                avatarRes == R.drawable.cat -> updates["avatarUri"] = "preset:cat"
                avatarRes == R.drawable.rabbit -> updates["avatarUri"] = "preset:rabbit"
                avatarUri != null -> updates["avatarUri"] = avatarUri.trim().take(600)
            }
            headerBgColorHex?.let { updates["headerBgColorHex"] = it }
            if (clearBgUri) {
                updates["headerBgUri"] = ""
            } else {
                headerBgUri?.trim()?.take(600)?.let { updates["headerBgUri"] = it }
            }

            if (updates.size > 1) {
                firestore.collection("users").document(uid)
                    .set(updates, SetOptions.merge())
                    .addOnFailureListener { e ->
                        handleFirestoreError(e, OperationType.UPDATE, "users/$uid")
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
        val existingPet = _pets.value.find { it.id == pet.id }
        val resolvedWeightKg = if (pet.weightKg > 0.0) {
            pet.weightKg.coerceIn(0.0, 250.0)
        } else {
            DateUtils.parseWeightKg(pet.weight)
        }
        val resolvedWeightText = pet.weight.trim().ifBlank {
            if (resolvedWeightKg > 0.0) String.format(Locale.US, "%.1f kg", resolvedWeightKg) else "Chưa có"
        }
        val normalized = pet.copy(
            name = pet.name.trim().take(60),
            species = pet.species.trim().take(30).ifBlank { "Chó" },
            breed = pet.breed.trim().take(80),
            dateOfBirth = pet.dateOfBirth.trim().take(30),
            age = pet.age.trim().take(40),
            weight = resolvedWeightText.take(30),
            weightKg = resolvedWeightKg,
            gender = pet.gender.trim().take(20).ifBlank { "Đực" },
            photoUri = pet.photoUri.trim().take(600),
            color = pet.color.trim().take(60),
            notes = pet.notes.trim().take(500),
            statusText = pet.statusText.trim().take(60).ifBlank { "Bình thường" },
            statusDetail = pet.statusDetail.trim().take(160),
            temperature = pet.temperature.trim().take(30),
            activity = pet.activity.trim().take(120),
            diet = pet.diet.trim().take(200),
            lastUpdated = pet.lastUpdated.trim().take(60),
        )

        _pets.update { list ->
            val index = list.indexOfFirst { it.id == normalized.id }
            if (index != -1) {
                list.toMutableList().apply { set(index, normalized) }
            } else {
                list + normalized
            }
        }
        if (_currentSelectedPetId.value.isBlank()) {
            _currentSelectedPetId.value = normalized.id
        }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val root = firestore.collection("users").document(uid)
            val petRef = root.collection("pets").document(normalized.id)
            val mutableFields = mutableMapOf<String, Any>(
                "name" to normalized.name,
                "species" to normalized.species,
                "breed" to normalized.breed,
                "dateOfBirth" to normalized.dateOfBirth,
                "age" to normalized.age,
                "weight" to normalized.weight,
                "weightKg" to normalized.weightKg,
                "gender" to normalized.gender,
                "iconRes" to normalized.safeIconRes,
                "photoUri" to normalized.photoUri,
                "color" to normalized.color,
                "notes" to normalized.notes,
                "statusText" to normalized.statusText,
                "statusDetail" to normalized.statusDetail,
                "isFavorite" to normalized.isFavorite,
                "temperature" to normalized.temperature,
                "activity" to normalized.activity,
                "diet" to normalized.diet,
                "lastUpdated" to normalized.lastUpdated,
                "updatedAt" to FieldValue.serverTimestamp(),
            )

            if (existingPet == null) {
                val createPayload = mutableFields + mapOf(
                    "userId" to uid,
                    "createdByAi" to normalized.createdByAi,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
                petRef.set(createPayload)
                    .addOnFailureListener { e ->
                        handleFirestoreError(e, OperationType.CREATE, "users/$uid/pets/${normalized.id}")
                        _userMessage.value = "Không thể lưu hồ sơ lên đám mây."
                    }
            } else {
                val nameChanged = existingPet.name != normalized.name && normalized.name.isNotBlank()
                if (nameChanged) {
                    cascadeRenamePet(uid, normalized.id, normalized.name, petRef, mutableFields)
                } else {
                    petRef.update(mutableFields)
                        .addOnFailureListener { e ->
                            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/pets/${normalized.id}")
                            _userMessage.value = "Không thể cập nhật hồ sơ lên đám mây."
                        }
                }
            }
        }
    }

    private fun cascadeRenamePet(
        uid: String,
        petId: String,
        newName: String,
        petRef: DocumentReference,
        petUpdates: Map<String, Any>,
    ) {
        val root = firestore.collection("users").document(uid)
        val childRefs = mutableListOf<DocumentReference>()
        var remaining = PET_CHILD_COLLECTIONS.size
        for (col in PET_CHILD_COLLECTIONS) {
            root.collection(col).whereEqualTo("petId", petId).get()
                .addOnSuccessListener { snap ->
                    snap.documents.mapTo(childRefs) { it.reference }
                }
                .addOnCompleteListener {
                    remaining--
                    if (remaining == 0) {
                        val ops = buildList<(WriteBatch) -> Unit> {
                            add { batch -> batch.update(petRef, petUpdates) }
                            childRefs.forEach { ref ->
                                add { batch ->
                                    batch.update(
                                        ref,
                                        mapOf(
                                            "petName" to newName,
                                            "updatedAt" to FieldValue.serverTimestamp(),
                                        )
                                    )
                                }
                            }
                        }
                        commitBatches(ops, "users/$uid/pets/$petId", OperationType.UPDATE)
                    }
                }
        }
    }

    fun deletePet(id: String) {
        _pets.update { list -> list.filterNot { it.id == id } }
        _vaccinations.update { list -> list.filterNot { it.petId == id } }
        _medications.update { list -> list.filterNot { it.petId == id } }
        _medicalRecords.update { list -> list.filterNot { it.petId == id } }
        _weightRecords.update { list -> list.filterNot { it.petId == id } }
        _checkupRecords.update { list -> list.filterNot { it.petId == id } }
        _schedules.update { list -> list.filterNot { it.petId == id } }
        if (_currentSelectedPetId.value == id) {
            _currentSelectedPetId.value = _pets.value.firstOrNull()?.id.orEmpty()
        }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val root = firestore.collection("users").document(uid)
            val petRef = root.collection("pets").document(id)
            val refs = mutableListOf<DocumentReference>()
            var remaining = PET_CHILD_COLLECTIONS.size

            for (col in PET_CHILD_COLLECTIONS) {
                root.collection(col).whereEqualTo("petId", id).get()
                    .addOnSuccessListener { snap ->
                        snap.documents.mapTo(refs) { it.reference }
                    }
                    .addOnCompleteListener {
                        remaining--
                        if (remaining == 0) {
                            // Keep parent pet doc last so retry remains safe if a chunk fails.
                            refs.add(petRef)
                            commitBatches(
                                refs.map { ref -> { b: WriteBatch -> b.delete(ref) } },
                                "users/$uid/pets/$id",
                                OperationType.DELETE,
                            )
                        }
                    }
            }
        }
    }

    private fun commitBatches(
        ops: List<(WriteBatch) -> Unit>,
        path: String,
        opType: OperationType,
    ) {
        ops.chunked(MAX_BATCH_OPS).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { it(batch) }
            batch.commit().addOnFailureListener { e ->
                handleFirestoreError(e, opType, path)
            }
        }
    }

    fun toggleFavorite(id: String) {
        var nextFav = false
        _pets.update { list ->
            list.map {
                if (it.id == id) {
                    nextFav = !it.isFavorite
                    it.copy(isFavorite = nextFav)
                } else {
                    it
                }
            }
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("pets").document(id)
                .update(
                    mapOf(
                        "isFavorite" to nextFav,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.UPDATE, "users/$uid/pets/$id")
                }
        }
    }

    fun getVaccinationsForPet(petId: String): List<VaccinationModel> =
        _vaccinations.value.filter { it.petId == petId }

    fun addVaccination(vaccination: VaccinationModel) {
        val petName = vaccination.petName.ifBlank { getPetById(vaccination.petId)?.name.orEmpty() }
        val schedMillis = vaccination.scheduledDateMillis.takeIf { it > 0L }
            ?: DateUtils.parseDateMillis(vaccination.dateTime)
            ?: 0L
        val isCompleted = vaccination.completed || vaccination.status.equals("Đã tiêm", ignoreCase = true)
        val normalized = vaccination.copy(
            petName = petName.take(60),
            title = vaccination.title.trim().take(100),
            dateTime = vaccination.dateTime.trim().take(40),
            status = vaccination.status.trim().take(40).ifBlank { if (isCompleted) "Đã tiêm" else "Sắp tới" },
            scheduledDateMillis = schedMillis.coerceAtLeast(0L),
            completed = isCompleted,
            reminderDaysBefore = vaccination.reminderDaysBefore.coerceIn(0L, 30L),
            notes = vaccination.notes.trim().take(300),
        )
        _vaccinations.update { listOf(normalized) + it.filterNot { item -> item.id == normalized.id } }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val payload = mapOf(
                "userId" to uid,
                "petId" to normalized.petId,
                "petName" to normalized.petName,
                "title" to normalized.title,
                "dateTime" to normalized.dateTime,
                "status" to normalized.status,
                "scheduledDateMillis" to normalized.scheduledDateMillis,
                "completed" to normalized.completed,
                "reminderEnabled" to normalized.reminderEnabled,
                "reminderDaysBefore" to normalized.reminderDaysBefore,
                "notes" to normalized.notes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("vaccinations").document(normalized.id).set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "users/$uid/vaccinations/${normalized.id}")
                }
        }
    }

    fun toggleVaccinationCompleted(vaccination: VaccinationModel) {
        val nextCompleted = !vaccination.completed
        val nextStatus = if (nextCompleted) "Đã tiêm" else "Sắp tới"
        _vaccinations.update { list ->
            list.map {
                if (it.id == vaccination.id) it.copy(completed = nextCompleted, status = nextStatus)
                else it
            }
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("vaccinations").document(vaccination.id)
                .update(
                    mapOf(
                        "completed" to nextCompleted,
                        "status" to nextStatus,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.UPDATE, "users/$uid/vaccinations/${vaccination.id}")
                }
        }
    }

    fun deleteVaccination(vaccinationId: String) {
        _vaccinations.update { list -> list.filterNot { it.id == vaccinationId } }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("vaccinations").document(vaccinationId).delete()
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.DELETE, "users/$uid/vaccinations/$vaccinationId")
                }
        }
    }

    fun getMedicationsForPet(petId: String): List<MedicationModel> =
        _medications.value.filter { it.petId == petId }

    fun addMedication(medication: MedicationModel) {
        val petName = medication.petName.ifBlank { getPetById(medication.petId)?.name.orEmpty() }
        val parsedHm = NotificationHelper.parseHourMinute(medication.scheduleTime)
        val hour = (parsedHm?.first?.toLong() ?: medication.reminderHour).coerceIn(0L, 23L)
        val minute = (parsedHm?.second?.toLong() ?: medication.reminderMinute).coerceIn(0L, 59L)
        val normalized = medication.copy(
            petName = petName.take(60),
            name = medication.name.trim().take(100),
            dosage = medication.dosage.trim().take(80),
            scheduleTime = medication.scheduleTime.trim().take(60),
            quantity = medication.quantity.trim().take(60),
            status = medication.status.trim().take(40).ifBlank { "Đang dùng" },
            reminderHour = hour,
            reminderMinute = minute,
        )
        _medications.update { listOf(normalized) + it.filterNot { item -> item.id == normalized.id } }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val payload = mapOf(
                "userId" to uid,
                "petId" to normalized.petId,
                "petName" to normalized.petName,
                "name" to normalized.name,
                "dosage" to normalized.dosage,
                "scheduleTime" to normalized.scheduleTime,
                "quantity" to normalized.quantity,
                "status" to normalized.status,
                "reminderHour" to normalized.reminderHour,
                "reminderMinute" to normalized.reminderMinute,
                "endDateMillis" to normalized.endDateMillis.coerceAtLeast(0L),
                "lastTakenDateKey" to normalized.lastTakenDateKey.take(20),
                "active" to normalized.active,
                "reminderEnabled" to normalized.reminderEnabled,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("medications").document(normalized.id).set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "users/$uid/medications/${normalized.id}")
                }
        }
    }

    fun toggleMedicationTakenToday(medication: MedicationModel) {
        val today = DateUtils.todayKey()
        val nextKey = if (medication.lastTakenDateKey == today) "" else today
        _medications.update { list ->
            list.map { if (it.id == medication.id) it.copy(lastTakenDateKey = nextKey) else it }
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("medications").document(medication.id)
                .update(
                    mapOf(
                        "lastTakenDateKey" to nextKey,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.UPDATE, "users/$uid/medications/${medication.id}")
                }
        }
    }

    fun deleteMedication(medicationId: String) {
        _medications.update { list -> list.filterNot { it.id == medicationId } }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            firestore.collection("users").document(uid)
                .collection("medications").document(medicationId).delete()
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.DELETE, "users/$uid/medications/$medicationId")
                }
        }
    }

    fun getMedicalRecordsForPet(petId: String): List<MedicalRecordEntryModel> =
        _medicalRecords.value.filter { it.petId == petId }

    fun addMedicalRecord(record: MedicalRecordEntryModel) {
        val petName = record.petName.ifBlank { getPetById(record.petId)?.name.orEmpty() }
        val recordMillis = record.recordDateMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        val normalized = record.copy(
            petName = petName.take(60),
            dateTime = record.dateTime.trim().take(40),
            activityType = record.activityType.trim().take(80),
            description = record.description.trim().take(500),
            photoUri = record.photoUri?.trim()?.take(600),
            emotion = record.emotion.trim().take(40),
            recordDateMillis = recordMillis,
        )
        _medicalRecords.update { listOf(normalized) + it }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val payload = mapOf(
                "userId" to uid,
                "petId" to normalized.petId,
                "petName" to normalized.petName,
                "dateTime" to normalized.dateTime,
                "activityType" to normalized.activityType,
                "description" to normalized.description,
                "photoUri" to (normalized.photoUri ?: ""),
                "emotion" to normalized.emotion,
                "recordDateMillis" to normalized.recordDateMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("medicalRecords").document(normalized.id).set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "users/$uid/medicalRecords/${normalized.id}")
                }
        }
    }

    fun getScheduleById(id: String): ScheduleModel? =
        _schedules.value.find { it.id == id }

    fun addSchedule(schedule: ScheduleModel) {
        val triggerMillis = schedule.triggerAtMillis.takeIf { it > 0L }
            ?: NotificationHelper.parseScheduleMillis(schedule.dateText, schedule.timeText)
            ?: 0L
        val normalized = schedule.copy(
            title = schedule.title.trim().take(100),
            description = schedule.description.trim().take(300),
            petName = schedule.petName.trim().take(60),
            dateText = schedule.dateText.trim().take(30),
            timeText = schedule.timeText.trim().take(20),
            timeOrDateLabel = schedule.timeOrDateLabel.trim().take(40),
            triggerAtMillis = triggerMillis.coerceAtLeast(0L),
        )
        _schedules.update { it + normalized }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val payload = mapOf(
                "userId" to uid,
                "title" to normalized.title,
                "description" to normalized.description,
                "petId" to normalized.petId,
                "petName" to normalized.petName,
                "dateText" to normalized.dateText,
                "timeText" to normalized.timeText,
                "timeOrDateLabel" to normalized.timeOrDateLabel,
                "triggerAtMillis" to normalized.triggerAtMillis,
                "reminderEnabled" to normalized.reminderEnabled,
                "isCompleted" to normalized.isCompleted,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("schedules").document(normalized.id).set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "users/$uid/schedules/${normalized.id}")
                }
        }
    }

    fun updateSchedule(schedule: ScheduleModel) {
        val triggerMillis = schedule.triggerAtMillis.takeIf { it > 0L }
            ?: NotificationHelper.parseScheduleMillis(schedule.dateText, schedule.timeText)
            ?: 0L
        val normalized = schedule.copy(
            title = schedule.title.trim().take(100),
            description = schedule.description.trim().take(300),
            petName = schedule.petName.trim().take(60),
            dateText = schedule.dateText.trim().take(30),
            timeText = schedule.timeText.trim().take(20),
            timeOrDateLabel = schedule.timeOrDateLabel.trim().take(40),
            triggerAtMillis = triggerMillis.coerceAtLeast(0L),
        )
        _schedules.update { list ->
            val index = list.indexOfFirst { it.id == normalized.id }
            if (index != -1) {
                list.toMutableList().apply { set(index, normalized) }
            } else {
                list + normalized
            }
        }
        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val updates = mapOf(
                "title" to normalized.title,
                "description" to normalized.description,
                "petId" to normalized.petId,
                "petName" to normalized.petName,
                "dateText" to normalized.dateText,
                "timeText" to normalized.timeText,
                "timeOrDateLabel" to normalized.timeOrDateLabel,
                "triggerAtMillis" to normalized.triggerAtMillis,
                "reminderEnabled" to normalized.reminderEnabled,
                "isCompleted" to normalized.isCompleted,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("schedules").document(normalized.id).update(updates)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.UPDATE, "users/$uid/schedules/${normalized.id}")
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
                    handleFirestoreError(e, OperationType.DELETE, "users/$uid/schedules/$id")
                }
        }
    }

    fun getWeightRecordsForPet(petId: String): List<WeightRecordModel> =
        _weightRecords.value.filter { it.petId == petId }

    fun addWeightRecord(record: WeightRecordModel) {
        val petName = record.petName.ifBlank { getPetById(record.petId)?.name.orEmpty() }
        val resolvedKg = if (record.weightKg > 0.0) record.weightKg else DateUtils.parseWeightKg(record.weight)
        val recordedMillis = record.recordedAtMillis.takeIf { it > 0L }
            ?: DateUtils.parseDateMillis(record.dateTime)
            ?: System.currentTimeMillis()
        val normalized = record.copy(
            petName = petName.take(60),
            weight = record.weight.trim().take(30),
            weightKg = resolvedKg.coerceIn(0.0, 250.0),
            dateTime = record.dateTime.trim().take(40),
            recordedAtMillis = recordedMillis,
            status = record.status.trim().take(40).ifBlank { "Ổn định" },
        )
        _weightRecords.update { listOf(normalized) + it }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val payload = mapOf(
                "userId" to uid,
                "petId" to normalized.petId,
                "petName" to normalized.petName,
                "weight" to normalized.weight,
                "weightKg" to normalized.weightKg,
                "dateTime" to normalized.dateTime,
                "recordedAtMillis" to normalized.recordedAtMillis,
                "status" to normalized.status,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("weightRecords").document(normalized.id).set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "users/$uid/weightRecords/${normalized.id}")
                }
        }
    }

    fun getCheckupRecordsForPet(petId: String): List<CheckupRecordModel> =
        _checkupRecords.value.filter { it.petId == petId }

    fun addCheckupRecord(record: CheckupRecordModel) {
        val petName = record.petName.ifBlank { getPetById(record.petId)?.name.orEmpty() }
        val recordMillis = record.recordDateMillis.takeIf { it > 0L }
            ?: DateUtils.parseDateMillis(record.dateTime)
            ?: System.currentTimeMillis()
        val normalized = record.copy(
            petName = petName.take(60),
            title = record.title.trim().take(100),
            doctorOrClinic = record.doctorOrClinic.trim().take(100),
            dateTime = record.dateTime.trim().take(40),
            result = record.result.trim().take(300),
            status = record.status.trim().take(40).ifBlank { "Hoàn thành" },
            recordDateMillis = recordMillis,
        )
        _checkupRecords.update { listOf(normalized) + it }

        runCatching {
            val uid = auth.currentUser?.uid ?: return@runCatching
            val payload = mapOf(
                "userId" to uid,
                "petId" to normalized.petId,
                "petName" to normalized.petName,
                "title" to normalized.title,
                "doctorOrClinic" to normalized.doctorOrClinic,
                "dateTime" to normalized.dateTime,
                "result" to normalized.result,
                "status" to normalized.status,
                "recordDateMillis" to normalized.recordDateMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            firestore.collection("users").document(uid)
                .collection("checkupRecords").document(normalized.id).set(payload)
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "users/$uid/checkupRecords/${normalized.id}")
                }
        }
    }

    suspend fun deleteUserCloudData(uid: String) {
        val root = firestore.collection("users").document(uid)
        val refsToDelete = mutableListOf<DocumentReference>()
        val allCols = listOf("pets") + PET_CHILD_COLLECTIONS
        for (col in allCols) {
            val snap = root.collection(col).get().await()
            snap.documents.mapTo(refsToDelete) { it.reference }
        }
        val userDoc = root.get().await()
        val username = userDoc.getString("username")?.trim()?.lowercase().orEmpty()
        if (username.isNotEmpty()) {
            refsToDelete.add(firestore.collection("usernames").document(username))
        }
        refsToDelete.add(root)

        refsToDelete.chunked(MAX_BATCH_OPS).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }

    fun resetToDefaults() {
        _isLoading.value = false
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
    val pet = toObject(PetModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.copy(id = id)
    if (pet != null) {
        val safeIcon = resolvePetIconRes(pet.iconRes, pet.species)
        val kg = if (pet.weightKg > 0.0) pet.weightKg else DateUtils.parseWeightKg(pet.weight)
        pet.copy(iconRes = safeIcon, weightKg = kg)
    } else null
}.getOrElse {
    runCatching {
        val weight = getString("weight") ?: ""
        val kg = getDouble("weightKg") ?: DateUtils.parseWeightKg(weight)
        val species = getString("species") ?: "Chó"
        PetModel(
            id = id,
            name = getString("name") ?: "Thú cưng",
            species = species,
            breed = getString("breed") ?: "",
            dateOfBirth = getString("dateOfBirth") ?: "",
            age = getString("age") ?: "",
            weight = weight,
            weightKg = kg,
            gender = getString("gender") ?: "Đực",
            iconRes = resolvePetIconRes(getLong("iconRes")?.toInt() ?: 0, species),
            photoUri = getString("photoUri") ?: "",
            color = getString("color") ?: "",
            notes = getString("notes") ?: "",
            statusText = getString("statusText") ?: "Bình thường",
            statusDetail = getString("statusDetail") ?: "",
            isFavorite = getBoolean("isFavorite") ?: false,
            createdByAi = getBoolean("createdByAi") ?: false,
            temperature = getString("temperature") ?: "",
            activity = getString("activity") ?: "",
            diet = getString("diet") ?: "",
            lastUpdated = getString("lastUpdated") ?: "",
            userId = getString("userId") ?: "",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toVaccinationModelSafe(): VaccinationModel? = runCatching {
    toObject(VaccinationModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.copy(id = id)
}.getOrElse {
    runCatching {
        val status = getString("status") ?: "Đã tiêm"
        VaccinationModel(
            id = id,
            petId = getString("petId") ?: "",
            petName = getString("petName") ?: "",
            title = getString("title") ?: "",
            dateTime = getString("dateTime") ?: "",
            status = status,
            scheduledDateMillis = getLong("scheduledDateMillis") ?: 0L,
            completed = getBoolean("completed") ?: status.equals("Đã tiêm", ignoreCase = true),
            reminderEnabled = getBoolean("reminderEnabled") ?: true,
            reminderDaysBefore = getLong("reminderDaysBefore") ?: 2L,
            notes = getString("notes") ?: "",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toMedicationModelSafe(): MedicationModel? = runCatching {
    toObject(MedicationModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.copy(id = id)
}.getOrElse {
    runCatching {
        val status = getString("status") ?: "Đang dùng"
        MedicationModel(
            id = id,
            petId = getString("petId") ?: "",
            petName = getString("petName") ?: "",
            name = getString("name") ?: "",
            dosage = getString("dosage") ?: "",
            scheduleTime = getString("scheduleTime") ?: "",
            quantity = getString("quantity") ?: "",
            status = status,
            reminderHour = getLong("reminderHour") ?: 8L,
            reminderMinute = getLong("reminderMinute") ?: 0L,
            endDateMillis = getLong("endDateMillis") ?: 0L,
            lastTakenDateKey = getString("lastTakenDateKey") ?: "",
            active = getBoolean("active") ?: true,
            reminderEnabled = getBoolean("reminderEnabled") ?: true,
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toMedicalRecordSafe(): MedicalRecordEntryModel? = runCatching {
    toObject(MedicalRecordEntryModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.copy(id = id)
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
            recordDateMillis = getLong("recordDateMillis") ?: 0L,
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toWeightRecordSafe(): WeightRecordModel? = runCatching {
    val rec = toObject(WeightRecordModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.copy(id = id)
    if (rec != null && rec.weightKg <= 0.0) {
        rec.copy(weightKg = DateUtils.parseWeightKg(rec.weight))
    } else rec
}.getOrElse {
    runCatching {
        val wText = getString("weight") ?: ""
        WeightRecordModel(
            id = id,
            petId = getString("petId") ?: "",
            petName = getString("petName") ?: "",
            weight = wText,
            weightKg = getDouble("weightKg") ?: DateUtils.parseWeightKg(wText),
            dateTime = getString("dateTime") ?: "",
            recordedAtMillis = getLong("recordedAtMillis") ?: 0L,
            status = getString("status") ?: "Ổn định",
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toCheckupRecordSafe(): CheckupRecordModel? = runCatching {
    toObject(CheckupRecordModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.copy(id = id)
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
            recordDateMillis = getLong("recordDateMillis") ?: 0L,
        )
    }.getOrNull()
}

private fun DocumentSnapshot.toScheduleModelSafe(): ScheduleModel? = runCatching {
    toObject(ScheduleModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        ?.copy(id = id)
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
            triggerAtMillis = getLong("triggerAtMillis") ?: 0L,
            reminderEnabled = getBoolean("reminderEnabled") ?: true,
        )
    }.getOrNull()
}
