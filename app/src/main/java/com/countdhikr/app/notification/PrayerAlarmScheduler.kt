package com.countdhikr.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.countdhikr.app.data.model.AppSettings
import com.countdhikr.app.data.model.PrayerTimes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerAlarmScheduler {
    private const val TAG = "PrayerAlarmScheduler"
    
    const val EXTRA_ALARM_TYPE = "alarm_type"
    const val EXTRA_PRAYER_NAME = "prayer_name"
    const val EXTRA_PRAYER_TIME = "prayer_time"
    
    const val TYPE_AZAN = "azan"
    const val TYPE_SALAT_REMINDER = "salat_reminder"
    const val TYPE_DHIKR_REMINDER = "dhikr_reminder"
    
    // Request code base offsets to avoid collisions
    private const val RC_AZAN_BASE = 1000
    private const val RC_SALAT_BASE = 2000
    private const val RC_DHIKR_BASE = 3000

    private val PRAYER_NAMES = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

    fun scheduleAlarms(context: Context, settings: AppSettings) {
        val prayerTimes = settings.cachedPrayerTimes ?: run {
            Log.w(TAG, "No cached prayer times found. Cannot schedule alarms.")
            return
        }
        
        // If master notification toggle is OFF, cancel all scheduled alarms
        if (!settings.reminderNotification) {
            Log.i(TAG, "Master alert toggle (reminderNotification) is OFF. Cancelling all alarms.")
            cancelAllAlarms(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        PRAYER_NAMES.forEachIndexed { index, name ->
            val timeStr = getPrayerTimeStr(prayerTimes, name) ?: return@forEachIndexed
            val parsedTime = parsePrayerTime(timeStr) ?: return@forEachIndexed

            // 1. Azan Playback / Notification Alarm
            if (settings.azanEnabled) {
                scheduleExactAlarm(
                    context,
                    alarmManager,
                    parsedTime.timeInMillis,
                    RC_AZAN_BASE + index,
                    createIntent(context, TYPE_AZAN, name, timeStr)
                )
            } else {
                cancelAlarm(context, RC_AZAN_BASE + index, createIntent(context, TYPE_AZAN, name, timeStr))
            }

            // 2. Salat Reminder Alarm (10 min after, 2 min for Maghrib)
            if (settings.salatReminderEnabled) {
                val delayMin = if (name.equals("Maghrib", ignoreCase = true)) 2 else 10
                val salatCal = (parsedTime.clone() as Calendar).apply {
                    add(Calendar.MINUTE, delayMin)
                }
                
                // If it's already in the past today, we don't schedule it
                if (salatCal.timeInMillis > System.currentTimeMillis()) {
                    scheduleExactAlarm(
                        context,
                        alarmManager,
                        salatCal.timeInMillis,
                        RC_SALAT_BASE + index,
                        createIntent(context, TYPE_SALAT_REMINDER, name, timeStr)
                    )
                }
            } else {
                cancelAlarm(context, RC_SALAT_BASE + index, createIntent(context, TYPE_SALAT_REMINDER, name, timeStr))
            }

            // 3. Dhikr Reminder Alarm (5 min after prayer)
            if (settings.dhikrReminderEnabled) {
                val dhikrCal = (parsedTime.clone() as Calendar).apply {
                    add(Calendar.MINUTE, 5)
                }
                if (dhikrCal.timeInMillis > System.currentTimeMillis()) {
                    scheduleExactAlarm(
                        context,
                        alarmManager,
                        dhikrCal.timeInMillis,
                        RC_DHIKR_BASE + index,
                        createIntent(context, TYPE_DHIKR_REMINDER, name, timeStr)
                    )
                }
            } else {
                cancelAlarm(context, RC_DHIKR_BASE + index, createIntent(context, TYPE_DHIKR_REMINDER, name, timeStr))
            }
        }
        
        Log.i(TAG, "Completed scheduling all enabled alarms.")
    }

    private fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        requestCode: Int,
        intent: Intent
    ) {
        // Skip past times
        if (triggerAtMillis <= System.currentTimeMillis()) {
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm code $requestCode for ${Date(triggerAtMillis)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm: SecurityException", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(context: Context, requestCode: Int, intent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm code $requestCode")
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        PRAYER_NAMES.forEachIndexed { index, name ->
            cancelAlarm(context, RC_AZAN_BASE + index, createIntent(context, TYPE_AZAN, name, ""))
            cancelAlarm(context, RC_SALAT_BASE + index, createIntent(context, TYPE_SALAT_REMINDER, name, ""))
            cancelAlarm(context, RC_DHIKR_BASE + index, createIntent(context, TYPE_DHIKR_REMINDER, name, ""))
        }
        Log.i(TAG, "Cancelled all possible scheduled alarms.")
    }

    private fun createIntent(context: Context, type: String, prayerName: String, timeStr: String): Intent {
        return Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.countdhikr.app.ACTION_PRAYER_ALARM"
            putExtra(EXTRA_ALARM_TYPE, type)
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_TIME, timeStr)
        }
    }

    private fun getPrayerTimeStr(times: PrayerTimes, name: String): String? {
        return when (name.lowercase()) {
            "fajr" -> times.fajr
            "dhuhr" -> times.dhuhr
            "asr" -> times.asr
            "maghrib" -> times.maghrib
            "isha" -> times.isha
            else -> null
        }
    }

    private fun parsePrayerTime(timeStr: String): Calendar? {
        try {
            // Clean the time string from things like (GST) or (EEST)
            val cleanTime = timeStr.substringBefore(" ").trim()
            val parts = cleanTime.split(":")
            if (parts.size != 2) return null
            
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null

            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If the scheduled time is already in the past, schedule it for the next day!
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing prayer time: $timeStr", e)
            return null
        }
    }
}
