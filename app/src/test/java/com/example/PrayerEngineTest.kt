package com.example

import com.example.domain.calendar.HijriCalendarEngine
import com.example.domain.prayer.AsrMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.HighLatitudeRule
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.qibla.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

class PrayerEngineTest {

    @Test
    fun dailyPrayerOrderingIsChronologicalForMakkahAndLondon() {
        val makkah = PrayerTimeCalculator.calculate(
            date = LocalDate.of(2025, 3, 15),
            zoneId = ZoneId.of("Asia/Riyadh"),
            params = PrayerTimeCalculator.Params(
                latitude = 21.4225,
                longitude = 39.8262,
                method = CalculationMethod.UMM_AL_QURA
            )
        )
        assertTrue(makkah.fajr.isBefore(makkah.sunrise))
        assertTrue(makkah.sunrise.isBefore(makkah.dhuhr))
        assertTrue(makkah.dhuhr.isBefore(makkah.asr))
        assertTrue(makkah.asr.isBefore(makkah.maghrib))
        assertTrue(makkah.maghrib.isBefore(makkah.isha))

        val london = PrayerTimeCalculator.calculate(
            date = LocalDate.of(2025, 6, 21),
            zoneId = ZoneId.of("Europe/London"),
            params = PrayerTimeCalculator.Params(
                latitude = 51.5074,
                longitude = -0.1278,
                method = CalculationMethod.MWL,
                highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
            )
        )
        assertTrue(london.fajr.isBefore(london.sunrise))
        assertTrue(london.sunrise.isBefore(london.dhuhr))
        assertTrue(london.dhuhr.isBefore(london.asr))
        assertTrue(london.asr.isBefore(london.maghrib))
        assertTrue(london.maghrib.isBefore(london.isha))
    }

    @Test
    fun hanafiAsrIsLaterThanStandardAsr() {
        val date = LocalDate.of(2025, 4, 10)
        val zone = ZoneId.of("Asia/Karachi")
        val std = PrayerTimeCalculator.calculate(
            date, zone,
            PrayerTimeCalculator.Params(24.8607, 67.0011, asrMethod = AsrMethod.STANDARD)
        )
        val hanafi = PrayerTimeCalculator.calculate(
            date, zone,
            PrayerTimeCalculator.Params(24.8607, 67.0011, asrMethod = AsrMethod.HANAFI)
        )
        assertTrue(hanafi.asr.isAfter(std.asr))
    }

    @Test
    fun ummAlQuraExtendsIshaInRamadanTo120Minutes() {
        val zone = ZoneId.of("Asia/Riyadh")
        val ramadanDate = LocalDate.of(2025, 3, 10) // 10 Ramadan 1446
        val shawwalDate = LocalDate.of(2025, 4, 10) // 12 Shawwal 1446
        val params = PrayerTimeCalculator.Params(21.4225, 39.8262, method = CalculationMethod.UMM_AL_QURA)

        val ramadanTimes = PrayerTimeCalculator.calculate(ramadanDate, zone, params)
        val normalTimes = PrayerTimeCalculator.calculate(shawwalDate, zone, params)

        assertEquals(120L, Duration.between(ramadanTimes.maghrib, ramadanTimes.isha).toMinutes())
        assertEquals(90L, Duration.between(normalTimes.maghrib, normalTimes.isha).toMinutes())
    }

    @Test
    fun hijriConversionRoundtripsAccurately() {
        val sample = LocalDate.of(2025, 3, 1)
        val hijri = HijriCalendarEngine.fromGregorian(sample)
        val back = HijriCalendarEngine.toGregorian(hijri)
        assertEquals(sample, back)
    }

    @Test
    fun qiblaBearingAndWraparoundMatchExpectedAzimuths() {
        val nycBearing = QiblaCalculator.calculateQiblaBearing(40.7128, -74.0060)
        assertEquals(58.5, nycBearing, 1.0)

        val londonBearing = QiblaCalculator.calculateQiblaBearing(51.5074, -0.1278)
        assertEquals(119.0, londonBearing, 1.0)

        val jakartaBearing = QiblaCalculator.calculateQiblaBearing(-6.2088, 106.8456)
        assertEquals(295.1, jakartaBearing, 1.0)

        val diffWrap = QiblaCalculator.shortestSignedAngleDiff(355.0, 5.0)
        assertEquals(10.0, diffWrap, 0.001)
    }
}
