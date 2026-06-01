package com.countdhikr.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ── Retrofit Service ─────────────────────────────────────────────────────────

interface AladhanApi {

    /**
     * Fetch prayer times for a specific date and location.
     *
     * @param date  DD-MM-YYYY format
     * @param latitude  Location latitude
     * @param longitude Location longitude
     * @param method    Calculation method id (e.g. 8 = Gulf Region)
     */
    @GET("/v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int
    ): AladhanTimingsResponse

    /**
     * Convert a Gregorian date to Hijri.
     *
     * @param date DD-MM-YYYY format
     */
    @GET("/v1/gToH/{date}")
    suspend fun getHijriDate(
        @Path("date") date: String
    ): AladhanHijriResponse
}

// ── Response DTOs ────────────────────────────────────────────────────────────

data class AladhanTimingsResponse(
    val code: Int,
    val status: String,
    val data: TimingsData
)

data class TimingsData(
    val timings: TimingsMap,
    val date: DateInfo? = null,
    val meta: MetaInfo? = null
)

data class TimingsMap(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Sunset") val sunset: String? = null,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String,
    @SerializedName("Imsak") val imsak: String? = null,
    @SerializedName("Midnight") val midnight: String? = null,
    @SerializedName("Firstthird") val firstThird: String? = null,
    @SerializedName("Lastthird") val lastThird: String? = null
)

data class DateInfo(
    val readable: String? = null,
    val timestamp: String? = null,
    val hijri: HijriDateInfo? = null,
    val gregorian: GregorianDateInfo? = null
)

data class HijriDateInfo(
    val date: String? = null,
    val format: String? = null,
    val day: String? = null,
    val weekday: HijriWeekday? = null,
    val month: HijriMonth? = null,
    val year: String? = null,
    val designation: Designation? = null
)

data class HijriWeekday(
    val en: String? = null,
    val ar: String? = null
)

data class HijriMonth(
    val number: Int? = null,
    val en: String? = null,
    val ar: String? = null
)

data class Designation(
    val abbreviated: String? = null,
    val expanded: String? = null
)

data class GregorianDateInfo(
    val date: String? = null,
    val format: String? = null,
    val day: String? = null,
    val weekday: GregorianWeekday? = null,
    val month: GregorianMonth? = null,
    val year: String? = null
)

data class GregorianWeekday(
    val en: String? = null
)

data class GregorianMonth(
    val number: Int? = null,
    val en: String? = null
)

data class MetaInfo(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val method: MethodInfo? = null
)

data class MethodInfo(
    val id: Int? = null,
    val name: String? = null
)

// ── Hijri conversion response ────────────────────────────────────────────────

data class AladhanHijriResponse(
    val code: Int,
    val status: String,
    val data: HijriConversionData
)

data class HijriConversionData(
    val hijri: HijriDateInfo,
    val gregorian: GregorianDateInfo? = null
)
