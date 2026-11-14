package com.example.data.quran

object QuranScriptFormatter {

    private val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    // Matches any Unicode form of leading Bismillah (Uthmani, Simple, or custom tashkeel)
    private val leadingBismillahRegex = Regex(
        """^\s*ب[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*س[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*م[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s+""" +
            """[اٱأإ][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ه[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s+""" +
            """[اٱ][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ر[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ح[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*م[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ن[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s+""" +
            """[اٱ][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ل[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ر[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*ح[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*[يی][\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*م[\u064B-\u065F\u0670\u06D6-\u06ED\u0640]*\s*"""
    )

    fun toArabicIndic(number: Int): String {
        if (number <= 0) return arabicIndicDigits[0].toString()
        return number.toString().map { ch ->
            if (ch in '0'..'9') arabicIndicDigits[ch - '0'] else ch
        }.joinToString("")
    }

    fun hasSajdahMark(text: String): Boolean = text.contains('\u06E9')

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
}
