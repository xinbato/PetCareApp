package com.example.petcare.data.model

import java.util.Locale

enum class ThemeMode(val displayName: String, val subtitle: String) {
    SYSTEM("Theo hệ thống", "Tự động theo cài đặt thiết bị"),
    LIGHT("Sáng", "Giao diện sáng nhẹ nhàng"),
    DARK("Tối", "Giao diện nền tối dễ nhìn")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val vaccineReminderEnabled: Boolean = true,
    val medicationReminderEnabled: Boolean = true,
    val feedingReminderEnabled: Boolean = true,
    val newsNotificationEnabled: Boolean = false,
    val defaultVaccineReminderDays: Int = 3,
    val usePounds: Boolean = false,
    val defaultSearchRadiusMeters: Int = 5000
) {
    val weightUnitLabel: String get() = if (usePounds) "lb" else "kg"

    fun toDisplayWeight(weightKg: Double): Double = if (usePounds) weightKg * KG_TO_LB else weightKg

    fun toKg(displayWeight: Double): Double = if (usePounds) displayWeight / KG_TO_LB else displayWeight

    fun formatWeight(weightKg: Double): String {
        if (weightKg <= 0.0) return "Chưa có"
        return String.format(Locale.US, "%.1f %s", toDisplayWeight(weightKg), weightUnitLabel)
    }

    private companion object {
        const val KG_TO_LB = 2.20462
    }
}
