package com.example.petcare.data.model

data class PetModel(
    val id: String,
    val name: String,
    val species: String = "Chó",
    val breed: String = "",
    val dateOfBirth: String = "14/05/2023",
    val age: String = "3 tuổi",
    val weight: String = "12.4 kg",
    val gender: String = "Đực",
    val iconRes: Int,
    val statusText: String = "Khỏe mạnh",
    val statusDetail: String = "Cân nặng 12.4 kg",
    val isFavorite: Boolean = false,
    val temperature: String = "38.4°C",
    val activity: String = "8,240 bước",
    val diet: String = "Tốt • 2/2 bữa",
    val lastUpdated: String = "hôm nay",
)

data class VaccinationModel(
    val id: String,
    val petId: String,
    val title: String,
    val dateTime: String,
    val status: String, // "Đã tiêm", "Nhắc lại"
)

data class MedicationModel(
    val id: String,
    val petId: String,
    val name: String,
    val dosage: String,
    val scheduleTime: String,
    val quantity: String = "30 viên",
    val status: String = "Đang dùng",
)

data class MedicalRecordEntryModel(
    val id: String,
    val petId: String,
    val petName: String,
    val dateTime: String,
    val activityType: String,
    val description: String,
    val photoUri: String? = null,
    val emotion: String = "Bình thường",
)
