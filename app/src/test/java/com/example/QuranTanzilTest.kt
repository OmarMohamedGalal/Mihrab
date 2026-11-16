package com.example

import com.example.data.azkar.AzkarCategory
import com.example.data.azkar.AzkarRepository
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranRecitersCatalog
import com.example.data.quran.QuranScriptFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranTanzilTest {

    @Test
    fun arabicIndicNumeralsFormatAccurately() {
        assertEquals("١", QuranScriptFormatter.toArabicIndic(1))
        assertEquals("٢٨٦", QuranScriptFormatter.toArabicIndic(286))
        assertEquals("١١٤", QuranScriptFormatter.toArabicIndic(114))
    }

    @Test
    fun uthmaniToSimpleScriptNormalizesAlefWaslaAndWaqfMarks() {
        val uthmani = "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَٱسْتَغْفِرْهُ ۚ إِنَّهُۥ"
        val simple = QuranScriptFormatter.formatTanzilScript(uthmani, simple = true)
        assertFalse(simple.contains('\u0671'))
        assertFalse(simple.contains('\u06DA'))
        assertTrue(simple.contains("وَاسْتَغْفِرْهُ"))
        assertEquals(uthmani, QuranScriptFormatter.formatTanzilScript(uthmani, simple = false))
    }

    @Test
    fun stripLeadingBismillahRemovesFromSurahs2To114ExceptFatihahAndTawbah() {
        val baqarahRaw = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ الٓمٓ"
        assertEquals("الٓمٓ", QuranScriptFormatter.stripLeadingBismillah(2, 1, baqarahRaw))

        val simpleIkhlasRaw = "بسم الله الرحمن الرحيم قُلْ هُوَ اللَّهُ أَحَدٌ"
        assertEquals("قُلْ هُوَ اللَّهُ أَحَدٌ", QuranScriptFormatter.stripLeadingBismillah(112, 1, simpleIkhlasRaw))

        // Surah 1 Ayah 1 IS Bismillah — must stay intact
        val fatihah1 = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"
        assertEquals(fatihah1, QuranScriptFormatter.stripLeadingBismillah(1, 1, fatihah1))

        // Surah 9 Ayah 1 has no Bismillah
        val tawbah1 = "بَرَآءَةٌ مِّنَ ٱللَّهِ وَرَسُولِهِ"
        assertEquals(tawbah1, QuranScriptFormatter.stripLeadingBismillah(9, 1, tawbah1))

        // Non-1st Ayah (e.g. An-Naml 27:30) keeps Bismillah inside the verse
        val naml30 = "إِنَّهُۥ مِن سُلَيْمَـٰنَ وَإِنَّهُۥ بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"
        assertEquals(naml30, QuranScriptFormatter.stripLeadingBismillah(27, 30, naml30))
    }

    @Test
    fun medinaMushafMetadataCoversAll604PagesAnd114SurahsInOrder() {
        assertEquals(114, QuranMetadata.surahs.size)
        assertEquals(30, QuranMetadata.juzList.size)
        assertEquals(1, QuranMetadata.surahs.first().startPage)
        assertEquals(604, QuranMetadata.surahs.last().startPage)

        var prevPage = 0
        for (s in QuranMetadata.surahs) {
            assertTrue("Surah ${s.number} startPage out of bounds", s.startPage in 1..604)
            assertTrue("Surah ${s.number} page order regressed", s.startPage >= prevPage)
            prevPage = s.startPage
        }

        val page604Surahs = QuranMetadata.surahsOnPage(604).map { it.number }
        assertEquals(listOf(112, 113, 114), page604Surahs)
        assertEquals(30, QuranMetadata.juzForPage(604).number)
    }

    @Test
    fun completeAzkarCatalogCoversAllCategoriesAndTasbeehPresets() {
        assertTrue(AzkarRepository.tasbeehPresets.size >= 8)
        for (category in AzkarCategory.entries) {
            val count = AzkarRepository.azkarItems.count { it.category == category }
            assertTrue("Category $category should have at least 8 authentic items, had $count", count >= 8)
        }
    }

    @Test
    fun quranRecitersCatalogIncludesOver50ReadersAndIslamSobhi() {
        val reciters = QuranRecitersCatalog.reciters
        assertTrue("Expected at least 50 Quran reciters, got ${reciters.size}", reciters.size >= 50)
        assertEquals(reciters.size, reciters.map { it.id }.distinct().size)

        val islamSobhi = reciters.find { it.id == "ar.islamsobhi" || it.nameAr.contains("إسلام صبحي") }
        assertNotNull("Islam Sobhi (إسلام صبحي) must be included in reciters catalog", islamSobhi)
        assertEquals("إسلام صبحي", islamSobhi!!.nameAr)
        assertTrue(islamSobhi.surahMp3Url(1).endsWith("/001.mp3"))
        assertTrue(islamSobhi.surahMp3Url(114).endsWith("/114.mp3"))

        for (r in reciters) {
            assertTrue("Reciter ${r.id} URL invalid", r.surahMp3Url(18).startsWith("https://") && r.surahMp3Url(18).endsWith("/018.mp3"))
        }
    }

    @Test
    fun tajweedSpansHizbAndKhatmahProgressWorkAccurately() {
        val sample = "إِنَّا أَعْطَيْنَـٰكَ ٱلْكَوْثَرَ ۝ قُلْ هُوَ ٱللَّهُ أَحَدٌ"
        val spans = QuranScriptFormatter.findTajweedSpans(sample)
        assertTrue("Expected Tajweed spans in sample Quranic text", spans.isNotEmpty())
        assertTrue(spans.any { it.rule == com.example.data.quran.TajweedRule.GHUNNAH })
        assertTrue(spans.any { it.rule == com.example.data.quran.TajweedRule.MADD })

        assertEquals(1, QuranScriptFormatter.hizbForPage(1))
        assertEquals(60, QuranScriptFormatter.hizbForPage(604))
        assertTrue(QuranScriptFormatter.rubInHizbForPage(604) in 1..4)
        assertEquals(1, QuranScriptFormatter.manzilForSurah(1))
        assertEquals(7, QuranScriptFormatter.manzilForSurah(114))

        val khatmah = com.example.presentation.quran.KhatmahProgressState(
            readPages = setOf(1, 2, 3),
            targetDays = 30
        )
        assertEquals(3, khatmah.completedPagesCount)
        assertEquals(21, khatmah.dailyPagesGoal)
        assertEquals(4, khatmah.nextUnreadPage)
    }
}
