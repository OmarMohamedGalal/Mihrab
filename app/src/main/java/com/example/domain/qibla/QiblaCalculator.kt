package com.example.domain.qibla

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaCalculator {
    const val KAABA_LAT = 21.422487
    const val KAABA_LON = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0088

    /**
     * Exact great-circle forward azimuth from user's coordinates to the Kaaba in Makkah.
     * Returns bearing in degrees [0, 360) clockwise from True North.
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LON - longitude)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        return (Math.toDegrees(theta) + 360.0) % 360.0
    }

    /**
     * Haversine great-circle distance to the Kaaba in kilometers.
     */
    fun distanceToKaabaKm(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dPhi = Math.toRadians(KAABA_LAT - latitude)
        val dLambda = Math.toRadians(KAABA_LON - longitude)

        val a = sin(dPhi / 2.0) * sin(dPhi / 2.0) +
            cos(phi1) * cos(phi2) * sin(dLambda / 2.0) * sin(dLambda / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Signed shortest angular difference (-180..+180] from current heading to target Qibla bearing.
     * Positive means turn clockwise/right; negative means turn counter-clockwise/left.
     */
    fun shortestSignedAngleDiff(currentHeading: Double, targetBearing: Double): Double {
        var diff = (targetBearing - currentHeading + 540.0) % 360.0 - 180.0
        if (diff == -180.0) diff = 180.0
        return diff
    }

    /**
     * Generates intermediate spherical great-circle (lat, lon) points between user and Kaaba
     * for rendering the geodesic arc on the Qibla map view.
     */
    fun greatCirclePoints(
        startLat: Double,
        startLon: Double,
        steps: Int = 40
    ): List<Pair<Double, Double>> {
        val phi1 = Math.toRadians(startLat)
        val lam1 = Math.toRadians(startLon)
        val phi2 = Math.toRadians(KAABA_LAT)
        val lam2 = Math.toRadians(KAABA_LON)

        val x1 = cos(phi1) * cos(lam1)
        val y1 = cos(phi1) * sin(lam1)
        val z1 = sin(phi1)

        val x2 = cos(phi2) * cos(lam2)
        val y2 = cos(phi2) * sin(lam2)
        val z2 = sin(phi2)

        val dot = (x1 * x2 + y1 * y2 + z1 * z2).coerceIn(-1.0, 1.0)
        val omega = kotlin.math.acos(dot)
        if (omega < 1e-6) return listOf(startLat to startLon, KAABA_LAT to KAABA_LON)

        val sinOmega = sin(omega)
        return (0..steps).map { step ->
            val f = step.toDouble() / steps
            val a = sin((1.0 - f) * omega) / sinOmega
            val b = sin(f * omega) / sinOmega
            val x = a * x1 + b * x2
            val y = a * y1 + b * y2
            val z = a * z1 + b * z2
            val lat = Math.toDegrees(atan2(z, sqrt(x * x + y * y)))
            val lon = Math.toDegrees(atan2(y, x))
            lat to lon
        }
    }
}
