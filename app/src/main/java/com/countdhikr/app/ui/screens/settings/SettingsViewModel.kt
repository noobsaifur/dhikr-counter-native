package com.countdhikr.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.countdhikr.app.CountDhikrApp
import com.countdhikr.app.data.model.AppSettings
import com.countdhikr.app.data.model.VibrateIntensity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val dayNumberOffset: Int = 0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dhikrRepository = (application as CountDhikrApp).dhikrRepository

    val uiState: StateFlow<SettingsUiState> = dhikrRepository.appState.map {
        SettingsUiState(
            settings = it.settings,
            dayNumberOffset = it.dailyDhikrSettings.dayNumberOffset
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleVibrate() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(vibrate = !it.vibrate) }
        }
    }

    fun setVibrateIntensity(intensity: VibrateIntensity) {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(vibrateIntensity = intensity) }
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(sound = !it.sound) }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(darkMode = !it.darkMode) }
        }
    }

    fun toggleAnimateBackground() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(animateBackground = !it.animateBackground) }
        }
    }

    fun toggleReminder() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(reminderNotification = !it.reminderNotification) }
        }
    }

    fun toggleDhikrReminder() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(dhikrReminderEnabled = !it.dhikrReminderEnabled) }
        }
    }

    fun toggleSalatReminder() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(salatReminderEnabled = !it.salatReminderEnabled) }
        }
    }

    fun setDayNumberOffset(offset: Int) {
        viewModelScope.launch {
            dhikrRepository.setDayNumberOffset(offset)
        }
    }

    fun factoryReset() {
        viewModelScope.launch {
            dhikrRepository.factoryReset()
        }
    }
}
