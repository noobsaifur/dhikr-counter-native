package com.countdhikr.app.ui.screens.home

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.countdhikr.app.CountDhikrApp
import com.countdhikr.app.data.model.DailyDhikr
import com.countdhikr.app.data.model.DailyDhikrItem
import com.countdhikr.app.data.model.VibrateIntensity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val count: Int = 0,
    val target: Int = 0,
    val name: String = "General Counter",
    val arabic: String? = null,
    val vibrateEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val vibrateIntensity: VibrateIntensity = VibrateIntensity.MEDIUM,
    val todayDhikr: DailyDhikr? = null,
    val activeDailyDhikrItem: DailyDhikrItem? = null,
    val isInDailyMode: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dhikrRepository = (application as CountDhikrApp).dhikrRepository
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    val uiState: StateFlow<HomeUiState> = dhikrRepository.appState.map { state ->
        val activeDailyDhikrId = state.dailyDhikrSettings.activeDailyDhikrId
        val todayDhikr = state.dailyDhikrs.find { it.date == state.dailyDhikrSettings.activeDailyDhikrDate }
        val activeItem = todayDhikr?.dhikrs?.find { it.id == activeDailyDhikrId }
        
        val isInDailyMode = activeItem != null
        
        if (isInDailyMode) {
            HomeUiState(
                count = activeItem!!.count,
                target = activeItem.target,
                name = activeItem.title,
                arabic = activeItem.arabic,
                vibrateEnabled = state.settings.vibrate,
                soundEnabled = state.settings.sound,
                vibrateIntensity = state.settings.vibrateIntensity,
                todayDhikr = todayDhikr,
                activeDailyDhikrItem = activeItem,
                isInDailyMode = true
            )
        } else {
            val activeGeneral = state.dhikrs.find { it.id == state.activeDhikrId }
            HomeUiState(
                count = activeGeneral?.count ?: state.generalCount,
                target = activeGeneral?.target ?: 0,
                name = activeGeneral?.title ?: "General Counter",
                arabic = activeGeneral?.arabic,
                vibrateEnabled = state.settings.vibrate,
                soundEnabled = state.settings.sound,
                vibrateIntensity = state.settings.vibrateIntensity,
                todayDhikr = todayDhikr,
                activeDailyDhikrItem = null,
                isInDailyMode = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            dhikrRepository.initializeTodayDhikr()
        }
    }

    fun onIncrement() {
        val currentState = uiState.value
        
        if (currentState.vibrateEnabled) {
            triggerVibration(currentState.vibrateIntensity)
        }
        
        viewModelScope.launch {
            if (currentState.isInDailyMode) {
                dhikrRepository.incrementDayDhikr(currentState.todayDhikr!!.date, currentState.activeDailyDhikrItem!!.id)
            } else {
                dhikrRepository.increment()
            }
        }
    }

    fun onReset() {
        val currentState = uiState.value
        viewModelScope.launch {
            if (currentState.isInDailyMode) {
                dhikrRepository.resetDayDhikr(currentState.todayDhikr!!.date, currentState.activeDailyDhikrItem!!.id)
            } else {
                dhikrRepository.resetCounter()
            }
        }
    }

    fun onToggleVibrate() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(vibrate = !it.vibrate) }
        }
    }

    fun onToggleSound() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(sound = !it.sound) }
        }
    }

    fun onSelectDailyDhikr(dhikrId: String) {
        viewModelScope.launch {
            val date = uiState.value.todayDhikr?.date
            dhikrRepository.setActiveDailyDhikrItem(date, dhikrId)
        }
    }

    fun setGeneralMode() {
        viewModelScope.launch {
            val date = uiState.value.todayDhikr?.date
            dhikrRepository.setActiveDailyDhikrItem(date, null)
            dhikrRepository.setActiveDhikr(null)
        }
    }

    private fun triggerVibration(intensity: VibrateIntensity) {
        if (!vibrator.hasVibrator()) return
        
        val duration = when (intensity) {
            VibrateIntensity.LIGHT -> 8L
            VibrateIntensity.MEDIUM -> 15L
            VibrateIntensity.STRONG -> 25L
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = when (intensity) {
                VibrateIntensity.LIGHT -> 102 // 40% of 255
                VibrateIntensity.MEDIUM -> 191 // 75% of 255
                VibrateIntensity.STRONG -> 255 // 100% of 255
            }
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}
