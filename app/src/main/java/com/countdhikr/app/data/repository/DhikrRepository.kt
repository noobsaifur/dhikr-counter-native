package com.countdhikr.app.data.repository

import com.countdhikr.app.data.local.AppDataStore
import com.countdhikr.app.data.model.AppSettings
import com.countdhikr.app.data.model.AppState
import com.countdhikr.app.data.model.DailyDhikr
import com.countdhikr.app.data.model.DailyDhikrItem
import com.countdhikr.app.data.model.DailyDhikrSettings
import com.countdhikr.app.data.model.DailyDhikrStatus
import com.countdhikr.app.data.model.Dhikr
import com.countdhikr.app.data.model.Dua
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Single source of truth for dhikr / dua / daily-tracking mutations.
 *
 * Every public method reads the current [AppState], computes the new state,
 * and writes it back through [AppDataStore]. The exposed [appState] flow lets
 * ViewModels observe changes reactively.
 */
class DhikrRepository(private val dataStore: AppDataStore) {

    val appState: Flow<AppState> = dataStore.appState

    // ── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun update(transform: (AppState) -> AppState) {
        dataStore.updateState(transform)
    }

    private fun todayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun generateId(): String = UUID.randomUUID().toString()

    // ── Dhikr CRUD ───────────────────────────────────────────────────────────

    suspend fun addDhikr(title: String, arabic: String? = null, target: Int) {
        update { state ->
            val newDhikr = Dhikr(
                id = generateId(),
                title = title,
                arabic = arabic,
                target = target,
                count = 0
            )
            state.copy(dhikrs = state.dhikrs + newDhikr)
        }
    }

    suspend fun deleteDhikr(dhikrId: String) {
        update { state ->
            val newActive = if (state.activeDhikrId == dhikrId) null else state.activeDhikrId
            state.copy(
                dhikrs = state.dhikrs.filter { it.id != dhikrId },
                activeDhikrId = newActive
            )
        }
    }

    suspend fun setActiveDhikr(dhikrId: String?) {
        update { state -> state.copy(activeDhikrId = dhikrId) }
    }

    // ── Dua CRUD ─────────────────────────────────────────────────────────────

    suspend fun addDua(arabic: String, desc: String) {
        update { state ->
            val newDua = Dua(id = generateId(), arabic = arabic, desc = desc)
            state.copy(duas = state.duas + newDua)
        }
    }

    suspend fun deleteDua(duaId: String) {
        update { state ->
            state.copy(duas = state.duas.filter { it.id != duaId })
        }
    }

    // ── General Counter ──────────────────────────────────────────────────────

    suspend fun increment() {
        update { state ->
            val activeId = state.activeDhikrId
            if (activeId != null) {
                val updatedDhikrs = state.dhikrs.map { dhikr ->
                    if (dhikr.id == activeId) dhikr.copy(count = dhikr.count + 1) else dhikr
                }
                state.copy(dhikrs = updatedDhikrs)
            } else {
                state.copy(generalCount = state.generalCount + 1)
            }
        }
    }

    suspend fun resetCounter() {
        update { state ->
            val activeId = state.activeDhikrId
            if (activeId != null) {
                val updatedDhikrs = state.dhikrs.map { dhikr ->
                    if (dhikr.id == activeId) dhikr.copy(count = 0) else dhikr
                }
                state.copy(dhikrs = updatedDhikrs)
            } else {
                state.copy(generalCount = 0)
            }
        }
    }

    // ── Daily Dhikr ──────────────────────────────────────────────────────────

    suspend fun addDhikrToDay(date: String, title: String, arabic: String? = null, target: Int) {
        update { state ->
            val newItem = DailyDhikrItem(
                id = generateId(),
                title = title,
                arabic = arabic,
                count = 0,
                target = target
            )
            val updatedDays = state.dailyDhikrs.map { day ->
                if (day.date == date) {
                    day.copy(dhikrs = day.dhikrs + newItem)
                } else {
                    day
                }
            }
            state.copy(dailyDhikrs = updatedDays)
        }
    }

    suspend fun deleteDhikrFromDay(date: String, itemId: String) {
        update { state ->
            val updatedDays = state.dailyDhikrs.map { day ->
                if (day.date == date) {
                    val filtered = day.dhikrs.filter { it.id != itemId }
                    day.copy(
                        dhikrs = filtered,
                        status = computeDayStatus(filtered)
                    )
                } else {
                    day
                }
            }
            // Clear active item if it was deleted
            val settings = state.dailyDhikrSettings
            val newSettings = if (settings.activeDailyDhikrId == itemId) {
                settings.copy(activeDailyDhikrId = null)
            } else {
                settings
            }
            state.copy(dailyDhikrs = updatedDays, dailyDhikrSettings = newSettings)
        }
    }

    suspend fun incrementDayDhikr(date: String, itemId: String) {
        update { state ->
            val updatedDays = state.dailyDhikrs.map { day ->
                if (day.date == date) {
                    val updatedItems = day.dhikrs.map { item ->
                        if (item.id == itemId) item.copy(count = item.count + 1) else item
                    }
                    day.copy(
                        dhikrs = updatedItems,
                        status = computeDayStatus(updatedItems)
                    )
                } else {
                    day
                }
            }
            state.copy(dailyDhikrs = updatedDays)
        }
    }

    suspend fun resetDayDhikr(date: String, itemId: String) {
        update { state ->
            val updatedDays = state.dailyDhikrs.map { day ->
                if (day.date == date) {
                    val updatedItems = day.dhikrs.map { item ->
                        if (item.id == itemId) item.copy(count = 0) else item
                    }
                    day.copy(
                        dhikrs = updatedItems,
                        status = computeDayStatus(updatedItems)
                    )
                } else {
                    day
                }
            }
            state.copy(dailyDhikrs = updatedDays)
        }
    }

    suspend fun setActiveDailyDhikrItem(date: String?, itemId: String?) {
        update { state ->
            state.copy(
                dailyDhikrSettings = state.dailyDhikrSettings.copy(
                    activeDailyDhikrDate = date,
                    activeDailyDhikrId = itemId
                )
            )
        }
    }

    suspend fun setDayNumberOffset(offset: Int) {
        update { state ->
            state.copy(
                dailyDhikrSettings = state.dailyDhikrSettings.copy(dayNumberOffset = offset)
            )
        }
    }

    // ── Initialize Today ─────────────────────────────────────────────────────

    /**
     * Creates today's [DailyDhikr] card if it doesn't already exist, and
     * back-fills any missed days between the last recorded day and today.
     */
    suspend fun initializeTodayDhikr(hijriDate: String = "") {
        update { state ->
            val today = todayDateString()
            val settings = state.dailyDhikrSettings
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            // Already exists?
            if (state.dailyDhikrs.any { it.date == today }) {
                return@update state
            }

            // Set tracking start date if first time
            val startDate = settings.trackingStartDate ?: today
            val updatedSettings = settings.copy(
                trackingStartDate = startDate,
                activeDailyDhikrDate = today
            )

            // Back-fill missed days
            val missedDays = mutableListOf<DailyDhikr>()
            val lastDay = state.dailyDhikrs.maxByOrNull { it.date }
            if (lastDay != null) {
                val lastDate = dateFormat.parse(lastDay.date)
                val todayDate = dateFormat.parse(today)
                if (lastDate != null && todayDate != null) {
                    val diffMs = todayDate.time - lastDate.time
                    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
                    val cal = Calendar.getInstance()
                    cal.time = lastDate
                    for (i in 1 until diffDays) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val missedDateStr = dateFormat.format(cal.time)
                        // Only add if not already present
                        if (state.dailyDhikrs.none { it.date == missedDateStr }) {
                            val dayNum = computeDayNumber(
                                startDate, missedDateStr, dateFormat, updatedSettings.dayNumberOffset
                            )
                            missedDays.add(
                                DailyDhikr(
                                    id = generateId(),
                                    date = missedDateStr,
                                    hijriDate = "",
                                    dayNumber = dayNum,
                                    status = DailyDhikrStatus.MISSED,
                                    dhikrs = emptyList()
                                )
                            )
                        }
                    }
                }
            }

            // Create today's card
            val dayNumber = computeDayNumber(
                startDate, today, dateFormat, updatedSettings.dayNumberOffset
            )
            val todayCard = DailyDhikr(
                id = generateId(),
                date = today,
                hijriDate = hijriDate,
                dayNumber = dayNumber,
                status = DailyDhikrStatus.MISSED,
                dhikrs = emptyList()
            )

            state.copy(
                dailyDhikrs = state.dailyDhikrs + missedDays + todayCard,
                dailyDhikrSettings = updatedSettings
            )
        }
    }

    // ── Settings ─────────────────────────────────────────────────────────────

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        update { state ->
            state.copy(settings = transform(state.settings))
        }
    }

    // ── Factory Reset ────────────────────────────────────────────────────────

    suspend fun factoryReset() {
        dataStore.clearState()
    }

    // ── Private Utilities ────────────────────────────────────────────────────

    private fun computeDayStatus(items: List<DailyDhikrItem>): DailyDhikrStatus {
        if (items.isEmpty()) return DailyDhikrStatus.MISSED
        val allCompleted = items.all { it.count >= it.target }
        val anyStarted = items.any { it.count > 0 }
        return when {
            allCompleted -> DailyDhikrStatus.COMPLETED
            anyStarted -> DailyDhikrStatus.PARTIAL
            else -> DailyDhikrStatus.MISSED
        }
    }

    private fun computeDayNumber(
        startDateStr: String,
        currentDateStr: String,
        dateFormat: SimpleDateFormat,
        offset: Int
    ): Int {
        val startDate = dateFormat.parse(startDateStr) ?: return 1 + offset
        val currentDate = dateFormat.parse(currentDateStr) ?: return 1 + offset
        val diffMs = currentDate.time - startDate.time
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
        return diffDays + 1 + offset
    }
}
