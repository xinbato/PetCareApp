package com.example.petcare.data.model

data class PetModel(
    val id: String = "",
    val name: String = "",
    val species: String = "Chó",
    val breed: String = "",
    val dateOfBirth: String = "14/05/2023",
    val age: String = "3 tuổi",
    val weight: String = "12.4 kg",
    val gender: String = "Đực",
    val iconRes: Int = com.example.petcare.R.drawable.dog,
    val statusText: String = "Khỏe mạnh",
    val statusDetail: String = "Cân nặng 12.4 kg",
    val isFavorite: Boolean = false,
    val temperature: String = "38.4°C",
    val activity: String = "8,240 bước",
    val diet: String = "Tốt • 2/2 bữa",
    val lastUpdated: String = "hôm nay",
) {
    val safeIconRes: Int
        get() = if (iconRes != 0) iconRes else com.example.petcare.R.drawable.dog
}

data class VaccinationModel(
    val id: String = "",
    val petId: String = "",
    val title: String = "",
    val dateTime: String = "",
    val status: String = "Đã tiêm", // "Đã tiêm", "Nhắc lại"
)

data class MedicationModel(
    val id: String = "",
    val petId: String = "",
    val name: String = "",
    val dosage: String = "",
    val scheduleTime: String = "",
    val quantity: String = "30 viên",
    val status: String = "Đang dùng",
)

data class MedicalRecordEntryModel(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val dateTime: String = "",
    val activityType: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val emotion: String = "Bình thường",
)

data class WeightRecordModel(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val weight: String = "",
    val dateTime: String = "",
    val status: String = "Ổn định",
)

data class CheckupRecordModel(
    val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val title: String = "",
    val doctorOrClinic: String = "Phòng khám thú y PetCare",
    val dateTime: String = "",
    val result: String = "Bình thường",
    val status: String = "Hoàn thành",
)

data class ScheduleModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val petId: String = "1",
    val petName: String = "Momo",
    val dateText: String = "12/10/2026",
    val timeText: String = "18:00",
    val timeOrDateLabel: String = "18:00",
    val isCompleted: Boolean = false,
)

data class PetPlaceModel(
    val id: String = "",
    val name: String = "",
    val category: String = "Thú y", // "Thú y", "Spa", "Cửa hàng"
    val subtitle: String = "",
    val rating: String = "",
    val reviewCount: Int = 0,
    val distance: String = "",
    val address: String = "",
    val phone: String = "",
    val isFeatured: Boolean = false,
    val isSaved: Boolean = false,
    val imageRes: Int = 0,
    val lat: Double = 10.7769,
    val lng: Double = 106.7009,
    val isOpen: Boolean = true,
    val statusText: String = "Open • 5 min • Highway Road",
)



