package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val userName: String = "",
    val pets: List<PetModel> = emptyList(),
    val selectedPetId: String = "",
    val schedules: List<ScheduleModel> = emptyList(),
    val isLoading: Boolean = false,
) {
    val selectedPet: PetModel?
        get() = pets.find { it.id == selectedPetId } ?: pets.firstOrNull()
}

class HomeViewModel(
    private val repository: PetRepository = PetRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.userProfile,
        repository.pets,
        repository.currentSelectedPetId,
        repository.schedules,
    ) { userProfile, pets, selectedPetId, schedules ->
        HomeUiState(
            userName = userProfile.name,
            pets = pets,
            selectedPetId = selectedPetId.ifEmpty { pets.firstOrNull()?.id ?: "1" },
            schedules = schedules,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            userName = repository.userProfile.value.name,
            pets = repository.pets.value,
            selectedPetId = repository.currentSelectedPetId.value,
            schedules = repository.schedules.value,
            isLoading = false,
        ),
    )

    fun selectPet(petId: String) {
        repository.selectPet(petId)
    }

    fun toggleScheduleComplete(scheduleId: String) {
        val schedule = repository.getScheduleById(scheduleId) ?: return
        repository.updateSchedule(schedule.copy(isCompleted = !schedule.isCompleted))
    }
}
