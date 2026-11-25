package com.example.petcare.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.data.model.SuggestedVaccine
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.model.speciesIconRes
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.utils.DateUtils
import com.example.petcare.utils.NotificationHelper
import com.example.petcare.utils.PetPhotoStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class PetManagementUiState(
    val pets: List<PetModel> = emptyList(),
    val selectedPetId: String = "1",
    val activePet: PetModel? = null,
    val isLoading: Boolean = false,
    val isActionSuccess: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedPet: PetModel?
        get() = pets.find { it.id == selectedPetId } ?: pets.firstOrNull()
}

class PetManagementViewModel(
    private val repository: PetRepository = PetRepository,
) : ViewModel() {

    private val _activePetId = MutableStateFlow<String?>(null)
    private val _isActionSuccess = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val actionMeta = combine(
        _activePetId,
        _isActionSuccess,
        _errorMessage,
        repository.isLoading,
    ) { activeId, isSuccess, error, loading ->
        listOf(activeId, isSuccess, error, loading)
    }

    val uiState: StateFlow<PetManagementUiState> = combine(
        repository.pets,
        repository.currentSelectedPetId,
        actionMeta,
    ) { pets, selectedId, meta ->
        val activeId = meta[0] as String?
        val isSuccess = meta[1] as Boolean
        val error = meta[2] as String?
        val loading = meta[3] as Boolean
        val activePet = if (activeId != null) {
            pets.find { it.id == activeId }
        } else {
            pets.find { it.id == selectedId } ?: pets.firstOrNull()
        }
        PetManagementUiState(
            pets = pets,
            selectedPetId = selectedId,
            activePet = activePet,
            isLoading = loading,
            isActionSuccess = isSuccess,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PetManagementUiState(
            pets = repository.pets.value,
            selectedPetId = repository.currentSelectedPetId.value,
            activePet = repository.getSelectedPet(),
        ),
    )

    fun setActivePet(petId: String) {
        _activePetId.value = petId
    }

    fun selectPet(petId: String) {
        repository.selectPet(petId)
    }

    fun savePet(pet: PetModel) {
        viewModelScope.launch {
            try {
                repository.savePet(pet)
                _isActionSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi lưu thông tin thú cưng"
            }
        }
    }

    fun createAndSavePet(
        name: String,
        species: String = "Chó",
        breed: String = "",
        dateOfBirth: String = "01/01/2024",
        age: String = "1 tuổi",
        weight: String = "5.0 kg",
        gender: String = "Đực",
        iconRes: Int,
        statusText: String = "Khỏe mạnh",
        weightKg: Double = 0.0,
        photoUri: String = "",
        color: String = "",
        notes: String = "",
    ): PetModel {
        val resolvedWeightKg = if (weightKg > 0.0) weightKg else DateUtils.parseWeightKg(weight)
        val newPet = PetModel(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            species = species.trim(),
            breed = breed.trim().ifEmpty { "Chưa rõ giống" },
            dateOfBirth = dateOfBirth.trim(),
            age = age,
            weight = weight.trim().ifEmpty { DateUtils.formatWeight(resolvedWeightKg) },
            weightKg = resolvedWeightKg,
            gender = gender,
            iconRes = iconRes,
            photoUri = photoUri,
            color = color.trim(),
            notes = notes.trim(),
            statusText = statusText,
            isFavorite = false,
        )
        savePet(newPet)
        return newPet
    }

    fun savePetFromAi(
        name: String,
        species: String,
        breed: String,
        gender: String,
        ageText: String,
        weightKg: Double,
        color: String,
        notes: String,
        photoUri: String,
        suggestedVaccines: List<SuggestedVaccine>,
        defaultReminderDays: Int = 3,
    ): PetModel {
        val resolvedName = name.trim().ifEmpty { "Bé cưng mới" }
        val resolvedSpecies = species.trim().ifEmpty { "Chó" }
        val resolvedWeight = weightKg.coerceIn(0.1, 250.0)
        val petId = UUID.randomUUID().toString()
        val newPet = PetModel(
            id = petId,
            name = resolvedName,
            species = resolvedSpecies,
            breed = breed.trim().ifEmpty { "Chưa rõ giống" },
            dateOfBirth = "Chưa rõ",
            age = ageText.trim().ifEmpty { "Chưa rõ" },
            weight = DateUtils.formatWeight(resolvedWeight),
            weightKg = resolvedWeight,
            gender = gender.trim().ifEmpty { "Chưa rõ" },
            iconRes = speciesIconRes(resolvedSpecies),
            photoUri = photoUri,
            color = color.trim(),
            notes = notes.trim(),
            statusText = "Khỏe mạnh",
            isFavorite = false,
            createdByAi = true,
        )
        repository.savePet(newPet)
        suggestedVaccines.forEach { vaccine ->
            val vTitle = vaccine.vaccineName.trim()
            if (vTitle.isNotEmpty()) {
                val scheduledMillis = DateUtils.daysFromNowMillis(vaccine.daysFromNow)
                val dateLabel = DateUtils.formatDate(scheduledMillis)
                val noteText = vaccine.notes.trim()
                repository.addVaccination(
                    VaccinationModel(
                        id = UUID.randomUUID().toString(),
                        petId = petId,
                        petName = resolvedName,
                        title = vTitle,
                        dateTime = dateLabel,
                        status = "Sắp tới",
                        scheduledDateMillis = scheduledMillis,
                        completed = false,
                        reminderEnabled = true,
                        reminderDaysBefore = defaultReminderDays.coerceIn(0, 30).toLong(),
                        notes = noteText,
                    )
                )
                val scheduleTriggerMillis = NotificationHelper.parseScheduleMillis(dateLabel, "09:00")
                    ?: scheduledMillis
                repository.addSchedule(
                    ScheduleModel(
                        id = UUID.randomUUID().toString(),
                        title = "Tiêm phòng: $vTitle".take(100),
                        description = noteText.ifEmpty { "Lịch tiêm đề xuất cho $resolvedName" }.take(300),
                        petId = petId,
                        petName = resolvedName,
                        dateText = dateLabel,
                        timeText = "09:00",
                        timeOrDateLabel = "$dateLabel • 09:00",
                        isCompleted = false,
                        triggerAtMillis = scheduleTriggerMillis,
                        reminderEnabled = true,
                    )
                )
            }
        }
        _isActionSuccess.value = true
        return newPet
    }

    fun deletePet(petId: String, context: Context? = null) {
        viewModelScope.launch {
            try {
                val existingPhoto = repository.getPetById(petId)?.photoUri.orEmpty()
                if (context != null && existingPhoto.isNotBlank()) {
                    PetPhotoStorage.deleteOwnedPetPhoto(context, existingPhoto)
                }
                repository.deletePet(petId)
                _isActionSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi xóa thú cưng"
            }
        }
    }

    fun toggleFavorite(petId: String) {
        repository.toggleFavorite(petId)
    }

    fun getPetById(id: String): PetModel? = repository.getPetById(id)

    fun getSelectedPet(): PetModel = repository.getSelectedPet()

    fun clearActionState() {
        _isActionSuccess.value = false
        _errorMessage.value = null
    }
}
