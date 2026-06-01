package com.countdhikr.app.data.repository

import com.countdhikr.app.data.model.PrayerTimes
import com.countdhikr.app.data.remote.AladhanApi
import com.countdhikr.app.data.remote.GeocodingApi

/**
 * Repository that provides prayer times, Hijri date conversion,
 * and reverse geocoding through the Aladhan and BigDataCloud APIs.
 */
class PrayerRepository(
    private val aladhanApi: AladhanApi,
    private val geocodingApi: GeocodingApi
) {

    /**
     * Fetch prayer times for the given coordinates and calculation method.
     *
     * @param lat    Latitude
     * @param lng    Longitude
     * @param method Aladhan calculation method id
     * @param date   Date in DD-MM-YYYY format (defaults to today if blank)
     * @return [PrayerTimes] wrapped in [Result]
     */
    suspend fun fetchPrayerTimes(
        lat: Double,
        lng: Double,
        method: Int,
        date: String = formatTodayDate()
    ): Result<PrayerTimes> = runCatching {
        val response = aladhanApi.getTimings(
            date = date,
            latitude = lat,
            longitude = lng,
            method = method
        )
        val t = response.data.timings
        PrayerTimes(
            fajr = t.fajr,
            sunrise = t.sunrise,
            dhuhr = t.dhuhr,
            asr = t.asr,
            maghrib = t.maghrib,
            isha = t.isha
        )
    }

    /**
     * Convert a Gregorian date to a Hijri date string.
     *
     * @param dateStr Gregorian date in DD-MM-YYYY format
     * @return Human-readable Hijri date (e.g. "04 Dhul-Hijjah 1447") wrapped in [Result]
     */
    suspend fun fetchHijriDate(dateStr: String): Result<String> = runCatching {
        val response = aladhanApi.getHijriDate(dateStr)
        val hijri = response.data.hijri
        val day = hijri.day ?: ""
        val month = hijri.month?.en ?: ""
        val year = hijri.year ?: ""
        "$day $month $year".trim()
    }

    /**
     * Reverse-geocode GPS coordinates to a human-readable city/country string.
     *
     * @param lat Latitude
     * @param lng Longitude
     * @return Location string (e.g. "Dubai, United Arab Emirates") wrapped in [Result]
     */
    suspend fun reverseGeocode(lat: Double, lng: Double): Result<String> = runCatching {
        val response = geocodingApi.reverseGeocode(
            latitude = lat,
            longitude = lng
        )
        val city = response.city ?: response.locality ?: response.principalSubdivision ?: ""
        val country = sanitizeCountry(response.countryName ?: "")
        buildString {
            if (city.isNotBlank()) append(city)
            if (city.isNotBlank() && country.isNotBlank()) append(", ")
            if (country.isNotBlank()) append(country)
        }.ifBlank { "Unknown location" }
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private fun sanitizeCountry(country: String): String {
        var clean = country.trim()
        if (clean.endsWith(" (the)", ignoreCase = true)) {
            clean = clean.substring(0, clean.length - 6).trim()
        }
        if (clean.equals("United Arab Emirates", ignoreCase = true)) {
            return "UAE"
        }
        return clean
    }

    private fun formatTodayDate(): String {
        val cal = java.util.Calendar.getInstance()
        val day = String.format("%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))
        val month = String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
        val year = cal.get(java.util.Calendar.YEAR)
        return "$day-$month-$year"
    }
}
