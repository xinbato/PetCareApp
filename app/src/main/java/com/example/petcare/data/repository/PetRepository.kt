package com.example.petcare.data.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.petcare.R
import com.example.petcare.data.model.MedicalRecordEntryModel
import com.example.petcare.data.model.MedicationModel
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.VaccinationModel

object PetRepository {
    val pets = mutableStateListOf(
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

    val vaccinations = mutableStateListOf(
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

    val weightRecords = mutableStateListOf(
        com.example.petcare.data.model.WeightRecordModel(
            id = "w1",
            petId = "1",
            petName = "Momo",
            weight = "12.4 kg",
            dateTime = "01/09/2026",
            status = "Ổn định",
        ),
        com.example.petcare.data.model.WeightRecordModel(
            id = "w2",
            petId = "1",
            petName = "Momo",
            weight = "12.2 kg",
            dateTime = "15/08/2026",
            status = "Ổn định",
        ),
        com.example.petcare.data.model.WeightRecordModel(
            id = "w3",
            petId = "1",
            petName = "Momo",
            weight = "11.8 kg",
            dateTime = "01/08/2026",
            status = "Bình thường",
        ),
        com.example.petcare.data.model.WeightRecordModel(
            id = "w4",
            petId = "2",
            petName = "Luna",
            weight = "4.2 kg",
            dateTime = "01/09/2026",
            status = "Ăn ít hơn",
        ),
        com.example.petcare.data.model.WeightRecordModel(
            id = "w5",
            petId = "2",
            petName = "Luna",
            weight = "4.5 kg",
            dateTime = "15/08/2026",
            status = "Ổn định",
        ),
        com.example.petcare.data.model.WeightRecordModel(
            id = "w6",
            petId = "3",
            petName = "Cậu Vàng",
            weight = "5.0 kg",
            dateTime = "01/09/2026",
            status = "Khỏe mạnh",
        ),
        com.example.petcare.data.model.WeightRecordModel(
            id = "w7",
            petId = "3",
            petName = "Cậu Vàng",
            weight = "4.8 kg",
            dateTime = "10/08/2026",
            status = "Đang lớn",
        ),
    )

    val checkupRecords = mutableStateListOf(
        com.example.petcare.data.model.CheckupRecordModel(
            id = "c1",
            petId = "1",
            petName = "Momo",
            title = "Khám định kỳ & xét nghiệm máu",
            doctorOrClinic = "BS. Nguyễn Văn Tuấn",
            dateTime = "12/08/2026",
            result = "Khỏe mạnh, thể trạng tốt",
        ),
        com.example.petcare.data.model.CheckupRecordModel(
            id = "c2",
            petId = "1",
            petName = "Momo",
            title = "Kiểm tra răng miệng & tim phổi",
            doctorOrClinic = "Bệnh viện Thú y PetCare",
            dateTime = "12/02/2026",
            result = "Răng sạch, nhịp tim đều",
        ),
        com.example.petcare.data.model.CheckupRecordModel(
            id = "c3",
            petId = "2",
            petName = "Luna",
            title = "Khám tiêu hóa & kiểm tra lông da",
            doctorOrClinic = "BS. Trần Thu Hà",
            dateTime = "25/08/2026",
            result = "Cần theo dõi men vi sinh",
        ),
        com.example.petcare.data.model.CheckupRecordModel(
            id = "c4",
            petId = "3",
            petName = "Cậu Vàng",
            title = "Khám tổng quát định kỳ 6 tháng",
            doctorOrClinic = "Phòng khám thú y PetCare",
            dateTime = "02/09/2026",
            result = "Rất năng động & bình thường",
        ),
    )

    val medications = mutableStateListOf(
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

    val medicalRecords = mutableStateListOf(
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

    val currentSelectedPetId = mutableStateOf("1")

    fun getSelectedPet(): PetModel {
        return pets.find { it.id == currentSelectedPetId.value } ?: pets.firstOrNull() ?: PetModel(
            id = "1",
            name = "Momo",
            iconRes = R.drawable.dog,
        )
    }

    fun getPetById(id: String): PetModel? {
        return pets.find { it.id == id }
    }

    fun savePet(pet: PetModel) {
        val index = pets.indexOfFirst { it.id == pet.id }
        if (index != -1) {
            pets[index] = pet
        } else {
            pets.add(pet)
        }
    }

    fun deletePet(id: String) {
        pets.removeAll { it.id == id }
        if (currentSelectedPetId.value == id && pets.isNotEmpty()) {
            currentSelectedPetId.value = pets.first().id
        }
    }

    fun toggleFavorite(id: String) {
        val index = pets.indexOfFirst { it.id == id }
        if (index != -1) {
            val pet = pets[index]
            pets[index] = pet.copy(isFavorite = !pet.isFavorite)
        }
    }

    fun getVaccinationsForPet(petId: String): List<VaccinationModel> {
        return vaccinations.filter { it.petId == petId }
    }

    fun addVaccination(vaccination: VaccinationModel) {
        vaccinations.add(0, vaccination)
    }

    fun getMedicationsForPet(petId: String): List<MedicationModel> {
        return medications.filter { it.petId == petId }
    }

    fun addMedication(medication: MedicationModel) {
        medications.add(0, medication)
    }

    fun getMedicalRecordsForPet(petId: String): List<MedicalRecordEntryModel> {
        return medicalRecords.filter { it.petId == petId }
    }

    fun addMedicalRecord(record: MedicalRecordEntryModel) {
        medicalRecords.add(0, record)
    }
}
