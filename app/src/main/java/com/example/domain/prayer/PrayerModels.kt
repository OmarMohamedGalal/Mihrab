package com.example.domain.prayer

import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

enum class PrayerType(val isObligatory: Boolean) {
    FAJR(true),
    SUNRISE(false),
    DHUHR(true),
    ASR(true),
    MAGHRIB(true),
    ISHA(true);

    companion object {
        val obligatoryPrayers: List<PrayerType> = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
    }
}

enum class CalculationMethod(
    val displayNameEn: String,
    val displayNameAr: String,
    val fajrAngle: Double,
    val ishaAngle: Double?,
    val ishaMinutesAfterMaghrib: Int? = null,
    val maghribMinutesOffset: Int = 0,
    val dhuhrMinutesOffset: Int = 0,
    val sunriseMinutesOffset: Int = 0,
    val isRamadanExtendedIsha: Boolean = false
) {
    MWL(
        displayNameEn = "Muslim World League",
        displayNameAr = "رابطة العالم الإسلامي",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    EGYPT(
        displayNameEn = "Egyptian General Authority",
        displayNameAr = "الهيئة المصرية العامة للمساحة",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    UMM_AL_QURA(
        displayNameEn = "Umm Al-Qura University, Makkah",
        displayNameAr = "جامعة أم القرى - مكة المكرمة",
        fajrAngle = 18.5,
        ishaAngle = null,
        ishaMinutesAfterMaghrib = 90,
        isRamadanExtendedIsha = true
    ),
    KARACHI(
        displayNameEn = "Univ. of Islamic Sciences, Karachi",
        displayNameAr = "جامعة العلوم الإسلامية - كراتشي",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    ISNA(
        displayNameEn = "ISNA (North America)",
        displayNameAr = "الجمعية الإسلامية لأمريكا الشمالية",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    DUBAI(
        displayNameEn = "Dubai (UAE)",
        displayNameAr = "دائرة الشؤون الإسلامية - دبي",
        fajrAngle = 18.2,
        ishaAngle = 18.2,
        maghribMinutesOffset = 3,
        dhuhrMinutesOffset = 3,
        sunriseMinutesOffset = -3
    ),
    QATAR(
        displayNameEn = "Qatar",
        displayNameAr = "التقويم القطري",
        fajrAngle = 18.0,
        ishaAngle = null,
        ishaMinutesAfterMaghrib = 90
    ),
    KUWAIT(
        displayNameEn = "Kuwait",
        displayNameAr = "وزارة الأوقاف - الكويت",
        fajrAngle = 18.0,
        ishaAngle = 17.5
    ),
    SINGAPORE(
        displayNameEn = "Singapore (MUIS)",
        displayNameAr = "سنغافورة",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    ),
    TURKEY(
        displayNameEn = "Turkey (Diyanet)",
        displayNameAr = "رئاسة الشؤون الدينية التركية",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    UOIF(
        displayNameEn = "UOIF (France)",
        displayNameAr = "اتحاد المنظمات الإسلامية - فرنسا",
        fajrAngle = 12.0,
        ishaAngle = 12.0
    ),
    MOONSIGHTING(
        displayNameEn = "Moonsighting Committee",
        displayNameAr = "لجنة رؤية الهلال",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    )
}

enum class AsrMethod(val displayNameEn: String, val displayNameAr: String, val shadowFactor: Double) {
    STANDARD("Standard (Shafi'i, Maliki, Hanbali)", "الجمهور (الشافعي والمالكي والحنبلي)", 1.0),
    HANAFI("Hanafi", "الحنفي", 2.0)
}

enum class HighLatitudeRule(val displayNameEn: String, val displayNameAr: String) {
    MIDDLE_OF_NIGHT("Middle of the Night", "منتصف الليل"),
    ONE_SEVENTH("One-Seventh of the Night", "سُبع الليل"),
    ANGLE_BASED("Angle-Based", "حسب الزاوية"),
    NONE("None", "بدون تعديل")
}

data class PrayerOffsets(
    val fajr: Int = 0,
    val sunrise: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0
) {
    fun offsetFor(type: PrayerType): Int = when (type) {
        PrayerType.FAJR -> fajr
        PrayerType.SUNRISE -> sunrise
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }
}

data class PrayerEntry(
    val type: PrayerType,
    val time: ZonedDateTime
)

data class DailyPrayerTimes(
    val date: LocalDate,
    val fajr: ZonedDateTime,
    val sunrise: ZonedDateTime,
    val dhuhr: ZonedDateTime,
    val asr: ZonedDateTime,
    val maghrib: ZonedDateTime,
    val isha: ZonedDateTime,
    val imsak: ZonedDateTime,
    val midnight: ZonedDateTime
) {
    fun timeFor(type: PrayerType): ZonedDateTime = when (type) {
        PrayerType.FAJR -> fajr
        PrayerType.SUNRISE -> sunrise
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }

    val allEntries: List<PrayerEntry>
        get() = listOf(
            PrayerEntry(PrayerType.FAJR, fajr),
            PrayerEntry(PrayerType.SUNRISE, sunrise),
            PrayerEntry(PrayerType.DHUHR, dhuhr),
            PrayerEntry(PrayerType.ASR, asr),
            PrayerEntry(PrayerType.MAGHRIB, maghrib),
            PrayerEntry(PrayerType.ISHA, isha)
        )

    val obligatoryEntries: List<PrayerEntry>
        get() = listOf(
            PrayerEntry(PrayerType.FAJR, fajr),
            PrayerEntry(PrayerType.DHUHR, dhuhr),
            PrayerEntry(PrayerType.ASR, asr),
            PrayerEntry(PrayerType.MAGHRIB, maghrib),
            PrayerEntry(PrayerType.ISHA, isha)
        )
}

data class NextPrayerInfo(
    val nextPrayer: PrayerType,
    val nextTime: ZonedDateTime,
    val previousPrayer: PrayerType,
    val previousTime: ZonedDateTime,
    val remaining: Duration,
    val progress: Float
)
