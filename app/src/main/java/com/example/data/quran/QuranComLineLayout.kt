package com.example.data.quran

import android.content.Context
import com.example.data.local.QuranVerseEntity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File

@JsonClass(generateAdapter = true)
data class QuranComPageResponse(
    val verses: List<QuranComVerseDto> = emptyList(),
    val pagination: QuranComPaginationDto? = null
)

@JsonClass(generateAdapter = true)
data class QuranComPaginationDto(
    val per_page: Int = 50,
    val current_page: Int = 1,
    val next_page: Int? = null,
    val total_pages: Int = 1,
    val total_records: Int = 0
)

@JsonClass(generateAdapter = true)
data class QuranComVerseDto(
    val id: Int = 0,
    val verse_number: Int = 0,
    val verse_key: String = "",
    val juz_number: Int = 1,
    val page_number: Int = 1,
    val text_uthmani: String? = null,
    val words: List<QuranComWordDto> = emptyList(),
    val translations: List<QuranComTranslationDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuranComTranslationDto(
    val resource_id: Int = 0,
    val text: String = ""
)

@JsonClass(generateAdapter = true)
data class QuranComWordDto(
    val position: Int = 0,
    val char_type_name: String = "word",
    val text_uthmani: String? = null,
    val line_number: Int? = null,
    val page_number: Int? = null
)

@JsonClass(generateAdapter = true)
data class MushafLineWord(
    val surahNumber: Int,
    val ayahNumber: Int,
    val text: String,
    val isEndMarker: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MushafLine(
    val lineNumber: Int,
    val words: List<MushafLineWord>
)

@JsonClass(generateAdapter = true)
data class MushafPageLineLayout(
    val pageNumber: Int,
    val lines: List<MushafLine>
)

data class QuranComFetchResult(
    val verses: List<QuranVerseEntity>,
    val pageLayouts: Map<Int, MushafPageLineLayout>
)

interface QuranComV4Api {
    @GET("api/v4/verses/by_page/{pageNumber}")
    suspend fun getVersesByPageWithWords(
        @Path("pageNumber") pageNumber: Int,
        @Query("words") words: Boolean = true,
        @Query("per_page") perPage: Int = 50,
        @Query("translations") translations: String = "20",
        @Query("fields") fields: String = "text_uthmani,page_number,juz_number,verse_number",
        @Query("word_fields") wordFields: String = "text_uthmani,line_number,page_number"
    ): QuranComPageResponse

    @GET("api/v4/verses/by_chapter/{chapterNumber}")
    suspend fun getVersesByChapterWithWords(
        @Path("chapterNumber") chapterNumber: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50,
        @Query("words") words: Boolean = true,
        @Query("translations") translations: String = "20",
        @Query("fields") fields: String = "text_uthmani,page_number,juz_number,verse_number",
        @Query("word_fields") wordFields: String = "text_uthmani,line_number,page_number"
    ): QuranComPageResponse
}

class QuranComLineRepository(
    appContext: Context,
    private val api: QuranComV4Api,
    private val moshi: Moshi
) {
    private val cacheDir = File(appContext.filesDir, "quran_com_lines_v2").apply { mkdirs() }
    private val pageAdapter by lazy { moshi.adapter(MushafPageLineLayout::class.java) }

    suspend fun getOrFetchPageLines(pageNumber: Int): MushafPageLineLayout? = withContext(Dispatchers.IO) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        readCachedPageLayout(clamped)?.let { return@withContext it }

        val fetched = runCatching {
            val response = api.getVersesByPageWithWords(clamped)
            extractPageLayouts(response.verses)[clamped]
        }.getOrNull()

        if (fetched != null && fetched.lines.isNotEmpty()) {
            writeCachedPageLayout(fetched)
            return@withContext fetched
        }

        bundledStarterLineLayout(clamped)
    }

    suspend fun fetchPageFromQuranCom(pageNumber: Int): Result<QuranComFetchResult> = withContext(Dispatchers.IO) {
        runCatching {
            val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
            val allDtos = mutableListOf<QuranComVerseDto>()
            var pageIdx = 1
            while (true) {
                val resp = api.getVersesByPageWithWords(pageNumber = clamped)
                allDtos += resp.verses
                val next = resp.pagination?.next_page
                if (next == null || next <= pageIdx || resp.verses.isEmpty()) break
                pageIdx = next
            }
            if (allDtos.isEmpty()) error("Empty Quran.com page $clamped response")
            val entities = toVerseEntities(allDtos)
            val layouts = extractPageLayouts(allDtos)
            layouts.values.forEach { writeCachedPageLayout(it) }
            QuranComFetchResult(verses = entities, pageLayouts = layouts)
        }
    }

    suspend fun fetchChapterFromQuranCom(surahNumber: Int): Result<QuranComFetchResult> = withContext(Dispatchers.IO) {
        runCatching {
            val allDtos = mutableListOf<QuranComVerseDto>()
            var pageIdx = 1
            while (true) {
                val resp = api.getVersesByChapterWithWords(
                    chapterNumber = surahNumber,
                    page = pageIdx,
                    perPage = 50
                )
                allDtos += resp.verses
                val totalPages = resp.pagination?.total_pages ?: 1
                val next = resp.pagination?.next_page
                if (next == null || pageIdx >= totalPages || resp.verses.isEmpty()) break
                pageIdx = next
            }
            if (allDtos.isEmpty()) error("Empty Quran.com chapter $surahNumber response")
            val entities = toVerseEntities(allDtos)
            val layouts = extractPageLayouts(allDtos)
            layouts.values.forEach { writeCachedPageLayout(it) }
            QuranComFetchResult(verses = entities, pageLayouts = layouts)
        }
    }

    internal fun toVerseEntities(dtos: List<QuranComVerseDto>): List<QuranVerseEntity> {
        return dtos.map { dto ->
            val parts = dto.verse_key.split(':')
            val surah = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val ayah = parts.getOrNull(1)?.toIntOrNull() ?: dto.verse_number.coerceAtLeast(1)
            val rawArabic = dto.text_uthmani?.takeIf { it.isNotBlank() }
                ?: dto.words
                    .filter { !it.char_type_name.equals("end", ignoreCase = true) }
                    .joinToString(" ") { it.text_uthmani.orEmpty() }
                    .trim()
            val cleanArabic = QuranScriptFormatter.stripLeadingBismillah(surah, ayah, rawArabic)
            val rawEn = dto.translations.firstOrNull()?.text.orEmpty()
            val cleanEn = QuranScriptFormatter.stripTranslationHtml(rawEn)

            QuranVerseEntity(
                globalNumber = if (dto.id > 0) dto.id else surah * 1000 + ayah,
                surahNumber = surah,
                ayahNumber = ayah,
                arabicText = cleanArabic,
                englishTranslation = cleanEn,
                juzNumber = dto.juz_number.coerceIn(1, 30),
                pageNumber = dto.page_number.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
            )
        }
    }

    internal fun extractPageLayouts(dtos: List<QuranComVerseDto>): Map<Int, MushafPageLineLayout> {
        val pageLineMap = sortedMapOf<Int, java.util.TreeMap<Int, MutableList<MushafLineWord>>>()
        for (verse in dtos) {
            val parts = verse.verse_key.split(':')
            val surah = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val ayah = parts.getOrNull(1)?.toIntOrNull() ?: verse.verse_number.coerceAtLeast(1)
            val defaultPage = verse.page_number.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)

            for (word in verse.words) {
                val wordPage = (word.page_number ?: defaultPage).coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
                val lineNo = word.line_number ?: continue
                val text = word.text_uthmani?.trim().orEmpty()
                if (text.isEmpty()) continue
                val isEnd = word.char_type_name.equals("end", ignoreCase = true)
                val formattedText = if (isEnd) {
                    "\u06DD${QuranScriptFormatter.toArabicIndic(ayah)}"
                } else {
                    text
                }
                val linesForPage = pageLineMap.getOrPut(wordPage) { java.util.TreeMap() }
                linesForPage.getOrPut(lineNo) { mutableListOf() }.add(
                    MushafLineWord(
                        surahNumber = surah,
                        ayahNumber = ayah,
                        text = formattedText,
                        isEndMarker = isEnd
                    )
                )
            }
        }
        return pageLineMap.mapValues { (pageNum, linesTree) ->
            MushafPageLineLayout(
                pageNumber = pageNum,
                lines = linesTree.entries.map { (ln, w) -> MushafLine(lineNumber = ln, words = w) }
            )
        }
    }

    private fun readCachedPageLayout(pageNumber: Int): MushafPageLineLayout? {
        val cacheFile = File(cacheDir, "p$pageNumber.json")
        if (!cacheFile.exists()) return null
        return runCatching {
            pageAdapter.fromJson(cacheFile.readText())
        }.getOrNull()?.takeIf { it.lines.isNotEmpty() }
    }

    private fun writeCachedPageLayout(layout: MushafPageLineLayout) {
        if (layout.lines.isEmpty()) return
        runCatching {
            File(cacheDir, "p${layout.pageNumber}.json").writeText(pageAdapter.toJson(layout))
        }
    }

    companion object {
        fun bundledStarterLineLayout(pageNumber: Int): MushafPageLineLayout? = when (pageNumber) {
            1 -> MushafPageLineLayout(
                pageNumber = 1,
                lines = listOf(
                    line(2, 1, 1, listOf("بِسْمِ", "ٱللَّهِ", "ٱلرَّحْمَـٰنِ", "ٱلرَّحِيمِ"), endAyah = true),
                    line(3, 1, 2, listOf("ٱلْحَمْدُ", "لِلَّهِ", "رَبِّ", "ٱلْعَـٰلَمِينَ"), endAyah = true),
                    line(
                        4,
                        1,
                        3,
                        listOf("ٱلرَّحْمَـٰنِ", "ٱلرَّحِيمِ"),
                        endAyah = true,
                        extra = listOf(w(1, 4, "مَـٰلِكِ"), w(1, 4, "يَوْمِ"), w(1, 4, "ٱلدِّينِ", isEndAfter = true))
                    ),
                    line(5, 1, 5, listOf("إِيَّاكَ", "نَعْبُدُ", "وَإِيَّاكَ", "نَسْتَعِينُ"), endAyah = true),
                    line(6, 1, 6, listOf("ٱهْدِنَا", "ٱلصِّرَٰطَ", "ٱلْمُسْتَقِيمَ"), endAyah = true),
                    line(7, 1, 7, listOf("صِرَٰطَ", "ٱلَّذِينَ", "أَنْعَمْتَ", "عَلَيْهِمْ", "غَيْرِ"), endAyah = false),
                    line(8, 1, 7, listOf("ٱلْمَغْضُوبِ", "عَلَيْهِمْ", "وَلَا", "ٱلضَّآلِّينَ"), endAyah = true)
                )
            )
            // Exact Medina Mushaf Page 2 (Surah Al-Baqarah 2:1-5) lines 3..8 below Header + Bismillah
            2 -> MushafPageLineLayout(
                pageNumber = 2,
                lines = listOf(
                    line(
                        3,
                        2,
                        1,
                        listOf("الٓمٓ"),
                        endAyah = true,
                        extra = listOf(
                            w(2, 2, "ذَٰلِكَ"),
                            w(2, 2, "ٱلْكِتَـٰبُ"),
                            w(2, 2, "لَا"),
                            w(2, 2, "رَيْبَ ۛ"),
                            w(2, 2, "فِيهِ ۛ"),
                            w(2, 2, "هُدًى"),
                            w(2, 2, "لِّلْمُتَّقِينَ", isEndAfter = true)
                        )
                    ),
                    line(
                        4,
                        2,
                        3,
                        listOf("ٱلَّذِينَ", "يُؤْمِنُونَ", "بِٱلْغَيْبِ", "وَيُقِيمُونَ", "ٱلصَّلَوٰةَ", "وَمِمَّا"),
                        endAyah = false
                    ),
                    line(
                        5,
                        2,
                        3,
                        listOf("رَزَقْنَـٰهُمْ", "يُنفِقُونَ"),
                        endAyah = true,
                        extra = listOf(
                            w(2, 4, "وَٱلَّذِينَ"),
                            w(2, 4, "يُؤْمِنُونَ"),
                            w(2, 4, "بِمَآ"),
                            w(2, 4, "أُنزِلَ")
                        )
                    ),
                    line(
                        6,
                        2,
                        4,
                        listOf("إِلَيْكَ", "وَمَآ", "أُنزِلَ", "مِن", "قَبْلِكَ", "وَبِٱلْـَٔاخِرَةِ", "هُمْ", "يُوقِنُونَ"),
                        endAyah = true
                    ),
                    line(
                        7,
                        2,
                        5,
                        listOf("أُو۟لَـٰٓئِكَ", "عَلَىٰ", "هُدًى", "مِّن", "رَّبِّهِمْ ۖ", "وَأُو۟لَـٰٓئِكَ", "هُمُ"),
                        endAyah = false
                    ),
                    line(
                        8,
                        2,
                        5,
                        listOf("ٱلْمُفْلِحُونَ"),
                        endAyah = true
                    )
                )
            )
            else -> null
        }

        private fun w(surah: Int, ayah: Int, text: String, isEndAfter: Boolean = false): List<MushafLineWord> {
            return if (isEndAfter) {
                listOf(
                    MushafLineWord(surah, ayah, text, isEndMarker = false),
                    MushafLineWord(surah, ayah, "\u06DD${QuranScriptFormatter.toArabicIndic(ayah)}", isEndMarker = true)
                )
            } else {
                listOf(MushafLineWord(surah, ayah, text, isEndMarker = false))
            }
        }

        private fun line(
            lineNumber: Int,
            surah: Int,
            ayah: Int,
            words: List<String>,
            endAyah: Boolean,
            extra: List<List<MushafLineWord>> = emptyList()
        ): MushafLine {
            val out = mutableListOf<MushafLineWord>()
            words.forEach { out += MushafLineWord(surah, ayah, it, isEndMarker = false) }
            if (endAyah) {
                out += MushafLineWord(surah, ayah, "\u06DD${QuranScriptFormatter.toArabicIndic(ayah)}", isEndMarker = true)
            }
            extra.forEach { out += it }
            return MushafLine(lineNumber = lineNumber, words = out)
        }
    }
}
