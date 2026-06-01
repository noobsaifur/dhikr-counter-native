package com.countdhikr.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.countdhikr.app.MainActivity
import com.countdhikr.app.data.local.AppDataStore
import com.countdhikr.app.data.model.DailyDhikrStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerAlarmReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.countdhikr.app.ACTION_PRAYER_ALARM") return

        val pendingResult = goAsync()
        val type = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_ALARM_TYPE)
        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
        val prayerTime = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TIME) ?: ""

        Log.i("PrayerAlarmReceiver", "Received alarm: Type=$type, Prayer=$prayerName, Time=$prayerTime")

        scope.launch {
            try {
                when (type) {
                    PrayerAlarmScheduler.TYPE_AZAN -> {
                        handleAzanAlarm(context, prayerName)
                    }
                    PrayerAlarmScheduler.TYPE_SALAT_REMINDER -> {
                        showSalatReminderNotification(context, prayerName)
                    }
                    PrayerAlarmScheduler.TYPE_DHIKR_REMINDER -> {
                        handleDhikrReminderAlarm(context, prayerName)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAzanAlarm(context: Context, prayerName: String) {
        val dataStore = AppDataStore(context.applicationContext)
        val appState = dataStore.appState.first()
        val settings = appState.settings

        if (settings.azanSound) {
            // Start Foreground Service to play Azan Audio
            val serviceIntent = Intent(context, AzanPlaybackService::class.java).apply {
                putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            // Just show high-priority silent notification
            showPrayerTimeNotification(context, prayerName)
        }
    }

    private fun showPrayerTimeNotification(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "prayer_reminders")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System fallback alarm icon
            .setContentTitle("Salat Time: $prayerName 🕌")
            .setContentText("It is time for $prayerName prayer. Connect with Allah.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun showSalatReminderNotification(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "prayer_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Salat Reminder: $prayerName 🕌")
            .setContentText("Did you pray $prayerName? Take a moment for prayer.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }

    private suspend fun handleDhikrReminderAlarm(context: Context, prayerName: String) {
        val dataStore = AppDataStore(context.applicationContext)
        val appState = dataStore.appState.first()
        
        // Check if today's dhikr status is already completed
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val todayDhikr = appState.dailyDhikrs.find { it.date == todayDate }
        
        if (todayDhikr == null || todayDhikr.status != DailyDhikrStatus.COMPLETED) {
            // User hasn't finished today's dhikr routine, show reminder
            showDhikrReminderNotification(context, prayerName)
        } else {
            Log.d("PrayerAlarmReceiver", "Dhikr is completed for today. Skipping dhikr alarm.")
        }
    }

    private fun showDhikrReminderNotification(context: Context, prayerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            103,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "dhikr_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Dhikr Reminder after $prayerName 📿")
            .setContentText("Take a moment to do your daily Dhikr.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1003, notification)
    }
}
