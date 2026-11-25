package com.example.petcare.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val viLocale = Locale.forLanguageTag("vi-VN")

    private val keyFormatter = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    private val displayFormatter = ThreadLocal.withInitial {
        SimpleDateFormat("dd/MM/yyyy", viLocale)
    }

    fun todayKey(): String = keyFormatter.get()!!.format(Date())

    fun formatDate(millis: Long): String {
        if (millis <= 0L) return "Chưa đặt"
        return displayFormatter.get()!!.format(Date(millis))
    }

    fun formatTime(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))

    fun startOfTodayMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun daysFromNowMillis(days: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, days)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun parseDateMillis(dateText: String): Long? = runCatching {
        val trimmed = dateText.trim()
        if (trimmed.equals("Hôm nay", ignoreCase = true)) return startOfTodayMillis()
        if (trimmed.equals("Ngày mai", ignoreCase = true)) return daysFromNowMillis(1)
        if (trimmed.equals("Tuần sau", ignoreCase = true)) return daysFromNowMillis(7)
        if (trimmed.equals("Tháng sau", ignoreCase = true)) return daysFromNowMillis(30)
        val parts = trimmed.split("/")
        if (parts.size != 3) return null
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.YEAR, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }.getOrNull()

    fun parseWeightKg(rawWeight: String): Double {
        val match = Regex("([0-9]+([.,][0-9]+)?)").find(rawWeight) ?: return 0.0
        val value = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return 0.0
        val isLbs = rawWeight.contains("lb", ignoreCase = true)
        return (if (isLbs) value / 2.20462 else value).coerceIn(0.0, 250.0)
    }

    fun formatWeight(weightKg: Double): String {
        if (weightKg <= 0.0) return "Chưa có"
        return String.format(Locale.US, "%.1f kg", weightKg)
    }

    fun relativeDayLabel(targetMillis: Long): String {
        if (targetMillis <= 0L) return ""
        val startToday = startOfTodayMillis()
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = targetMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays = TimeUnit.MILLISECONDS.toDays(targetCal.timeInMillis - startToday).toInt()
        return when {
            diffDays < 0 -> "Quá hạn ${-diffDays} ngày"
            diffDays == 0 -> "Hôm nay"
            diffDays == 1 -> "Ngày mai"
            diffDays <= 7 -> "Còn $diffDays ngày"
            else -> formatDate(targetMillis)
        }
    }
}
