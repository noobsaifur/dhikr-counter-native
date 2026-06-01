package com.countdhikr.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// ── Retrofit Service ─────────────────────────────────────────────────────────

interface GeocodingApi {

    /**
     * Reverse-geocode a latitude/longitude pair into a human-readable location.
     *
     * This is a free, no-API-key endpoint from BigDataCloud.
     *
     * @param latitude         GPS latitude
     * @param longitude        GPS longitude
     * @param localityLanguage Language code for the result (default "en")
     */
    @GET("/data/reverse-geocode-client")
    suspend fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("localityLanguage") localityLanguage: String = "en"
    ): ReverseGeocodeResponse
}

// ── Response DTO ─────────────────────────────────────────────────────────────

data class ReverseGeocodeResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val continent: String? = null,
    @SerializedName("lookupSource") val lookupSource: String? = null,
    @SerializedName("continentCode") val continentCode: String? = null,
    @SerializedName("localityLanguageRequested") val localityLanguageRequested: String? = null,
    val city: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("countryCode") val countryCode: String? = null,
    val postcode: String? = null,
    @SerializedName("principalSubdivision") val principalSubdivision: String? = null,
    @SerializedName("principalSubdivisionCode") val principalSubdivisionCode: String? = null,
    val locality: String? = null,
    @SerializedName("localityInfo") val localityInfo: LocalityInfo? = null
)

data class LocalityInfo(
    val administrative: List<LocalityAdminInfo>? = null,
    val informative: List<LocalityAdminInfo>? = null
)

data class LocalityAdminInfo(
    val name: String? = null,
    val description: String? = null,
    val order: Int? = null,
    @SerializedName("isoName") val isoName: String? = null,
    @SerializedName("isoCode") val isoCode: String? = null,
    @SerializedName("wikidataId") val wikidataId: String? = null,
    @SerializedName("geonameId") val geonameId: Long? = null
)
