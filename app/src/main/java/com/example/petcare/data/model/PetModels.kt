package com.example.petcare.data.model

import androidx.annotation.Keep
import com.example.petcare.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

// shortcut: keeps numeric iconRes field in Firestore for legacy doc compatibility; migrates to string keys if custom icon packs expand
fun speciesIconRes(species: String): Int = when {
    species.contains("Mèo", ignoreCase = true) || species.equals("CAT", ignoreCase = true) -> R.drawable.cat
    species.contains("Thỏ", ignoreCase = true) || species.equals("RABBIT", ignoreCase = true) -> R.drawable.rabbit
    else -> R.drawable.dog
}

fun resolvePetIconRes(iconRes: Int, species: String = ""): Int = when (iconRes) {
    R.drawable.dog, R.drawable.cat, R.drawable.rabbit -> iconRes
    else -> speciesIconRes(species)
}

@Keep
@IgnoreExtraProperties
data class PetModel(
    @DocumentId val id: String = "",
    val name: String = "",
    val species: String = "Chó",
    val breed: String = "",
    val dateOfBirth: String = "",
    val age: String = "",
    val weight: String = "",
    val gender: String = "Đực",
    val iconRes: Int = R.drawable.dog,
    val statusText: String = "Bình thường",
    val statusDetail: String = "",
    val isFavorite: Boolean = false,
    val temperature: String = "",
    val activity: String = "",
    val diet: String = "",
    val lastUpdated: String = "",
    val userId: String = "",
    val photoUri: String = "",
    val weightKg: Double = 0.0,
    val color: String = "",
    val notes: String = "",
    val createdByAi: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    @get:Exclude
    val safeIconRes: Int
        get() = resolvePetIconRes(iconRes, species)
}

@Keep
@IgnoreExtraProperties
data class VaccinationModel(
    @DocumentId val id: String = "",
    val petId: String = "",
    val title: String = "",
    val dateTime: String = "",
    val status: String = "Đã tiêm",
    val userId: String = "",
    val petName: String = "",
    val scheduledDateMillis: Long = 0L,
    val completed: Boolean = status.equals("Đã tiêm", ignoreCase = true),
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Long = 2L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class MedicationModel(
    @DocumentId val id: String = "",
    val petId: String = "",
    val name: String = "",
    val dosage: String = "",
    val scheduleTime: String = "",
    val quantity: String = "",
    val status: String = "Đang dùng",
    val userId: String = "",
    val petName: String = "",
    val reminderHour: Long = 8L,
    val reminderMinute: Long = 0L,
    val endDateMillis: Long = 0L,
    val lastTakenDateKey: String = "",
    val active: Boolean = !status.equals("Đã hoàn thành", ignoreCase = true) &&
        !status.equals("Tạm dừng", ignoreCase = true),
    val reminderEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class MedicalRecordEntryModel(
    @DocumentId val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val dateTime: String = "",
    val activityType: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val emotion: String = "Bình thường",
    val userId: String = "",
    val recordDateMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class WeightRecordModel(
    @DocumentId val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val weight: String = "",
    val dateTime: String = "",
    val status: String = "Ổn định",
    val userId: String = "",
    val weightKg: Double = 0.0,
    val recordedAtMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CheckupRecordModel(
    @DocumentId val id: String = "",
    val petId: String = "",
    val petName: String = "",
    val title: String = "",
    val doctorOrClinic: String = "",
    val dateTime: String = "",
    val result: String = "Bình thường",
    val status: String = "Hoàn thành",
    val userId: String = "",
    val recordDateMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ScheduleModel(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val petId: String = "",
    val petName: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val timeOrDateLabel: String = "",
    val isCompleted: Boolean = false,
    val userId: String = "",
    val triggerAtMillis: Long = 0L,
    val reminderEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

data class PetPlaceModel(
    val id: String = "",
    val name: String = "",
    val category: String = "Thú y",
    val subtitle: String = "",
    val rating: String = "",
    val reviewCount: Int = 0,
    val distance: String = "",
    val distanceKm: Double = 0.0,
    val address: String = "",
    val phone: String = "",
    val openingHours: String? = null,
    val isFeatured: Boolean = false,
    val isSaved: Boolean = false,
    val imageRes: Int = 0,
    val lat: Double = 10.7769,
    val lng: Double = 106.7009,
    val isOpen: Boolean = true,
    val statusText: String = "",
)

data class SuggestedVaccine(
    val vaccineName: String,
    val daysFromNow: Int,
    val notes: String,
    val selected: Boolean = true,
)
