package com.example.petcare.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.petcare.data.model.AppSettings
import com.example.petcare.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "petcare_settings")

class SettingsRepository(private val context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val VACCINE_REMINDER_ENABLED = booleanPreferencesKey("vaccine_reminder_enabled")
        val MEDICATION_REMINDER_ENABLED = booleanPreferencesKey("medication_reminder_enabled")
        val FEEDING_REMINDER_ENABLED = booleanPreferencesKey("feeding_reminder_enabled")
        val NEWS_NOTIFICATION_ENABLED = booleanPreferencesKey("news_notification_enabled")
        val DEFAULT_VACCINE_REMINDER_DAYS = intPreferencesKey("default_vaccine_reminder_days")
        val USE_POUNDS = booleanPreferencesKey("use_pounds")
        val DEFAULT_SEARCH_RADIUS_METERS = intPreferencesKey("default_search_radius_meters")
    }

    val settingsFlow: Flow<AppSettings> = appContext.settingsDataStore.data.map { prefs ->
        val themeMode = runCatching {
            ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)

        AppSettings(
            themeMode = themeMode,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            vaccineReminderEnabled = prefs[Keys.VACCINE_REMINDER_ENABLED] ?: true,
            medicationReminderEnabled = prefs[Keys.MEDICATION_REMINDER_ENABLED] ?: true,
            feedingReminderEnabled = prefs[Keys.FEEDING_REMINDER_ENABLED] ?: true,
            newsNotificationEnabled = prefs[Keys.NEWS_NOTIFICATION_ENABLED] ?: false,
            defaultVaccineReminderDays = (prefs[Keys.DEFAULT_VACCINE_REMINDER_DAYS] ?: 3).coerceIn(1, 30),
            usePounds = prefs[Keys.USE_POUNDS] ?: false,
            defaultSearchRadiusMeters = (prefs[Keys.DEFAULT_SEARCH_RADIUS_METERS] ?: 5000).coerceIn(1000, 20000)
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        appContext.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setVaccineReminderEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.VACCINE_REMINDER_ENABLED] = enabled }
    }

    suspend fun setMedicationReminderEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.MEDICATION_REMINDER_ENABLED] = enabled }
    }

    suspend fun setFeedingReminderEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.FEEDING_REMINDER_ENABLED] = enabled }
    }

    suspend fun setNewsNotificationEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.NEWS_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setDefaultVaccineReminderDays(days: Int) {
        appContext.settingsDataStore.edit { it[Keys.DEFAULT_VACCINE_REMINDER_DAYS] = days.coerceIn(1, 30) }
    }

    suspend fun setUsePounds(usePounds: Boolean) {
        appContext.settingsDataStore.edit { it[Keys.USE_POUNDS] = usePounds }
    }

    suspend fun setDefaultSearchRadiusMeters(radiusMeters: Int) {
        appContext.settingsDataStore.edit { it[Keys.DEFAULT_SEARCH_RADIUS_METERS] = radiusMeters.coerceIn(1000, 20000) }
    }
}
