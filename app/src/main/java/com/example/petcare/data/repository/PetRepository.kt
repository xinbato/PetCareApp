package com.example.petcare.data.repository

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

data class UserProfileData(
    val name: String = "User",
    val memberSince: String = "Thành viên từ 2026",
    val rank: String = "rank hội viên: kim cương",
    val avatarUri: String? = null,
    val avatarRes: Int? = null,
    val headerBgColorHex: Long = 0xFF338C63,
    val headerBgUri: String? = null,
    val email: String = "user@example.com",
    val phone: String = "0987654321",
    val dob: String = "15/08/1998",
    val address: String = "Hà Nội, Việt Nam",
)

data class AccountUser(
    val id: String,
    val name: String,
    val isCurrent: Boolean = false,
)

object PetRepository {
    private val _userProfile = MutableStateFlow(UserProfileData())
    val userProfile: StateFlow<UserProfileData> = _userProfile.asStateFlow()

    private val _accounts = MutableStateFlow(initialAccounts())
    val accounts: StateFlow<List<AccountUser>> = _accounts.asStateFlow()

    private val _pets = MutableStateFlow(initialPets())
    val pets: StateFlow<List<PetModel>> = _pets.asStateFlow()

    private val _vaccinations = MutableStateFlow(initialVaccinations())
    val vaccinations: StateFlow<List<VaccinationModel>> = _vaccinations.asStateFlow()

    private val _weightRecords = MutableStateFlow(initialWeightRecords())
    val weightRecords: StateFlow<List<WeightRecordModel>> = _weightRecords.asStateFlow()

    private val _checkupRecords = MutableStateFlow(initialCheckupRecords())
    val checkupRecords: StateFlow<List<CheckupRecordModel>> = _checkupRecords.asStateFlow()

    private val _medications = MutableStateFlow(initialMedications())
    val medications: StateFlow<List<MedicationModel>> = _medications.asStateFlow()

    private val _medicalRecords = MutableStateFlow(initialMedicalRecords())
    val medicalRecords: StateFlow<List<MedicalRecordEntryModel>> = _medicalRecords.asStateFlow()

    private val _schedules = MutableStateFlow(initialSchedules())
    val schedules: StateFlow<List<ScheduleModel>> = _schedules.asStateFlow()

    private val _petPlaces = MutableStateFlow(initialPetPlaces())
    val petPlaces: StateFlow<List<PetPlaceModel>> = _petPlaces.asStateFlow()

    private val _currentSelectedPetId = MutableStateFlow("1")
    val currentSelectedPetId: StateFlow<String> = _currentSelectedPetId.asStateFlow()

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
            ?: PetModel(
                id = "1",
                name = "Momo",
                iconRes = R.drawable.dog,
            )
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
    }

    fun deletePet(id: String) {
        _pets.update { list -> list.filterNot { it.id == id } }
        if (_currentSelectedPetId.value == id) {
            _pets.value.firstOrNull()?.let { firstPet ->
                _currentSelectedPetId.value = firstPet.id
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
    }

    fun getMedicationsForPet(petId: String): List<MedicationModel> =
        _medications.value.filter { it.petId == petId }

    fun addMedication(medication: MedicationModel) {
        _medications.update { listOf(medication) + it }
    }

    fun getMedicalRecordsForPet(petId: String): List<MedicalRecordEntryModel> =
        _medicalRecords.value.filter { it.petId == petId }

    fun addMedicalRecord(record: MedicalRecordEntryModel) {
        _medicalRecords.update { listOf(record) + it }
    }

    fun getScheduleById(id: String): ScheduleModel? =
        _schedules.value.find { it.id == id }

    fun addSchedule(schedule: ScheduleModel) {
        _schedules.update { it + schedule }
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
    }

    fun deleteSchedule(id: String) {
        _schedules.update { list -> list.filterNot { it.id == id } }
    }

    fun getWeightRecordsForPet(petId: String): List<WeightRecordModel> =
        _weightRecords.value.filter { it.petId == petId }

    fun addWeightRecord(record: WeightRecordModel) {
        _weightRecords.update { listOf(record) + it }
    }

    fun getCheckupRecordsForPet(petId: String): List<CheckupRecordModel> =
        _checkupRecords.value.filter { it.petId == petId }

    fun addCheckupRecord(record: CheckupRecordModel) {
        _checkupRecords.update { listOf(record) + it }
    }

    fun resetToDefaults() {
        _userProfile.value = UserProfileData()
        _accounts.value = initialAccounts()
        _pets.value = initialPets()
        _vaccinations.value = initialVaccinations()
        _weightRecords.value = initialWeightRecords()
        _checkupRecords.value = initialCheckupRecords()
        _medications.value = initialMedications()
        _medicalRecords.value = initialMedicalRecords()
        _schedules.value = initialSchedules()
        _petPlaces.value = initialPetPlaces()
        _currentSelectedPetId.value = "1"
    }

    private fun initialAccounts() = listOf(
        AccountUser("a1", "Dương đz", isCurrent = true),
        AccountUser("a2", "Nguyên đz", isCurrent = false),
    )

    private fun initialPets() = listOf(
        PetModel(
            id = "1",
            name = "Momo",
            species = "Chó",
            breed = "Golden Retriever",
            dateOfBirth = "14/05/2023",
            age = "3 tuổi",
            weight = "12.4 kg",
            gender = "Đực",
            iconRes = R.drawable.dog,
            statusText = "Khỏe mạnh",
            statusDetail = "Cân nặng 12.4 kg",
            isFavorite = true,
            temperature = "38.4°C",
            activity = "8,240 bước",
            diet = "Tốt • 2/2 bữa",
            lastUpdated = "hôm nay",
        ),
        PetModel(
            id = "2",
            name = "Luna",
            species = "Mèo",
            breed = "Mèo Anh Lông Ngắn",
            dateOfBirth = "20/08/2024",
            age = "2 tuổi",
            weight = "4.2 kg",
            gender = "Cái",
            iconRes = R.drawable.cat,
            statusText = "Cần theo dõi",
            statusDetail = "Ăn ít hơn bình thường",
            isFavorite = false,
            temperature = "38.9°C",
            activity = "3,120 bước",
            diet = "Ít • 1/2 bữa",
            lastUpdated = "hôm qua",
        ),
        PetModel(
            id = "3",
            name = "Cậu Vàng",
            species = "Chó",
            breed = "Cún Poodle",
            dateOfBirth = "10/01/2025",
            age = "1.5 tuổi",
            weight = "5.0 kg",
            gender = "Đực",
            iconRes = R.drawable.dog,
            statusText = "Khỏe mạnh",
            statusDetail = "Năng động",
            isFavorite = false,
            temperature = "38.2°C",
            activity = "9,400 bước",
            diet = "Tốt • 2/2 bữa",
            lastUpdated = "hôm nay",
        ),
    )

    private fun initialVaccinations() = listOf(
        VaccinationModel(
            id = "v1",
            petId = "1",
            title = "Dại",
            dateTime = "15/09/2026",
            status = "Đã tiêm",
        ),
        VaccinationModel(
            id = "v2",
            petId = "1",
            title = "5 bệnh",
            dateTime = "15/09/2027",
            status = "Nhắc lại",
        ),
        VaccinationModel(
            id = "v3",
            petId = "1",
            title = "Kennel Cough",
            dateTime = "01/12/2026",
            status = "Nhắc lại",
        ),
        VaccinationModel(
            id = "v4",
            petId = "2",
            title = "Dại cho mèo",
            dateTime = "10/08/2026",
            status = "Đã tiêm",
        ),
        VaccinationModel(
            id = "v5",
            petId = "2",
            title = "4 bệnh (FVRCP)",
            dateTime = "20/07/2026",
            status = "Đã tiêm",
        ),
        VaccinationModel(
            id = "v6",
            petId = "2",
            title = "Bạch cầu FeLV",
            dateTime = "15/11/2026",
            status = "Nhắc lại",
        ),
        VaccinationModel(
            id = "v7",
            petId = "3",
            title = "Tiêm Care & Parvo",
            dateTime = "05/09/2026",
            status = "Đã tiêm",
        ),
        VaccinationModel(
            id = "v8",
            petId = "3",
            title = "Dại định kỳ",
            dateTime = "10/10/2026",
            status = "Nhắc lại",
        ),
    )

    private fun initialWeightRecords() = listOf(
        WeightRecordModel(
            id = "w1",
            petId = "1",
            petName = "Momo",
            weight = "12.4 kg",
            dateTime = "01/09/2026",
            status = "Ổn định",
        ),
        WeightRecordModel(
            id = "w2",
            petId = "1",
            petName = "Momo",
            weight = "12.2 kg",
            dateTime = "15/08/2026",
            status = "Ổn định",
        ),
        WeightRecordModel(
            id = "w3",
            petId = "1",
            petName = "Momo",
            weight = "11.8 kg",
            dateTime = "01/08/2026",
            status = "Bình thường",
        ),
        WeightRecordModel(
            id = "w4",
            petId = "2",
            petName = "Luna",
            weight = "4.2 kg",
            dateTime = "01/09/2026",
            status = "Ăn ít hơn",
        ),
        WeightRecordModel(
            id = "w5",
            petId = "2",
            petName = "Luna",
            weight = "4.5 kg",
            dateTime = "15/08/2026",
            status = "Ổn định",
        ),
        WeightRecordModel(
            id = "w6",
            petId = "3",
            petName = "Cậu Vàng",
            weight = "5.0 kg",
            dateTime = "01/09/2026",
            status = "Khỏe mạnh",
        ),
        WeightRecordModel(
            id = "w7",
            petId = "3",
            petName = "Cậu Vàng",
            weight = "4.8 kg",
            dateTime = "10/08/2026",
            status = "Đang lớn",
        ),
    )

    private fun initialCheckupRecords() = listOf(
        CheckupRecordModel(
            id = "c1",
            petId = "1",
            petName = "Momo",
            title = "Khám định kỳ & xét nghiệm máu",
            doctorOrClinic = "BS. Nguyễn Văn Tuấn",
            dateTime = "12/08/2026",
            result = "Khỏe mạnh, thể trạng tốt",
        ),
        CheckupRecordModel(
            id = "c2",
            petId = "1",
            petName = "Momo",
            title = "Kiểm tra răng miệng & tim phổi",
            doctorOrClinic = "Bệnh viện Thú y PetCare",
            dateTime = "12/02/2026",
            result = "Răng sạch, nhịp tim đều",
        ),
        CheckupRecordModel(
            id = "c3",
            petId = "2",
            petName = "Luna",
            title = "Khám tiêu hóa & kiểm tra lông da",
            doctorOrClinic = "BS. Trần Thu Hà",
            dateTime = "25/08/2026",
            result = "Cần theo dõi men vi sinh",
        ),
        CheckupRecordModel(
            id = "c4",
            petId = "3",
            petName = "Cậu Vàng",
            title = "Khám tổng quát định kỳ 6 tháng",
            doctorOrClinic = "Phòng khám thú y PetCare",
            dateTime = "02/09/2026",
            result = "Rất năng động & bình thường",
        ),
    )

    private fun initialMedications() = listOf(
        MedicationModel(
            id = "m1",
            petId = "1",
            name = "Omega 3",
            dosage = "1 viên",
            scheduleTime = "08:00 • mỗi ngày",
            quantity = "30 viên",
            status = "Đang dùng",
        ),
        MedicationModel(
            id = "m2",
            petId = "1",
            name = "Men tiêu hóa",
            dosage = "1 gói",
            scheduleTime = "20:00 • 7 ngày",
            quantity = "14 gói",
            status = "Đang dùng",
        ),
    )

    private fun initialMedicalRecords() = listOf(
        MedicalRecordEntryModel(
            id = "r1",
            petId = "1",
            petName = "Momo",
            dateTime = "26/9/2026-12h30",
            activityType = "Bỏ ăn",
            description = "10 cục",
            emotion = "Mệt mỏi",
        ),
        MedicalRecordEntryModel(
            id = "r2",
            petId = "1",
            petName = "Momo",
            dateTime = "25/9/2026-16h30",
            activityType = "Đi ngoài nhiều",
            description = "được 250ml",
            emotion = "Yếu ớt",
        ),
        MedicalRecordEntryModel(
            id = "r3",
            petId = "1",
            petName = "Momo",
            dateTime = "10/10/2026- 13h20",
            activityType = "Xuất huyết đại tràng",
            description = "Chết mất xác",
            emotion = "Cấp cứu",
        ),
    )

    private fun initialSchedules() = listOf(
        ScheduleModel(
            id = "s1",
            title = "Bữa tối",
            description = "Cho Momo ăn hạt và pate",
            petId = "1",
            petName = "Momo",
            dateText = "12/10/2026",
            timeText = "18:00",
            timeOrDateLabel = "18:00",
        ),
        ScheduleModel(
            id = "s2",
            title = "Tiêm phòng",
            description = "Tiêm ngừa dại định kỳ tại phòng khám",
            petId = "2",
            petName = "Luna",
            dateText = "13/10/2026",
            timeText = "09:00",
            timeOrDateLabel = "Ngày mai",
        ),
        ScheduleModel(
            id = "s3",
            title = "Tắm và vệ sinh",
            description = "Tắm sấy và cắt móng",
            petId = "1",
            petName = "Momo",
            dateText = "17/10/2026",
            timeText = "15:00",
            timeOrDateLabel = "Thứ 7",
        ),
    )

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
