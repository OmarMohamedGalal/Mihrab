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
        assertEquals(7, AzkarCategory.entries.size)
        assertTrue(AzkarRepository.azkarItems.size >= 80)
        for (category in AzkarCategory.entries) {
            val items = AzkarRepository.azkarItems.filter { it.category == category }
            assertTrue("Category $category should have at least 4 authentic items, had ${items.size}", items.size >= 4)
            for (item in items) {
                assertTrue("Azkar ${item.id} missing titleAr", item.titleAr.isNotBlank())
                assertTrue("Azkar ${item.id} must not use ... truncation", !item.arabicText.contains("..."))
                assertTrue("Azkar ${item.id} must have positive repeatTarget", item.repeatTarget >= 1)
            }
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

    @Test
    fun quranComLineLayoutParsesLinesAndStarterPagesOneAndTwoAccurately() {
        val page1 = com.example.data.quran.QuranComLineRepository.bundledStarterLineLayout(1)
        assertNotNull(page1)
        assertEquals(1, page1!!.pageNumber)
        assertEquals(7, page1.lines.size)
        assertTrue(page1.lines.first().words.any { it.isEndMarker && it.text.contains('\u06DD') })

        val page2 = com.example.data.quran.QuranComLineRepository.bundledStarterLineLayout(2)
        assertNotNull(page2)
        assertEquals(2, page2!!.pageNumber)
        assertEquals(6, page2.lines.size)
        assertTrue(page2.lines.first().words.any { it.surahNumber == 2 && it.ayahNumber == 1 })

        // Verify simple script preserves عَلَىٰ / ٱلصَّلَوٰةَ without inserting spurious extra alef
        val baqarahSample = "أُو۟لَـٰٓئِكَ عَلَىٰ هُدًى ۖ وَيُقِيمُونَ ٱلصَّلَوٰةَ"
        val cleanSimple = QuranScriptFormatter.formatTanzilScript(baqarahSample, simple = true)
        assertFalse(cleanSimple.contains("علىا"))
        assertFalse(cleanSimple.contains("الصلواة"))
        assertFalse(cleanSimple.contains('\u06D6'))

        // Verify HTML footnotes stripped cleanly from translations
        val cleanEn = QuranScriptFormatter.stripTranslationHtml(
            "This is the Book<sup foot_note=\"19592\">1</sup> about which there is no doubt."
        )
        assertEquals("This is the Book about which there is no doubt.", cleanEn)
    }

    @Test
    fun ksuAyatTafsirAndHusaryDefaultsWorkAccurately() {
        // Verify Al-Baqarah 2:6 is seeded offline on Page 3
        val baqarah6 = QuranMetadata.bundledStarterVerses.find { it.surahNumber == 2 && it.ayahNumber == 6 }
        assertNotNull("Al-Baqarah 2:6 should be bundled for offline KSU #aya=2_6 access", baqarah6)
        assertEquals(3, baqarah6!!.pageNumber)

        // Verify KSU Tafsir Al-Muyassar (ar_mu) is bundled for 2:6
        val muyassar26 = com.example.data.quran.QuranTafsirRepository.bundledMuyassarStarter["2:6"]
        assertNotNull("Tafsir Al-Muyassar for 2:6 should be bundled offline", muyassar26)
        assertTrue(muyassar26!!.contains("جحدوا"))

        // Verify KSU Tafsir edition IDs
        assertEquals("ar_mu", com.example.data.quran.KsuTafsirEdition.AR_MUYASSAR.ksuCode)
        assertEquals(16, com.example.data.quran.KsuTafsirEdition.AR_MUYASSAR.quranComTafsirId)
        assertEquals(1, com.example.data.quran.KsuTafsirEdition.AR_MUYASSAR.mirrorTafseerId)

        // Verify Al-Husary (qaree=husary) is the default reciter
        val prefs = com.example.data.preferences.UserPreferences()
        assertEquals("ar.husary", prefs.selectedReciterId)
        val husary = QuranRecitersCatalog.reciterById("ar.husary")
        assertEquals("محمود خليل الحصري", husary.nameAr)
    }

    @Test
    fun quranFlashMedina1PageUrlsAndImageValidationWorkAccurately() {
        val urls1 = com.example.data.quran.QuranFlashPageRepository.medina1UrlCandidates(1)
        assertEquals("https://app.quranflash.com/book/Medina1/epub/EPUB/imgs/0001.gif", urls1.first())
        assertTrue(urls1.contains("https://www.quranflash.com/books/Medina1/data/N/0001.gif"))

        val urls604 = com.example.data.quran.QuranFlashPageRepository.medina1UrlCandidates(604)
        assertEquals("https://app.quranflash.com/book/Medina1/epub/EPUB/imgs/0604.gif", urls604.first())

        val gifHeader = byteArrayOf('G'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), '8'.code.toByte(), '9'.code.toByte(), 'a'.code.toByte(), 0, 0, 0, 0, 0, 0)
        val htmlHeader = "<!DOCTYPE html><html></html>".toByteArray()
        assertTrue(com.example.data.quran.QuranFlashPageRepository.looksLikeValidImage(gifHeader))
        assertFalse(com.example.data.quran.QuranFlashPageRepository.looksLikeValidImage(htmlHeader))
    }
}


