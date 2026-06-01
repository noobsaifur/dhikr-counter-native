package com.countdhikr.app.ui.screens.prayer

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.countdhikr.app.CountDhikrApp
import com.countdhikr.app.data.model.PrayerTimes
import com.countdhikr.app.data.model.SelectedCountry
import com.countdhikr.app.util.DateUtils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrayerUiState(
    val times: PrayerTimes? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val locationName: String? = null,
    val hijriDate: String? = null,
    val nextPrayer: String? = null,
    val timeToNext: String? = null,
    val azanEnabled: Boolean = false,
    val azanSound: Boolean = false,
    val azanVibrate: Boolean = false,
    val prayerMethod: Int = 8,
    val selectedCountry: SelectedCountry? = null,
    val useAutoLocation: Boolean = true,
    val customAzanUrl: String? = null
)

class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as CountDhikrApp
    private val dhikrRepository = app.dhikrRepository
    private val prayerRepository = app.prayerRepository
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    
    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dhikrRepository.appState.collect { state ->
                val settings = state.settings
                _uiState.update {
                    it.copy(
                        azanEnabled = settings.azanEnabled,
                        azanSound = settings.azanSound,
                        azanVibrate = settings.azanVibrate,
                        prayerMethod = settings.prayerMethod,
                        selectedCountry = settings.selectedCountry,
                        useAutoLocation = settings.useAutoLocation,
                        customAzanUrl = settings.customAzanUrl,
                        times = settings.cachedPrayerTimes,
                        locationName = settings.cachedLocationName
                    )
                }
                if (settings.cachedPrayerTimes == null && !settings.useAutoLocation && settings.selectedCountry != null) {
                    fetchPrayerTimesForLocation(
                        lat = settings.selectedCountry.lat,
                        lng = settings.selectedCountry.lng,
                        method = settings.prayerMethod,
                        presetLocationName = settings.selectedCountry.name
                    )
                }
            }
        }
        
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                updateNextPrayer()
                delay(60000) // update every minute
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPrayerTimes() {
        if (!_uiState.value.useAutoLocation) {
            val country = _uiState.value.selectedCountry
            if (country != null) {
                fetchPrayerTimesForLocation(country.lat, country.lng, _uiState.value.prayerMethod, country.name)
            } else {
                // Default to Mecca if manual but no country
                fetchPrayerTimesForLocation(21.4225, 39.8262, 4, "Mecca (Default)")
            }
            return
        }

        _uiState.update { it.copy(loading = true, error = null) }
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    fetchPrayerTimesForLocation(location.latitude, location.longitude, _uiState.value.prayerMethod, null)
                } else {
                    _uiState.update { it.copy(loading = false, error = "Could not get location") }
                }
            }.addOnFailureListener {
                _uiState.update { state -> state.copy(loading = false, error = it.message) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(loading = false, error = e.message) }
        }
    }

    private fun fetchPrayerTimesForLocation(lat: Double, lng: Double, method: Int, presetLocationName: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            
            val prayerResult = prayerRepository.fetchPrayerTimes(lat, lng, method)
            var times: com.countdhikr.app.data.model.PrayerTimes? = null
            if (prayerResult.isSuccess) {
                times = prayerResult.getOrNull()
                _uiState.update { it.copy(times = times) }
                updateNextPrayer()
            } else {
                _uiState.update { it.copy(error = "Failed to fetch prayer times") }
            }

            val today = DateUtils.getTodayDateString()
            val hijriResult = prayerRepository.fetchHijriDate(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()))
            if (hijriResult.isSuccess) {
                _uiState.update { it.copy(hijriDate = hijriResult.getOrNull()) }
            }

            var resolvedLocationName = presetLocationName
            if (presetLocationName != null) {
                _uiState.update { it.copy(locationName = presetLocationName, loading = false) }
            } else {
                val geoResult = prayerRepository.reverseGeocode(lat, lng)
                if (geoResult.isSuccess) {
                    resolvedLocationName = geoResult.getOrNull()
                    _uiState.update { it.copy(locationName = resolvedLocationName, loading = false) }
                } else {
                    _uiState.update { it.copy(loading = false) }
                }
            }

            // Persist cached timings and coordinate details inside DataStore
            if (times != null) {
                dhikrRepository.updateSettings { current ->
                    current.copy(
                        cachedPrayerTimes = times,
                        cachedLocationName = resolvedLocationName ?: current.cachedLocationName ?: "My Location",
                        cachedLatitude = lat,
                        cachedLongitude = lng
                    )
                }
            }
        }
    }

    private fun updateNextPrayer() {
        val times = _uiState.value.times ?: return
        
        val now = Date()
        val format = SimpleDateFormat("HH:mm", Locale.US)
        val currentTimeStr = format.format(now)
        
        val prayers = listOf(
            "Fajr" to times.fajr,
            "Sunrise" to times.sunrise,
            "Dhuhr" to times.dhuhr,
            "Asr" to times.asr,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )
        
        var nextP = prayers.firstOrNull { it.second > currentTimeStr }
        if (nextP == null) {
            nextP = prayers.first() // Next day's Fajr
        }
        
        _uiState.update { 
            it.copy(
                nextPrayer = nextP.first,
                timeToNext = calculateTimeTo(currentTimeStr, nextP.second)
            )
        }
    }
    
    private fun calculateTimeTo(current: String, target: String): String {
        try {
            val format = SimpleDateFormat("HH:mm", Locale.US)
            val d1 = format.parse(current) ?: return ""
            var d2 = format.parse(target) ?: return ""
            
            if (d2.before(d1)) {
                // Add one day (24 hours) in milliseconds
                d2 = Date(d2.time + 24 * 60 * 60 * 1000)
            }
            
            val diff = d2.time - d1.time
            val diffMinutes = diff / (60 * 1000)
            val hours = diffMinutes / 60
            val mins = diffMinutes % 60
            return if (hours == 0L) {
                "$mins min"
            } else {
                "${hours}h ${mins}m"
            }
        } catch (e: Exception) {
            return ""
        }
    }

    fun toggleAutoLocation() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(useAutoLocation = !it.useAutoLocation) }
            refreshPrayerTimes()
        }
    }

    fun selectCountry(country: SelectedCountry) {
        viewModelScope.launch {
            dhikrRepository.updateSettings {
                it.copy(
                    selectedCountry = country,
                    useAutoLocation = false,
                    prayerMethod = country.method
                )
            }
            refreshPrayerTimes()
        }
    }

    fun setPrayerMethod(methodId: Int) {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(prayerMethod = methodId) }
            refreshPrayerTimes()
        }
    }

    fun toggleAzan() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(azanEnabled = !it.azanEnabled) }
        }
    }

    fun toggleAzanSound() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(azanSound = !it.azanSound) }
        }
    }

    fun toggleAzanVibrate() {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(azanVibrate = !it.azanVibrate) }
        }
    }

    fun setAzanSoundUrl(url: String) {
        viewModelScope.launch {
            dhikrRepository.updateSettings { it.copy(customAzanUrl = url) }
        }
    }
}
