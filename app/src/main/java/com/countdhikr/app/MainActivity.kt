package com.countdhikr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.countdhikr.app.ui.navigation.AppNavigation
import com.countdhikr.app.ui.theme.CountDhikrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as CountDhikrApp
        val dhikrRepository = app.dhikrRepository
        
        setContent {
            val appState by dhikrRepository.appState.collectAsState(
                initial = com.countdhikr.app.data.model.AppState()
            )
            
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { _ -> }

            LaunchedEffect(Unit) {
                val permissions = mutableListOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }

            // Butter-smooth reactive alarm scheduler trigger
            LaunchedEffect(appState.settings) {
                if (appState.settings.cachedPrayerTimes != null) {
                    com.countdhikr.app.notification.PrayerAlarmScheduler.scheduleAlarms(
                        app.applicationContext,
                        appState.settings
                    )
                }
            }

            CountDhikrTheme(
                darkTheme = appState.settings.darkMode,
                animateBackground = appState.settings.animateBackground
            ) {
                AppNavigation()
            }
        }
    }
}
