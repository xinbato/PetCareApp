package com.example.petcare.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.petcare.MainActivity
import com.example.petcare.R
import com.example.petcare.data.model.AppSettings
import com.example.petcare.data.model.MedicationModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.data.model.VaccinationModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class PetAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(NotificationHelper.EXTRA_IS_MEDICATION, false)) {
            NotificationHelper.rescheduleMedicationAfterTrigger(context, intent)
        }
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Nhắc nhở thú cưng"
        val message = intent.getStringExtra(NotificationHelper.EXTRA_MESSAGE)
            ?: "Đã đến lịch chăm sóc bé yêu của bạn!"
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_ID, 1001)
        NotificationHelper.showNotification(context, notificationId, title, message)
    }
}

class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            NotificationHelper.restoreAllFromCache(context)
        }
    }
}

object NotificationHelper {
    const val CHANNEL_ID = "pet_care_reminders"
    private const val CHANNEL_NAME = "Nhắc nhở lịch thú cưng"
    private const val TAG = "NotificationHelper"
    private const val PREFS_NAME = "petcare_active_reminders"
    private const val KEY_SPECS = "specs"
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    private const val IMMEDIATE_DELAY_MILLIS = 60_000L

    internal const val EXTRA_TITLE = "EXTRA_TITLE"
    internal const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    internal const val EXTRA_ID = "EXTRA_ID"
    internal const val EXTRA_IS_MEDICATION = "EXTRA_IS_MEDICATION"
    internal const val EXTRA_MED_HOUR = "EXTRA_MED_HOUR"
    internal const val EXTRA_MED_MINUTE = "EXTRA_MED_MINUTE"
    internal const val EXTRA_MED_END_MILLIS = "EXTRA_MED_END_MILLIS"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Kênh thông báo nhắc lịch chăm sóc, tiêm phòng và uống thuốc cho thú cưng"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun showNotification(context: Context, id: Int, title: String, message: String) {
        createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    fun syncReminders(
        context: Context,
        vaccinations: List<VaccinationModel>,
        medications: List<MedicationModel>,
        schedules: List<ScheduleModel>,
        settings: AppSettings,
    ) {
        val appContext = context.applicationContext
        createNotificationChannel(appContext)
        val previousCodes = loadCachedSpecs(appContext).map { it.requestCode }.toSet()

        if (!settings.notificationsEnabled) {
            previousCodes.forEach { cancelAlarmByCode(appContext, it) }
            saveCachedSpecs(appContext, emptyList())
            return
        }

        val now = System.currentTimeMillis()
        val activeSpecs = buildList {
            if (settings.vaccineReminderEnabled) {
                vaccinations.forEach { vac ->
                    val schedMillis = vac.scheduledDateMillis.takeIf { it > 0L }
                        ?: DateUtils.parseDateMillis(vac.dateTime)
                        ?: 0L
                    if (!vac.completed && vac.reminderEnabled && schedMillis > 0L) {
                        val daysBefore = vac.reminderDaysBefore.takeIf { it > 0L }
                            ?: settings.defaultVaccineReminderDays.toLong()
                        val triggerAt = computeVaccinationTriggerMillis(schedMillis, daysBefore, now)
                        if (triggerAt != null) {
                            val petLabel = vac.petName.ifBlank { "thú cưng" }
                            add(
                                ReminderSpec(
                                    requestCode = "vac_${vac.id}".hashCode(),
                                    triggerAtMillis = triggerAt,
                                    title = "Nhắc tiêm phòng cho $petLabel",
                                    body = "Lịch tiêm ${vac.title} (${vac.dateTime}). Chuẩn bị đưa bé đi tiêm nhé!",
                                    isMedication = false,
                                    scheduledDateMillis = schedMillis,
                                )
                            )
                        }
                    }
                }
            }

            if (settings.medicationReminderEnabled) {
                medications.forEach { med ->
                    if (med.active && med.reminderEnabled) {
                        val parsedTime = parseHourMinute(med.scheduleTime)
                        val hour = (parsedTime?.first ?: med.reminderHour.toInt()).coerceIn(0, 23)
                        val minute = (parsedTime?.second ?: med.reminderMinute.toInt()).coerceIn(0, 59)
                        val triggerAt = computeNextDailyTriggerMillis(hour, minute, med.endDateMillis, now)
                        if (triggerAt != null) {
                            val petLabel = med.petName.ifBlank { "thú cưng" }
                            add(
                                ReminderSpec(
                                    requestCode = "med_${med.id}".hashCode(),
                                    triggerAtMillis = triggerAt,
                                    title = "Giờ uống thuốc của $petLabel",
                                    body = "${med.name} • Liều: ${med.dosage} (${med.scheduleTime})",
                                    isMedication = true,
                                    hour = hour,
                                    minute = minute,
                                    endDateMillis = med.endDateMillis,
                                )
                            )
                        }
                    }
                }
            }

            if (settings.feedingReminderEnabled) {
                schedules.forEach { sch ->
                    val triggerAt = sch.triggerAtMillis.takeIf { it > 0L }
                        ?: parseScheduleMillis(sch.dateText, sch.timeText)
                        ?: 0L
                    if (!sch.isCompleted && sch.reminderEnabled && triggerAt > now) {
                        val petLabel = if (sch.petName.isNotBlank()) "bé ${sch.petName}" else "thú cưng"
                        add(
                            ReminderSpec(
                                requestCode = sch.id.hashCode(),
                                triggerAtMillis = triggerAt,
                                title = "Lịch hẹn: ${sch.title}",
                                body = "Đến lịch ${sch.title} cho $petLabel lúc ${sch.timeText} (${sch.dateText})",
                                isMedication = false,
                                scheduledDateMillis = triggerAt,
                            )
                        )
                    }
                }
            }
        }

        val activeCodes = activeSpecs.map { it.requestCode }.toSet()
        previousCodes.filterNot { it in activeCodes }.forEach { cancelAlarmByCode(appContext, it) }
        activeSpecs.forEach { scheduleSpec(appContext, it) }
        saveCachedSpecs(appContext, activeSpecs)
    }

    fun rescheduleMedicationAfterTrigger(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val requestCode = intent.getIntExtra(EXTRA_ID, -1)
        if (requestCode == -1) return
        val hour = intent.getIntExtra(EXTRA_MED_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_MED_MINUTE, -1)
        if (hour !in 0..23 || minute !in 0..59) return
        val endDateMillis = intent.getLongExtra(EXTRA_MED_END_MILLIS, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_MESSAGE) ?: return

        val nextTrigger = computeNextDailyTriggerMillis(
            hour = hour,
            minute = minute,
            endDateMillis = endDateMillis,
            nowMillis = System.currentTimeMillis() + 1_000L
        )
        val specs = loadCachedSpecs(appContext).toMutableList()
        specs.removeAll { it.requestCode == requestCode }
        if (nextTrigger != null) {
            val nextSpec = ReminderSpec(
                requestCode = requestCode,
                triggerAtMillis = nextTrigger,
                title = title,
                body = body,
                isMedication = true,
                hour = hour,
                minute = minute,
                endDateMillis = endDateMillis,
            )
            scheduleSpec(appContext, nextSpec)
            specs.add(nextSpec)
        } else {
            cancelAlarmByCode(appContext, requestCode)
        }
        saveCachedSpecs(appContext, specs)
    }

    fun restoreAllFromCache(context: Context) {
        val appContext = context.applicationContext
        createNotificationChannel(appContext)
        val now = System.currentTimeMillis()
        val refreshed = loadCachedSpecs(appContext).mapNotNull { spec ->
            if (spec.isMedication) {
                val nextTrigger = computeNextDailyTriggerMillis(
                    hour = spec.hour,
                    minute = spec.minute,
                    endDateMillis = spec.endDateMillis,
                    nowMillis = now
                ) ?: return@mapNotNull null
                spec.copy(triggerAtMillis = nextTrigger)
            } else {
                when {
                    spec.triggerAtMillis > now -> spec
                    spec.scheduledDateMillis > now -> spec.copy(triggerAtMillis = now + IMMEDIATE_DELAY_MILLIS)
                    else -> null
                }
            }
        }
        refreshed.forEach { scheduleSpec(appContext, it) }
        saveCachedSpecs(appContext, refreshed)
    }

    fun clearAllReminders(context: Context) {
        val appContext = context.applicationContext
        val specs = loadCachedSpecs(appContext)
        if (specs.isEmpty()) return
        specs.forEach { cancelAlarmByCode(appContext, it.requestCode) }
        saveCachedSpecs(appContext, emptyList())
    }

    fun parseScheduleMillis(dateText: String, timeText: String): Long? = runCatching {
        val dateParts = dateText.trim().split("/")
        val timeParts = timeText.trim().split(":")
        if (dateParts.size == 3) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateParts[2].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                set(Calendar.HOUR_OF_DAY, if (timeParts.size >= 2) timeParts[0].toInt() else 8)
                set(Calendar.MINUTE, if (timeParts.size >= 2) timeParts[1].toInt() else 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } else null
    }.getOrNull()

    fun scheduleAlarm(
        context: Context,
        scheduleId: String,
        title: String,
        message: String,
        triggerAtMillis: Long,
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val spec = ReminderSpec(
            requestCode = scheduleId.hashCode(),
            triggerAtMillis = triggerAtMillis,
            title = title,
            body = message,
            isMedication = false,
            scheduledDateMillis = triggerAtMillis,
        )
        scheduleSpec(context.applicationContext, spec)
        val specs = loadCachedSpecs(context.applicationContext).toMutableList()
        specs.removeAll { it.requestCode == spec.requestCode }
        specs.add(spec)
        saveCachedSpecs(context.applicationContext, specs)
    }

    fun cancelAlarm(context: Context, scheduleId: String) {
        val appContext = context.applicationContext
        val code = scheduleId.hashCode()
        cancelAlarmByCode(appContext, code)
        val specs = loadCachedSpecs(appContext).filterNot { it.requestCode == code }
        saveCachedSpecs(appContext, specs)
    }

    private fun scheduleSpec(context: Context, spec: ReminderSpec) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PetAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, spec.title)
            putExtra(EXTRA_MESSAGE, spec.body)
            putExtra(EXTRA_ID, spec.requestCode)
            putExtra(EXTRA_IS_MEDICATION, spec.isMedication)
            if (spec.isMedication) {
                putExtra(EXTRA_MED_HOUR, spec.hour)
                putExtra(EXTRA_MED_MINUTE, spec.minute)
                putExtra(EXTRA_MED_END_MILLIS, spec.endDateMillis)
            }
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            spec.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, spec.triggerAtMillis, pendingIntent)
            Log.d(TAG, "[INFO] Scheduled alarm code=${spec.requestCode} at=${spec.triggerAtMillis}")
        } catch (e: SecurityException) {
            Log.w(TAG, "[WARN] Unable to schedule alarm: ${e.message}")
        }
    }

    private fun cancelAlarmByCode(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PetAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun loadCachedSpecs(context: Context): List<ReminderSpec> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SPECS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        ReminderSpec(
                            requestCode = obj.getInt("code"),
                            triggerAtMillis = obj.getLong("triggerAt"),
                            title = obj.getString("title"),
                            body = obj.getString("body"),
                            isMedication = obj.optBoolean("isMed", false),
                            hour = obj.optInt("hour", 8),
                            minute = obj.optInt("minute", 0),
                            endDateMillis = obj.optLong("endMillis", 0L),
                            scheduledDateMillis = obj.optLong("schedMillis", 0L),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveCachedSpecs(context: Context, specs: List<ReminderSpec>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (specs.isEmpty()) {
            prefs.edit { remove(KEY_SPECS) }
            return
        }
        val arr = JSONArray()
        specs.forEach { spec ->
            arr.put(
                JSONObject().apply {
                    put("code", spec.requestCode)
                    put("triggerAt", spec.triggerAtMillis)
                    put("title", spec.title)
                    put("body", spec.body)
                    put("isMed", spec.isMedication)
                    put("hour", spec.hour)
                    put("minute", spec.minute)
                    put("endMillis", spec.endDateMillis)
                    put("schedMillis", spec.scheduledDateMillis)
                }
            )
        }
        prefs.edit { putString(KEY_SPECS, arr.toString()) }
    }

    internal fun parseHourMinute(scheduleTime: String): Pair<Int, Int>? {
        val match = Regex("""(\d{1,2}):(\d{2})""").find(scheduleTime) ?: return null
        val hour = match.groupValues[1].toIntOrNull()?.takeIf { it in 0..23 } ?: return null
        val minute = match.groupValues[2].toIntOrNull()?.takeIf { it in 0..59 } ?: return null
        return hour to minute
    }

    internal fun computeVaccinationTriggerMillis(
        scheduledDateMillis: Long,
        reminderDaysBefore: Long,
        nowMillis: Long,
    ): Long? {
        if (scheduledDateMillis <= 0L || scheduledDateMillis <= nowMillis) return null
        val offsetMillis = reminderDaysBefore.coerceAtLeast(0L) * DAY_MILLIS
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = (scheduledDateMillis - offsetMillis).coerceAtLeast(nowMillis)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val triggerAt = targetCal.timeInMillis
        return if (triggerAt <= nowMillis) nowMillis + IMMEDIATE_DELAY_MILLIS else triggerAt
    }

    internal fun computeNextDailyTriggerMillis(
        hour: Int,
        minute: Int,
        endDateMillis: Long,
        nowMillis: Long,
    ): Long? {
        if (endDateMillis > 0L && nowMillis > endDateMillis) return null
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val triggerAt = cal.timeInMillis
        return if (endDateMillis > 0L && triggerAt > endDateMillis) null else triggerAt
    }

    private data class ReminderSpec(
        val requestCode: Int,
        val triggerAtMillis: Long,
        val title: String,
        val body: String,
        val isMedication: Boolean,
        val hour: Int = 8,
        val minute: Int = 0,
        val endDateMillis: Long = 0L,
        val scheduledDateMillis: Long = 0L,
    )
}
