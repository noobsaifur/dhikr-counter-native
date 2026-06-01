package com.countdhikr.app.ui.screens.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.countdhikr.app.CountDhikrApp
import com.countdhikr.app.data.model.DailyDhikr
import com.countdhikr.app.data.model.Dhikr
import com.countdhikr.app.data.model.Dua
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ListUiState(
    val dhikrs: List<Dhikr> = emptyList(),
    val duas: List<Dua> = emptyList(),
    val dailyDhikrs: List<DailyDhikr> = emptyList(),
    val activeDhikrId: String? = null,
    val activeDailyDhikrId: String? = null,
    val activeDailyDhikrDate: String? = null,
    val soundEnabled: Boolean = false
)

class ListViewModel(application: Application) : AndroidViewModel(application) {
    private val dhikrRepository = (application as CountDhikrApp).dhikrRepository

    val uiState: StateFlow<ListUiState> = dhikrRepository.appState.map {
        ListUiState(
            dhikrs = it.dhikrs,
            duas = it.duas,
            dailyDhikrs = it.dailyDhikrs,
            activeDhikrId = it.activeDhikrId,
            activeDailyDhikrId = it.dailyDhikrSettings.activeDailyDhikrId,
            activeDailyDhikrDate = it.dailyDhikrSettings.activeDailyDhikrDate,
            soundEnabled = it.settings.sound
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState()
    )

    val showAddDhikrDialog = MutableStateFlow(false)
    val showAddDuaDialog = MutableStateFlow(false)

    fun addDhikr(title: String, target: Int, arabic: String? = null) {
        viewModelScope.launch {
            dhikrRepository.addDhikr(title, arabic, target)
        }
    }

    fun deleteDhikr(id: String) {
        viewModelScope.launch {
            dhikrRepository.deleteDhikr(id)
        }
    }

    fun selectDhikr(id: String) {
        viewModelScope.launch {
            val normalizedId = if (id.isEmpty()) null else id
            dhikrRepository.setActiveDhikr(normalizedId)
            dhikrRepository.setActiveDailyDhikrItem(null, null)
        }
    }

    fun addDua(arabic: String, desc: String) {
        viewModelScope.launch {
            dhikrRepository.addDua(arabic, desc)
        }
    }

    fun deleteDua(id: String) {
        viewModelScope.launch {
            dhikrRepository.deleteDua(id)
        }
    }

    fun addDhikrToDay(date: String, title: String, target: Int, arabic: String? = null) {
        viewModelScope.launch {
            dhikrRepository.addDhikrToDay(date, title, arabic, target)
        }
    }

    fun deleteDhikrFromDay(date: String, dhikrId: String) {
        viewModelScope.launch {
            dhikrRepository.deleteDhikrFromDay(date, dhikrId)
        }
    }

    fun selectDailyDhikr(dhikrId: String) {
        viewModelScope.launch {
            val date = com.countdhikr.app.util.DateUtils.getTodayDateString()
            dhikrRepository.setActiveDailyDhikrItem(date, dhikrId)
        }
    }

    fun incrementDayDhikr(date: String, dhikrId: String) {
        viewModelScope.launch {
            dhikrRepository.incrementDayDhikr(date, dhikrId)
        }
    }

    fun resetDayDhikr(date: String, dhikrId: String) {
        viewModelScope.launch {
            dhikrRepository.resetDayDhikr(date, dhikrId)
        }
    }
}
