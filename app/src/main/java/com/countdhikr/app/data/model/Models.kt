package com.countdhikr.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Enums ────────────────────────────────────────────────────────────────────

@Serializable
enum class VibrateIntensity {
    @SerialName("light") LIGHT,
    @SerialName("medium") MEDIUM,
    @SerialName("strong") STRONG
}

@Serializable
enum class DailyDhikrStatus {
    @SerialName("completed") COMPLETED,
    @SerialName("partial") PARTIAL,
    @SerialName("missed") MISSED
}

// ── Core Domain Models ───────────────────────────────────────────────────────

@Serializable
data class Dhikr(
    val id: String,
    val title: String,
    val arabic: String? = null,
    val target: Int,
    val count: Int = 0
)

@Serializable
data class Dua(
    val id: String,
    val arabic: String,
    val desc: String
)

@Serializable
data class DailyDhikrItem(
    val id: String,
    val title: String,
    val arabic: String? = null,
    val count: Int = 0,
    val target: Int
)

@Serializable
data class DailyDhikr(
    val id: String,
    val date: String,
    val hijriDate: String,
    val dayNumber: Int,
    val status: DailyDhikrStatus = DailyDhikrStatus.MISSED,
    val dhikrs: List<DailyDhikrItem> = emptyList()
)

@Serializable
data class DailyDhikrSettings(
    val trackingStartDate: String? = null,
    val activeDailyDhikrDate: String? = null,
    val activeDailyDhikrId: String? = null,
    val dayNumberOffset: Int = 0
)

// ── Location & Country ───────────────────────────────────────────────────────

@Serializable
data class SelectedCountry(
    val name: String,
    val code: String,
    val lat: Double,
    val lng: Double,
    val method: Int
)

// ── Settings ─────────────────────────────────────────────────────────────────

@Serializable
data class AppSettings(
    val vibrate: Boolean = true,
    val vibrateIntensity: VibrateIntensity = VibrateIntensity.MEDIUM,
    val sound: Boolean = false,
    val darkMode: Boolean = true,
    val animateBackground: Boolean = true,
    val reminderNotification: Boolean = false,
    val dhikrReminderEnabled: Boolean = false,
    val salatReminderEnabled: Boolean = false,
    val prayerNotifications: Boolean = false,
    val azanEnabled: Boolean = false,
    val azanSound: Boolean = false,
    val azanVibrate: Boolean = false,
    val prayerMethod: Int = 8,
    val customAzanUrl: String? = null,
    val selectedCountry: SelectedCountry? = null,
    val useAutoLocation: Boolean = true,
    val cachedPrayerTimes: PrayerTimes? = null,
    val cachedLocationName: String? = null,
    val cachedLatitude: Double? = null,
    val cachedLongitude: Double? = null
)

// ── App State (root persisted state) ─────────────────────────────────────────

@Serializable
data class AppState(
    val dhikrs: List<Dhikr> = emptyList(),
    val duas: List<Dua> = emptyList(),
    val dailyDhikrs: List<DailyDhikr> = emptyList(),
    val dailyDhikrSettings: DailyDhikrSettings = DailyDhikrSettings(),
    val activeDhikrId: String? = null,
    val generalCount: Int = 0,
    val settings: AppSettings = AppSettings()
)

// ── Prayer-Related Models ────────────────────────────────────────────────────

@Serializable
data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

@Serializable
data class PrayerMethod(
    val id: Int,
    val name: String
)

@Serializable
data class AzanSound(
    val id: String,
    val name: String,
    val url: String
)

// ── Companion / Defaults ─────────────────────────────────────────────────────

val DEFAULT_PRAYER_METHODS: List<PrayerMethod> = listOf(
    PrayerMethod(0, "Shia Ithna-Ashari"),
    PrayerMethod(1, "University of Islamic Sciences, Karachi"),
    PrayerMethod(2, "Islamic Society of North America (ISNA)"),
    PrayerMethod(3, "Muslim World League"),
    PrayerMethod(4, "Umm Al-Qura University, Makkah"),
    PrayerMethod(5, "Egyptian General Authority of Survey"),
    PrayerMethod(7, "Institute of Geophysics, University of Tehran"),
    PrayerMethod(8, "Gulf Region"),
    PrayerMethod(9, "Kuwait"),
    PrayerMethod(10, "Qatar"),
    PrayerMethod(11, "Majlis Ugama Islam Singapura, Singapore"),
    PrayerMethod(12, "Union Organization Islamic de France"),
    PrayerMethod(13, "Diyanet İşleri Başkanlığı, Turkey"),
    PrayerMethod(14, "Spiritual Administration of Muslims of Russia"),
    PrayerMethod(15, "Moonsighting Committee Worldwide")
)

val DEFAULT_AZAN_SOUNDS: List<AzanSound> = listOf(
    AzanSound("default", "Default Azan", ""),
    AzanSound("makkah", "Makkah", "https://cdn.aladhan.com/audio/adhaan/makkah.mp3"),
    AzanSound("madina", "Madinah", "https://cdn.aladhan.com/audio/adhaan/madina.mp3")
)
