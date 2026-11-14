package com.example.domain.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor

data class HijriDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..30
) {
    fun formatted(isArabic: Boolean): String {
        val monthName = HijriCalendarEngine.monthName(month, isArabic)
        return if (isArabic) "$day $monthName $year هـ" else "$day $monthName $year AH"
    }
}

data class IslamicOccasion(
    val hijriMonth: Int,
    val hijriDay: Int,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String
)

data class UpcomingOccasion(
    val occasion: IslamicOccasion,
    val gregorianDate: LocalDate,
    val hijriYear: Int,
    val daysUntil: Long
)

/**
 * Kuwaiti / Astronomical Tabular Hijri Calendar Engine with user offset (-2..+2 days),
 * Islamic occasions list, Ramadan countdown, and Jumu'ah detection.
 */
object HijriCalendarEngine {

    private val monthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val monthsAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val islamicOccasions = listOf(
        IslamicOccasion(1, 1, "Islamic New Year", "رأس السنة الهجرية", "1 Muharram — Beginning of the Hijri year", "١ محرم — بداية العام الهجري الجديد"),
        IslamicOccasion(1, 10, "Day of Ashura", "يوم عاشوراء", "10 Muharram — Recommended day of fasting", "١٠ محرم — يوم صيام مستحب"),
        IslamicOccasion(3, 12, "Mawlid al-Nabi", "المولد النبوي الشريف", "12 Rabi' al-Awwal", "١٢ ربيع الأول"),
        IslamicOccasion(7, 27, "Isra' & Mi'raj", "الإسراء والمعراج", "27 Rajab — Night Journey and Ascension", "٢٧ رجب — ذكرى الإسراء والمعراج"),
        IslamicOccasion(8, 15, "Mid-Sha'ban", "ليلة النصف من شعبان", "15 Sha'ban", "١٥ شعبان"),
        IslamicOccasion(9, 1, "Start of Ramadan", "أول أيام رمضان", "1 Ramadan — Month of fasting & Quran", "١ رمضان — شهر الصيام والقرآن"),
        IslamicOccasion(9, 27, "Laylat al-Qadr (Observed)", "ليلة القدر (المرجوة)", "27 Ramadan — Seek in the odd nights of the last ten", "٢٧ رمضان — تُلتمس في العشر الأواخر"),
        IslamicOccasion(10, 1, "Eid al-Fitr", "عيد الفطر المبارك", "1 Shawwal — Festival of breaking the fast", "١ شوال — عيد الفطر السعيد"),
        IslamicOccasion(12, 9, "Day of Arafah", "يوم عرفة", "9 Dhu al-Hijjah — Best day of the year", "٩ ذو الحجة — خير أيام الدنيا"),
        IslamicOccasion(12, 10, "Eid al-Adha", "عيد الأضحى المبارك", "10 Dhu al-Hijjah — Festival of Sacrifice", "١٠ ذو الحجة — عيد الأضحى المبارك")
    )

    fun monthName(month: Int, isArabic: Boolean): String {
        val idx = (month - 1).coerceIn(0, 11)
        return if (isArabic) monthsAr[idx] else monthsEn[idx]
    }

    fun fromGregorian(date: LocalDate, adjustmentDays: Int = 0): HijriDate {
        val adjusted = date.plusDays(adjustmentDays.toLong())
        val jd = gregorianToJulianDay(adjusted.year, adjusted.monthValue, adjusted.dayOfMonth)
        return julianDayToHijri(jd)
    }

    fun toGregorian(hijri: HijriDate, adjustmentDays: Int = 0): LocalDate {
        val jd = hijriToJulianDay(hijri.year, hijri.month, hijri.day)
        return julianDayToGregorian(jd).minusDays(adjustmentDays.toLong())
    }

    fun daysInHijriMonth(year: Int, month: Int): Int {
        // Odd months 30, even months 29, 12th month 30 in leap year
        return if (month % 2 == 1) 30
        else if (month == 12 && isHijriLeapYear(year)) 30
        else 29
    }

    fun isHijriLeapYear(year: Int): Boolean {
        return ((11 * year + 14) % 30) < 11
    }

    fun isJumuah(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.FRIDAY

    fun daysUntilRamadan(today: LocalDate, adjustmentDays: Int = 0): Long {
        val h = fromGregorian(today, adjustmentDays)
        if (h.month == 9) return 0L
        val targetYear = if (h.month < 9) h.year else h.year + 1
        val ramadanStart = toGregorian(HijriDate(targetYear, 9, 1), adjustmentDays)
        return ChronoUnit.DAYS.between(today, ramadanStart).coerceAtLeast(0L)
    }

    fun upcomingOccasions(today: LocalDate, adjustmentDays: Int = 0): List<UpcomingOccasion> {
        val hToday = fromGregorian(today, adjustmentDays)
        return islamicOccasions.map { occ ->
            val candidateThisYear = toGregorian(HijriDate(hToday.year, occ.hijriMonth, occ.hijriDay), adjustmentDays)
            val (targetYear, gDate) = if (!candidateThisYear.isBefore(today)) {
                hToday.year to candidateThisYear
            } else {
                (hToday.year + 1) to toGregorian(HijriDate(hToday.year + 1, occ.hijriMonth, occ.hijriDay), adjustmentDays)
            }
            UpcomingOccasion(
                occasion = occ,
                gregorianDate = gDate,
                hijriYear = targetYear,
                daysUntil = ChronoUnit.DAYS.between(today, gDate)
            )
        }.sortedBy { it.daysUntil }
    }

    // Kuwaiti / Astronomical arithmetic Hijri conversion
    private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun julianDayToGregorian(jd: Int): LocalDate {
        val a = jd + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = 100 * b + d - 4800 + m / 10
        return LocalDate.of(year, month, day)
    }

    private fun julianDayToHijri(jd: Int): HijriDate {
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        val l2 = l - 10631 * n + 354
        val j = (floor((10985 - l2) / 5316.0) * floor((50 * l2) / 17719.0) +
            floor(l2 / 5670.0) * floor((43 * l2) / 15238.0)).toInt()
        val l3 = l2 - (floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) +
            floor(j / 16.0) * floor((15238 * j) / 43.0)).toInt() + 29
        val month = floor((24 * l3) / 709.0).toInt()
        val day = l3 - floor((709 * month) / 24.0).toInt()
        val year = 30 * n + j - 30
        return HijriDate(year = year, month = month.coerceIn(1, 12), day = day.coerceIn(1, 30))
    }

    private fun hijriToJulianDay(year: Int, month: Int, day: Int): Int {
        return ((11 * year + 3) / 30) + 354 * year + 30 * month - ((month - 1) / 2) + day + 1948440 - 385
    }
}
