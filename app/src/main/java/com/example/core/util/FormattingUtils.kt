package com.example.core.util

import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object FormattingUtils {
    fun formatPrayerTime(time: ZonedDateTime, use24Hour: Boolean, isArabic: Boolean): String {
        val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun formatCountdown(duration: Duration): String {
        val totalSeconds = duration.seconds.coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatGregorianDate(date: LocalDate, isArabic: Boolean): String {
        val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
        val pattern = if (isArabic) "EEEE، d MMMM yyyy" else "EEE, d MMM yyyy"
        return date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    fun formatDegrees(deg: Double): String = "${deg.roundToInt()}°"

    fun formatDistanceKm(km: Double, unit: String): String {
        val formatted = if (km >= 1000) String.format(Locale.US, "%,.0f", km) else String.format(Locale.US, "%.1f", km)
        return "$formatted $unit"
    }
}
