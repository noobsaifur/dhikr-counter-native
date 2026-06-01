package com.countdhikr.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.countdhikr.app.MainActivity
import com.countdhikr.app.data.local.AppDataStore
import com.countdhikr.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AzanPlaybackService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var fallbackRingtone: android.media.Ringtone? = null
    private var isPlaying = false
    private val scope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val TAG = "AzanPlaybackService"
        private const val NOTIFICATION_ID = 9999
        private const val ACTION_STOP = "com.countdhikr.app.ACTION_STOP_AZAN"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service onCreate called.")
        
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service onStartCommand called.")
        
        if (intent?.action == ACTION_STOP) {
            Log.i(TAG, "Stop action triggered from notification.")
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
        
        // Start Foreground immediately with placeholder notification to avoid Android crash
        val initialNotification = createNotification(prayerName, "Preparing Azan playback...")
        startForeground(NOTIFICATION_ID, initialNotification)
        
        if (!isPlaying) {
            isPlaying = true
            startAzanPlayback(prayerName)
        }

        return START_NOT_STICKY
    }

    private fun startAzanPlayback(prayerName: String) {
        scope.launch {
            try {
                val dataStore = AppDataStore(applicationContext)
                val appState = dataStore.appState.first()
                val settings = appState.settings
                
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                val ringerMode = audioManager.ringerMode
                
                val shouldPlaySound = settings.azanSound && (ringerMode == android.media.AudioManager.RINGER_MODE_NORMAL)
                val shouldVibrate = settings.azanVibrate && (ringerMode != android.media.AudioManager.RINGER_MODE_SILENT)

                if (shouldPlaySound) {
                    val url = settings.customAzanUrl ?: Constants.AZAN_SOUNDS.first().url
                    Log.i(TAG, "Playing Azan for $prayerName from URL: $url")

                    // Update notification text to show voice name
                    val voiceName = Constants.AZAN_SOUNDS.find { it.url == url }?.name ?: "Selected Voice"
                    updateNotification(prayerName, "Playing Azan ($voiceName)")

                    // Initialize MediaPlayer
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(url)
                        
                        setOnPreparedListener { mp ->
                            Log.i(TAG, "MediaPlayer prepared. Starting playback.")
                            mp.start()
                            
                            // Start vibration if enabled and system permits
                            if (shouldVibrate) {
                                startVibration()
                            }
                        }
                        
                        setOnCompletionListener {
                            Log.i(TAG, "MediaPlayer completed playback. Stopping service.")
                            stopSelf()
                        }
                        
                        setOnErrorListener { _, what, extra ->
                            Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra. Playing fallback alarm sound.")
                            playFallbackAlarm()
                            true
                        }
                        
                        prepareAsync()
                    }
                } else {
                    // Update notification to silent alert
                    updateNotification(prayerName, "Prayer Time Alert")
                    if (shouldVibrate) {
                        Log.i(TAG, "Silent/vibrate mode: starting vibration only.")
                        startVibration()
                        // Stop vibration and service after 15 seconds to avoid background battery drain
                        scope.launch {
                            kotlinx.coroutines.delay(15000)
                            stopSelf()
                        }
                    } else {
                        Log.i(TAG, "Silent mode: stopping service immediately.")
                        stopSelf()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting MediaPlayer: ${e.message}. Playing fallback alarm sound.", e)
                playFallbackAlarm()
            }
        }
    }

    private fun playFallbackAlarm() {
        try {
            val alert: android.net.Uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            fallbackRingtone = android.media.RingtoneManager.getRingtone(applicationContext, alert)
            fallbackRingtone?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    it.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                }
                it.play()
                Log.i(TAG, "Playing offline fallback alarm sound successfully.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play fallback alarm sound: ${e.message}", e)
            stopSelf()
        }
    }

    private fun startVibration() {
        vibrator?.let { vib ->
            if (vib.hasVibrator()) {
                val pattern = longArrayOf(0, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 = loop infinitely
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(pattern, 0)
                }
            }
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    private fun createNotification(prayerName: String, contentText: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            201,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AzanPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            202,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "azan_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Azan: $prayerName 🕌")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun updateNotification(prayerName: String, contentText: String) {
        val notification = createNotification(prayerName, contentText)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy called. Releasing resources.")
        
        stopVibration()
        
        fallbackRingtone?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping fallbackRingtone in onDestroy: ${e.message}")
            }
        }
        fallbackRingtone = null
        
        mediaPlayer?.apply {
            try {
                if (isPlaying) {
                    stop()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping mediaPlayer in onDestroy: ${e.message}")
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
