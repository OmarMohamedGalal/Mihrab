package com.example.data.quran

enum class TajweedRule(
    val labelAr: String,
    val labelEn: String
) {
    GHUNNAH("غنة", "Ghunnah"),
    QALQALAH("قلقلة", "Qalqalah"),
    MADD("مد", "Madd"),
    IKHFA("إخفاء / إدغام", "Ikhfa / Idgham")
}

data class TajweedSpan(
    val start: Int,
    val endExclusive: Int,
    val rule: TajweedRule
)

object QuranScriptFormatter {

    private val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    // Matches any Unicode form of leading Bismillah (Uthmani, Simple, or custom tashkeel)
    private val leadingBismillahRegex = Regex(
        """^\s*ب[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*س[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*م[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s+""" +
            """[اٱأإ][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ه[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s+""" +
            """[اٱ][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ر[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ح[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*م[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ن[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s+""" +
            """[اٱ][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ر[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ح[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*[يی][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*م[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s*"""
    )

    private val ghunnahRegex = Regex("""[نم][\u064B-\u065F]*\u0651[\u064B-\u065F\u0670]*""")
    private val qalqalahRegex = Regex("""[قطبجد][\u0652\u06E1]""")
    private val maddRegex = Regex("""(?:\u0622|[^\s][\u064B-\u065F]*(?:\u0653|\u0670)[\u064B-\u065F]*)""")
    private val ikhfaRegex = Regex("""(?:\u0646[\u0652\u06E1]?|[^\s][\u064B\u064C\u064D])""")

    fun toArabicIndic(number: Int): String {
        if (number <= 0) return arabicIndicDigits[0].toString()
        return number.toString().map { ch ->
            if (ch in '0'..'9') arabicIndicDigits[ch - '0'] else ch
        }.joinToString("")
    }

    fun hasSajdahMark(text: String): Boolean = text.contains('\u06E9')

    /**
     * Computes the Hizb number (1..60) for a given Madinah Mushaf page (1..604).
     */
    fun hizbForPage(pageNumber: Int): Int {
        val clamped = pageNumber.coerceIn(1, 604)
        val juz = QuranMetadata.juzForPage(clamped)
        val nextJuzStart = QuranMetadata.juzList.getOrNull(juz.number)?.startPage ?: 605
        val juzSpan = (nextJuzStart - juz.startPage).coerceAtLeast(1)
        val secondHalf = (clamped - juz.startPage) >= (juzSpan / 2)
        return ((juz.number - 1) * 2 + if (secondHalf) 2 else 1).coerceIn(1, 60)
    }

    /**
     * Computes the quarter (Rub' 1..4) within the Hizb for a given Madinah Mushaf page.
     */
    fun rubInHizbForPage(pageNumber: Int): Int {
        val clamped = pageNumber.coerceIn(1, 604)
        return (((clamped - 1) % 10) * 4 / 10 + 1).coerceIn(1, 4)
    }

    /**
     * Computes the classical 7-Manzil weekly division (1..7) for a given Surah.
     */
    fun manzilForSurah(surahNumber: Int): Int = when (surahNumber) {
        in 1..3 -> 1
        in 4..9 -> 2
        in 10..16 -> 3
        in 17..25 -> 4
        in 26..36 -> 5
        in 37..49 -> 6
        else -> 7
    }

    /**
     * Removes the prepended Bismillah from Ayah 1 of Surahs 2..114 (except Surah 9),
     * matching Mushaf Al-Madinah where Bismillah is a standalone header line, not part of Ayah 1.
     */
    fun stripLeadingBismillah(surahNumber: Int, ayahNumber: Int, text: String): String {
        if (surahNumber == 1 || surahNumber == 9 || ayahNumber != 1) return text
        val stripped = leadingBismillahRegex.replaceFirst(text, "").trimStart()
        return stripped.ifEmpty { text }
    }

    /**
     * Converts Tanzil Uthmani Unicode text to clean Simple / Uthmani-Minimal text offline,
     * preserving standard vowel tashkeel while removing Medina Mushaf pause/small-high glyphs.
     */
    fun formatTanzilScript(uthmaniText: String, simple: Boolean): String {
        if (!simple) return uthmaniText
        val sb = StringBuilder(uthmaniText.length)
        for (ch in uthmaniText) {
            when (ch) {
                '\u0671' -> sb.append('\u0627') // Alef Wasla -> standard Alef
                '\u06E1' -> sb.append('\u0652') // Small high dotless head of khah -> Sukun
                '\u0670' -> sb.append('\u0627') // Superscript Alef -> Alef
                '\u0640' -> Unit // Remove kashida/tatweel
                in '\u06D6'..'\u06ED' -> {
                    if (ch == '\u06E9') sb.append(' ').append(ch)
                }
                else -> sb.append(ch)
            }
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Identifies non-overlapping Tajweed rule spans (Ghunnah, Qalqalah, Madd, Ikhfa/Idgham)
     * in Quranic Arabic text for color-coded Mushaf rendering.
     */
    fun findTajweedSpans(text: String): List<TajweedSpan> {
        if (text.isEmpty()) return emptyList()
        val raw = mutableListOf<TajweedSpan>()
        ghunnahRegex.findAll(text).forEach { m ->
            raw += TajweedSpan(m.range.first, m.range.last + 1, TajweedRule.GHUNNAH)
        }
        qalqalahRegex.findAll(text).forEach { m ->
            raw += TajweedSpan(m.range.first, m.range.last + 1, TajweedRule.QALQALAH)
        }
        maddRegex.findAll(text).forEach { m ->
            raw += TajweedSpan(m.range.first, m.range.last + 1, TajweedRule.MADD)
        }
        ikhfaRegex.findAll(text).forEach { m ->
            raw += TajweedSpan(m.range.first, m.range.last + 1, TajweedRule.IKHFA)
        }
        if (raw.isEmpty()) return emptyList()
        val sorted = raw.sortedWith(compareBy<TajweedSpan> { it.start }.thenBy { it.rule.ordinal })
        val merged = mutableListOf<TajweedSpan>()
        var lastEnd = 0
        for (span in sorted) {
            if (span.start >= lastEnd && span.endExclusive <= text.length) {
                merged += span
                lastEnd = span.endExclusive
            }
        }
        return merged
    }
}
