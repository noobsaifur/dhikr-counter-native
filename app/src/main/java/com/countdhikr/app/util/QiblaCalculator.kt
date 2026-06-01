package com.countdhikr.app.util

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaCalculator {

    const val KAABA_LAT = 21.4225
    const val KAABA_LNG = 39.8262

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the Qibla direction (bearing) from the user's location to the Kaaba.
     *
     * Uses the forward azimuth of the great-circle path.
     *
     * @param userLat User's latitude in degrees.
     * @param userLng User's longitude in degrees.
     * @return Bearing in degrees, normalized to 0–360.
     */
    fun calculateQiblaDirection(userLat: Double, userLng: Double): Double {
        val lat1 = userLat.toRadians()
        val lat2 = KAABA_LAT.toRadians()
        val dLng = (KAABA_LNG - userLng).toRadians()

        val x = sin(dLng) * cos(lat2)
        val y = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)

        val bearing = atan2(x, y).toDegrees()
        return (bearing + 360.0) % 360.0
    }

    /**
     * Calculates the distance from the user's location to the Kaaba using the Haversine formula.
     *
     * @param userLat User's latitude in degrees.
     * @param userLng User's longitude in degrees.
     * @return Distance in kilometres.
     */
    fun calculateDistanceToKaaba(userLat: Double, userLng: Double): Double {
        val dLat = (KAABA_LAT - userLat).toRadians()
        val dLng = (KAABA_LNG - userLng).toRadians()

        val lat1 = userLat.toRadians()
        val lat2 = KAABA_LAT.toRadians()

        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS_KM * c
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
    private fun Double.toDegrees(): Double = this * 180.0 / PI
}
