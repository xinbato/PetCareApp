package com.example.petcare.data.model

data class PetModel(
    val id: String = "",
    val name: String = "",
    val species: String = "Chó",
    val breed: String = "",
    val dateOfBirth: String = "",
    val age: String = "",
    val weight: String = "",
    val gender: String = "Đực",
    val iconRes: Int = com.example.petcare.R.drawable.dog,
    val statusText: String = "Bình thường",
    val statusDetail: String = "",
    val isFavorite: Boolean = false,
    val temperature: String = "",
    val activity: String = "",
    val diet: String = "",
    val lastUpdated: String = "",
) {
    @get:com.google.firebase.firestore.Exclude
    val safeIconRes: Int
        get() = if (iconRes != 0) iconRes else com.example.petcare.R.drawable.dog
}

data class VaccinationModel(
    val id: String = "",
    val petId: String = "",
    val title: String = "",
    val dateTime: String = "",
    val status: String = "Đã tiêm",
)

data class MedicationModel(
    val id: String = "",
    val petId: String = "",
    val name: String = "",
    val dosage: String = "",
    val scheduleTime: String = "",
    val quantity: String = "",
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
    val doctorOrClinic: String = "",
    val dateTime: String = "",
    val result: String = "Bình thường",
    val status: String = "Hoàn thành",
)

data class ScheduleModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val petId: String = "",
    val petName: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val timeOrDateLabel: String = "",
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



