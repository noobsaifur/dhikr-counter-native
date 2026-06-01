package com.countdhikr.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.countdhikr.app.data.local.AppDataStore
import com.countdhikr.app.data.remote.RetrofitClient
import com.countdhikr.app.data.repository.DhikrRepository
import com.countdhikr.app.data.repository.PrayerRepository

class CountDhikrApp : Application() {
    lateinit var dataStore: AppDataStore
    lateinit var dhikrRepository: DhikrRepository
    lateinit var prayerRepository: PrayerRepository
    
    override fun onCreate() {
        super.onCreate()
        dataStore = AppDataStore(applicationContext)
        dhikrRepository = DhikrRepository(dataStore)
        prayerRepository = PrayerRepository(
            RetrofitClient.aladhanApi,
            RetrofitClient.geocodingApi
        )
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Dhikr channel
            val dhikrChannel = NotificationChannel(
                "dhikr_reminders",
                "Dhikr Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to perform Dhikr"
            }

            // Prayer channel
            val prayerChannel = NotificationChannel(
                "prayer_reminders",
                "Prayer Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for prayer times"
            }

            // Azan channel (foreground service audio notification)
            val azanChannel = NotificationChannel(
                "azan_channel",
                "Azan Playback",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "System notifications for ongoing Azan playback"
            }

            notificationManager.createNotificationChannel(dhikrChannel)
            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(azanChannel)
        }
    }
}
