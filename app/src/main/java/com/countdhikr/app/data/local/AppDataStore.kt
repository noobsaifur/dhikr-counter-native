package com.countdhikr.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.countdhikr.app.data.model.AppState
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Extension property to create a single DataStore instance scoped to the application.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "countdhikr_prefs"
)

/**
 * Persistence layer for the entire [AppState].
 */
class AppDataStore(private val context: Context) {

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    companion object {
        private val KEY_APP_STATE = stringPreferencesKey("app_state")
    }

    // ── Default State ────────────────────────────────────────────────────────

    private val defaultState: AppState
        get() = AppState()

    // ── Reactive Read ────────────────────────────────────────────────────────

    /**
     * Emits the current [AppState] and re-emits whenever it changes.
     * Falls back to [defaultState] on deserialization errors or first launch.
     */
    val appState: Flow<AppState> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[KEY_APP_STATE]
            if (json != null) {
                try {
                    jsonSerializer.decodeFromString<AppState>(json)
                } catch (_: Exception) {
                    defaultState
                }
            } else {
                defaultState
            }
        }

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Atomically persists the given [state] to DataStore.
     */
    suspend fun saveState(state: AppState) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_STATE] = jsonSerializer.encodeToString(state)
        }
    }

    /**
     * Atomically executes a transactional mutation on the persisted [AppState].
     */
    suspend fun updateState(transform: (AppState) -> AppState) {
        context.dataStore.edit { preferences ->
            val rawJson = preferences[KEY_APP_STATE]
            val currentState = if (rawJson != null) {
                try {
                    jsonSerializer.decodeFromString<AppState>(rawJson)
                } catch (_: Exception) {
                    defaultState
                }
            } else {
                defaultState
            }
            val updatedState = transform(currentState)
            preferences[KEY_APP_STATE] = jsonSerializer.encodeToString(updatedState)
        }
    }

    // ── Clear ────────────────────────────────────────────────────────────────

    /**
     * Resets persisted state to defaults by removing all keys.
     */
    suspend fun clearState() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
