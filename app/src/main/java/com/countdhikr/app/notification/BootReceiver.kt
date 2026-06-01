package com.countdhikr.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.countdhikr.app.data.local.AppDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i("BootReceiver", "Received boot broadcast action: $action")
        
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.MY_PACKAGE_REPLACED"
        ) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val dataStore = AppDataStore(context.applicationContext)
                    val appState = dataStore.appState.first()
                    val settings = appState.settings
                    
                    Log.i("BootReceiver", "Rescheduling alarms on system reboot/app update...")
                    PrayerAlarmScheduler.scheduleAlarms(context.applicationContext, settings)
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
