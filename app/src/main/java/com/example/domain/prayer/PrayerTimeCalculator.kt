package com.example.domain.prayer

import com.example.domain.calendar.HijriCalendarEngine
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Validated offline astronomical prayer times calculation engine using Jean Meeus solar coordinates.
 */
object PrayerTimeCalculator {

    data class Params(
        val latitude: Double,
        val longitude: Double,
        val elevationMeters: Double = 0.0,
        val method: CalculationMethod = CalculationMethod.MWL,
        val asrMethod: AsrMethod = AsrMethod.STANDARD,
        val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
        val offsets: PrayerOffsets = PrayerOffsets(),
        val imsakMinutesBeforeFajr: Int = 10,
        val hijriAdjustmentDays: Int = 0
    )

    fun calculate(
        date: LocalDate,
        zoneId: ZoneId,
        params: Params
    ): DailyPrayerTimes {
        val sampleNoon = ZonedDateTime.of(date, LocalTime.NOON, zoneId)
        val tzOffsetHours = sampleNoon.offset.totalSeconds / 3600.0
        val baseJd = julianDay(date.year, date.monthValue, date.dayOfMonth) - params.longitude / 360.0

        // Initial fractional day guesses (hours / 24)
        var fajrH = 5.0
        var sunriseH = 6.0
        var dhuhrH = 12.0
        var asrH = 15.0
        var sunsetH = 18.0
        var ishaH = 19.5

        val sunDepression = 0.8333 + 0.0347 * sqrt(params.elevationMeters.coerceAtLeast(0.0))

        // Two astronomical refinement passes
        repeat(2) {
            dhuhrH = computeMidday(baseJd + dhuhrH / 24.0, params.longitude, tzOffsetHours)
            sunriseH = computeSunTime(
                jd = baseJd + sunriseH / 24.0,
                latitude = params.latitude,
                longitude = params.longitude,
                tzOffsetHours = tzOffsetHours,
                angleDeg = sunDepression,
                isMorning = true
            ) ?: 6.0
            sunsetH = computeSunTime(
                jd = baseJd + sunsetH / 24.0,
                latitude = params.latitude,
                longitude = params.longitude,
                tzOffsetHours = tzOffsetHours,
                angleDeg = sunDepression,
                isMorning = false
            ) ?: 18.0

            fajrH = computeSunTime(
                jd = baseJd + fajrH / 24.0,
                latitude = params.latitude,
                longitude = params.longitude,
                tzOffsetHours = tzOffsetHours,
                angleDeg = params.method.fajrAngle,
                isMorning = true
            ) ?: Double.NaN

            asrH = computeAsrTime(
                jd = baseJd + asrH / 24.0,
                latitude = params.latitude,
                longitude = params.longitude,
                tzOffsetHours = tzOffsetHours,
                shadowFactor = params.asrMethod.shadowFactor
            )

            ishaH = if (params.method.ishaAngle != null) {
                computeSunTime(
                    jd = baseJd + ishaH / 24.0,
                    latitude = params.latitude,
                    longitude = params.longitude,
                    tzOffsetHours = tzOffsetHours,
                    angleDeg = params.method.ishaAngle,
                    isMorning = false
                ) ?: Double.NaN
            } else {
                Double.NaN
            }
        }

        // Apply High-Latitude adjustments when twilight fails or exceeds safe night portion
        val nightDuration = (24.0 - sunsetH) + sunriseH
        val rule = if (abs(params.latitude) > 48.0 || fajrH.isNaN() || (params.method.ishaAngle != null && ishaH.isNaN())) {
            params.highLatitudeRule
        } else {
            HighLatitudeRule.NONE
        }

        if (rule != HighLatitudeRule.NONE) {
            val fajrPortion = nightPortion(rule, params.method.fajrAngle) * nightDuration
            if (fajrH.isNaN() || (sunriseH - fajrH) > fajrPortion) {
                fajrH = sunriseH - fajrPortion
            }
            if (params.method.ishaAngle != null) {
                val ishaPortion = nightPortion(rule, params.method.ishaAngle) * nightDuration
                if (ishaH.isNaN() || (ishaH - sunsetH) > ishaPortion) {
                    ishaH = sunsetH + ishaPortion
                }
            }
        }

        // Fixed minute offsets for methods like Umm Al-Qura & Qatar
        val maghribH = sunsetH + params.method.maghribMinutesOffset / 60.0
        if (params.method.ishaAngle == null) {
            val baseMinutes = params.method.ishaMinutesAfterMaghrib ?: 90
            val hijriDate = HijriCalendarEngine.fromGregorian(date, params.hijriAdjustmentDays)
            val ishaOffsetMin = if (params.method.isRamadanExtendedIsha && hijriDate.month == 9) 120 else baseMinutes
            ishaH = maghribH + ishaOffsetMin / 60.0
        }

        val fajrZdt = hoursToZonedDateTime(date, zoneId, fajrH)
            .plusMinutes(params.offsets.fajr.toLong())
        val sunriseZdt = hoursToZonedDateTime(date, zoneId, sunriseH + params.method.sunriseMinutesOffset / 60.0)
            .plusMinutes(params.offsets.sunrise.toLong())
        val dhuhrZdt = hoursToZonedDateTime(date, zoneId, dhuhrH + params.method.dhuhrMinutesOffset / 60.0)
            .plusMinutes(params.offsets.dhuhr.toLong())
        val asrZdt = hoursToZonedDateTime(date, zoneId, asrH)
            .plusMinutes(params.offsets.asr.toLong())
        val maghribZdt = hoursToZonedDateTime(date, zoneId, maghribH)
            .plusMinutes(params.offsets.maghrib.toLong())
        val ishaZdt = hoursToZonedDateTime(date, zoneId, ishaH)
            .plusMinutes(params.offsets.isha.toLong())

        val imsakZdt = fajrZdt.minusMinutes(params.imsakMinutesBeforeFajr.toLong())
        val midnightSeconds = Duration.between(maghribZdt, sunriseZdt.plusDays(1)).seconds / 2
        val midnightZdt = maghribZdt.plusSeconds(midnightSeconds)

        return DailyPrayerTimes(
            date = date,
            fajr = fajrZdt,
            sunrise = sunriseZdt,
            dhuhr = dhuhrZdt,
            asr = asrZdt,
            maghrib = maghribZdt,
            isha = ishaZdt,
            imsak = imsakZdt,
            midnight = midnightZdt
        )
    }

    fun calculateMonth(
        year: Int,
        month: Int,
        zoneId: ZoneId,
        params: Params
    ): List<DailyPrayerTimes> {
        val firstDay = LocalDate.of(year, month, 1)
        val length = firstDay.lengthOfMonth()
        return (1..length).map { day ->
            calculate(LocalDate.of(year, month, day), zoneId, params)
        }
    }

    fun resolveNextPrayer(
        now: ZonedDateTime,
        today: DailyPrayerTimes,
        tomorrow: DailyPrayerTimes,
        yesterday: DailyPrayerTimes
    ): NextPrayerInfo {
        val upcomingToday = today.obligatoryEntries.firstOrNull { it.time.isAfter(now) }
        return if (upcomingToday != null) {
            val prevEntry = today.obligatoryEntries.lastOrNull { !it.time.isAfter(now) }
                ?: PrayerEntry(PrayerType.ISHA, yesterday.isha)
            val total = Duration.between(prevEntry.time, upcomingToday.time).seconds.coerceAtLeast(1L)
            val elapsed = Duration.between(prevEntry.time, now).seconds.coerceIn(0L, total)
            NextPrayerInfo(
                nextPrayer = upcomingToday.type,
                nextTime = upcomingToday.time,
                previousPrayer = prevEntry.type,
                previousTime = prevEntry.time,
                remaining = Duration.between(now, upcomingToday.time),
                progress = elapsed.toFloat() / total.toFloat()
            )
        } else {
            val nextTime = tomorrow.fajr
            val prevTime = today.isha
            val total = Duration.between(prevTime, nextTime).seconds.coerceAtLeast(1L)
            val elapsed = Duration.between(prevTime, now).seconds.coerceIn(0L, total)
            NextPrayerInfo(
                nextPrayer = PrayerType.FAJR,
                nextTime = nextTime,
                previousPrayer = PrayerType.ISHA,
                previousTime = prevTime,
                remaining = Duration.between(now, nextTime),
                progress = elapsed.toFloat() / total.toFloat()
            )
        }
    }

    private fun nightPortion(rule: HighLatitudeRule, angle: Double): Double = when (rule) {
        HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5
        HighLatitudeRule.ONE_SEVENTH -> 1.0 / 7.0
        HighLatitudeRule.ANGLE_BASED -> angle / 60.0
        HighLatitudeRule.NONE -> 0.5
    }

    private data class SunPosition(val declinationRad: Double, val equationOfTimeHours: Double)

    private fun sunPosition(jd: Double): SunPosition {
        val d = jd - 2451545.0
        val g = degToRad(fixAngle(357.529 + 0.98560028 * d))
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = degToRad(fixAngle(q + 1.915 * sin(g) + 0.020 * sin(2.0 * g)))
        val e = degToRad(23.439 - 0.00000036 * d)

        val ra = radToDeg(atan2(cos(e) * sin(l), cos(l))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = asin(sin(e) * sin(l))
        return SunPosition(decl, eqt)
    }

    private fun computeMidday(jd: Double, longitude: Double, tzOffsetHours: Double): Double {
        val (_, eqt) = sunPosition(jd)
        return fixHour(12.0 + tzOffsetHours - longitude / 15.0 - eqt)
    }

    private fun computeSunTime(
        jd: Double,
        latitude: Double,
        longitude: Double,
        tzOffsetHours: Double,
        angleDeg: Double,
        isMorning: Boolean
    ): Double? {
        val (decl, eqt) = sunPosition(jd)
        val midday = fixHour(12.0 + tzOffsetHours - longitude / 15.0 - eqt)
        val latRad = degToRad(latitude)
        val altRad = -degToRad(angleDeg)

        val cosHourAngle = (sin(altRad) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))
        if (cosHourAngle < -1.0 || cosHourAngle > 1.0) return null

        val hourAngle = radToDeg(acos(cosHourAngle)) / 15.0
        return if (isMorning) midday - hourAngle else midday + hourAngle
    }

    private fun computeAsrTime(
        jd: Double,
        latitude: Double,
        longitude: Double,
        tzOffsetHours: Double,
        shadowFactor: Double
    ): Double {
        val (decl, eqt) = sunPosition(jd)
        val midday = fixHour(12.0 + tzOffsetHours - longitude / 15.0 - eqt)
        val latRad = degToRad(latitude)
        val altRad = atan(1.0 / (shadowFactor + tan(abs(latRad - decl))))
        val cosHourAngle = (sin(altRad) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))
        val clamped = cosHourAngle.coerceIn(-1.0, 1.0)
        val hourAngle = radToDeg(acos(clamped)) / 15.0
        return midday + hourAngle
    }

    private fun hoursToZonedDateTime(date: LocalDate, zoneId: ZoneId, hours: Double): ZonedDateTime {
        val totalMinutes = (hours * 60.0).roundToLong()
        val startOfDay = date.atStartOfDay(zoneId)
        return startOfDay.plusMinutes(totalMinutes)
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun degToRad(deg: Double): Double = deg * PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / PI

    private fun fixAngle(a: Double): Double {
        var res = a % 360.0
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h % 24.0
        if (res < 0) res += 24.0
        return res
    }
}
