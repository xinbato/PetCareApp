package com.example.petcare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.AppSettings
import com.example.petcare.data.model.ThemeMode
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AppSettings()
        )

    val pets = PetRepository.pets
    val vaccinations = PetRepository.vaccinations

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }

    fun setVaccineReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVaccineReminderEnabled(enabled) }
    }

    fun setMedicationReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMedicationReminderEnabled(enabled) }
    }

    fun setFeedingReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setFeedingReminderEnabled(enabled) }
    }

    fun setNewsNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNewsNotificationEnabled(enabled) }
    }

    fun setDefaultVaccineReminderDays(days: Int) {
        viewModelScope.launch { settingsRepository.setDefaultVaccineReminderDays(days) }
    }

    fun setUsePounds(usePounds: Boolean) {
        viewModelScope.launch { settingsRepository.setUsePounds(usePounds) }
    }

    fun setDefaultSearchRadiusMeters(radiusMeters: Int) {
        viewModelScope.launch { settingsRepository.setDefaultSearchRadiusMeters(radiusMeters) }
    }
}
