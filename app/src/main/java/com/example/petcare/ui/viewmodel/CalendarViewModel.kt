package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarUiState(
    val schedules: List<ScheduleModel> = emptyList(),
    val todaySchedules: List<ScheduleModel> = emptyList(),
    val upcomingSchedules: List<ScheduleModel> = emptyList(),
    val selectedSchedule: ScheduleModel? = null,
    val selectedPetId: String = "1",
    val pets: List<PetModel> = emptyList(),
    val isLoading: Boolean = false,
    val isActionSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class CalendarViewModel(
    private val repository: PetRepository = PetRepository,
) : ViewModel() {

    private val _selectedScheduleId = MutableStateFlow<String?>(null)
    private val _isActionSuccess = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.schedules,
        repository.pets,
        repository.currentSelectedPetId,
        _selectedScheduleId,
        _isActionSuccess,
    ) { schedules, pets, selectedPetId, selectedScheduleId, isSuccess ->
        val selectedSchedule = schedules.find { it.id == selectedScheduleId }
        val today = schedules.filter {
            it.timeOrDateLabel.contains("hôm nay", ignoreCase = true) ||
                it.timeOrDateLabel.matches(Regex("\\d{1,2}:\\d{2}"))
        }
        val upcoming = schedules.filterNot { today.contains(it) }

        CalendarUiState(
            schedules = schedules,
            todaySchedules = today,
            upcomingSchedules = upcoming,
            selectedSchedule = selectedSchedule,
            selectedPetId = selectedPetId,
            pets = pets,
            isLoading = false,
            isActionSuccess = isSuccess,
            errorMessage = _errorMessage.value,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(
            schedules = repository.schedules.value,
            todaySchedules = repository.schedules.value,
            selectedPetId = repository.currentSelectedPetId.value,
            pets = repository.pets.value,
        ),
    )

    fun selectSchedule(scheduleId: String?) {
        _selectedScheduleId.value = scheduleId
    }

    fun toggleScheduleComplete(scheduleId: String) {
        val schedule = repository.getScheduleById(scheduleId) ?: return
        repository.updateSchedule(schedule.copy(isCompleted = !schedule.isCompleted))
    }

    fun addSchedule(
        title: String,
        description: String = "",
        petId: String = "1",
        petName: String = "",
        dateText: String = "12/10/2026",
        timeText: String = "18:00",
        timeOrDateLabel: String = "18:00",
    ) {
        val resolvedPetName = if (petName.isBlank()) {
            repository.getPetById(petId)?.name ?: "Momo"
        } else {
            petName
        }
        val schedule = ScheduleModel(
            id = "s_${System.currentTimeMillis()}",
            title = title.trim(),
            description = description.trim(),
            petId = petId,
            petName = resolvedPetName,
            dateText = dateText.trim(),
            timeText = timeText.trim(),
            timeOrDateLabel = timeOrDateLabel.trim(),
            isCompleted = false,
        )
        viewModelScope.launch {
            repository.addSchedule(schedule)
            _isActionSuccess.value = true
        }
    }

    fun addSchedule(schedule: ScheduleModel) {
        viewModelScope.launch {
            repository.addSchedule(schedule)
            _isActionSuccess.value = true
        }
    }

    fun updateSchedule(schedule: ScheduleModel) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
            _isActionSuccess.value = true
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            repository.deleteSchedule(scheduleId)
            _isActionSuccess.value = true
        }
    }

    fun getScheduleById(id: String): ScheduleModel? = repository.getScheduleById(id)

    fun clearActionState() {
        _isActionSuccess.value = false
        _errorMessage.value = null
    }
}
