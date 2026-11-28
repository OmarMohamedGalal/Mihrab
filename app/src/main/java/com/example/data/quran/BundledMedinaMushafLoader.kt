package com.example.data.quran

import android.content.Context
import com.example.data.local.QuranVerseEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.TreeMap
import java.util.zip.GZIPInputStream

data class LoadedMedinaMushaf(
    val allVerses: List<QuranVerseEntity>,
    val versesBySurah: Map<Int, List<QuranVerseEntity>>,
    val versesByPage: Map<Int, List<QuranVerseEntity>>,
    val pageLayouts: Map<Int, MushafPageLineLayout>
)

/**
 * Loads the complete pre-bundled 604-page, 6,236-verse King Fahd Madinah Mushaf dataset
 * (`assets/medina_mushaf_604.tsv.gz`) lazily once in memory for 100% offline reading and search.
 */
class BundledMedinaMushafLoader(private val appContext: Context) {

    @Volatile
    private var cached: LoadedMedinaMushaf? = null
    private val loadLock = Any()

    fun loadOrNull(): LoadedMedinaMushaf? {
        cached?.let { return it }
        synchronized(loadLock) {
            cached?.let { return it }
            val loaded = runCatching { parseFromAssets() }.getOrNull()
            if (loaded != null) {
                cached = loaded
            }
            return loaded
        }
    }

    fun getAllVerses(): List<QuranVerseEntity> = loadOrNull()?.allVerses.orEmpty()

    fun getSurahVerses(surahNumber: Int): List<QuranVerseEntity> =
        loadOrNull()?.versesBySurah?.get(surahNumber).orEmpty()

    fun getPageVerses(pageNumber: Int): List<QuranVerseEntity> =
        loadOrNull()?.versesByPage?.get(pageNumber).orEmpty()

    fun getPageLayout(pageNumber: Int): MushafPageLineLayout? =
        loadOrNull()?.pageLayouts?.get(pageNumber)

    fun getCachedPageLayoutSync(pageNumber: Int): MushafPageLineLayout? =
        cached?.pageLayouts?.get(pageNumber)

    fun searchVerses(query: String, limit: Int = 60): List<QuranVerseEntity> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val data = loadOrNull() ?: return emptyList()
        val normalizedQuery = normalizeArabicForSearch(trimmed)
        if (normalizedQuery.isEmpty()) return emptyList()

        val matches = ArrayList<QuranVerseEntity>(limit)
        for (v in data.allVerses) {
            if (v.arabicText.contains(trimmed) ||
                normalizeArabicForSearch(v.arabicText).contains(normalizedQuery) ||
                (v.englishTranslation.isNotEmpty() && v.englishTranslation.contains(trimmed, ignoreCase = true))
            ) {
                matches.add(v)
                if (matches.size >= limit) break
            }
        }
        return matches
    }

    private fun parseFromAssets(): LoadedMedinaMushaf {
        val verses = ArrayList<QuranVerseEntity>(6236)
        val bySurah = HashMap<Int, MutableList<QuranVerseEntity>>(114)
        val byPage = HashMap<Int, MutableList<QuranVerseEntity>>(604)
        val layouts = HashMap<Int, MutableList<MushafLine>>(604)

        appContext.assets.open(ASSET_FILE_NAME).use { rawStream ->
            java.io.BufferedInputStream(rawStream, 32 * 1024).use { buffered ->
                buffered.mark(2)
                val b0 = buffered.read()
                val b1 = buffered.read()
                buffered.reset()
                val isGzip = (b0 == 0x1F && b1 == 0x8B)
                val decodedStream = if (isGzip) GZIPInputStream(buffered) else buffered
                decodedStream.use { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8), 32 * 1024).use { reader ->
                        while (true) {
                            val line = reader.readLine() ?: break
                            if (line.isEmpty()) continue
                            if (line.startsWith("V\t")) {
                                val parts = line.split('\t')
                                if (parts.size >= 7) {
                                    val globalId = parts[1].toIntOrNull() ?: continue
                                    val surah = parts[2].toIntOrNull() ?: continue
                                    val ayah = parts[3].toIntOrNull() ?: continue
                                    val juz = parts[4].toIntOrNull() ?: 1
                                    val page = parts[5].toIntOrNull()?.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES) ?: 1
                                    val arabic = parts[6]
                                    val entity = QuranVerseEntity(
                                        globalNumber = globalId,
                                        surahNumber = surah,
                                        ayahNumber = ayah,
                                        arabicText = arabic,
                                        englishTranslation = "",
                                        juzNumber = juz,
                                        pageNumber = page
                                    )
                                    verses.add(entity)
                                    bySurah.getOrPut(surah) { ArrayList() }.add(entity)
                                    byPage.getOrPut(page) { ArrayList() }.add(entity)
                                }
                            } else if (line.startsWith("L\t")) {
                                val parts = line.split('\t')
                                if (parts.size >= 4) {
                                    val page = parts[1].toIntOrNull()?.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES) ?: continue
                                    val lineNum = parts[2].toIntOrNull() ?: continue
                                    val words = ArrayList<MushafLineWord>(parts.size - 3)
                                    for (i in 3 until parts.size) {
                                        val tok = parts[i]
                                        val c1 = tok.indexOf(':')
                                        if (c1 <= 0) continue
                                        val c2 = tok.indexOf(':', c1 + 1)
                                        if (c2 <= c1 + 1) continue
                                        val c3 = tok.indexOf(':', c2 + 1)
                                        if (c3 <= c2 + 1) continue
                                        val s = tok.substring(0, c1).toIntOrNull() ?: continue
                                        val a = tok.substring(c1 + 1, c2).toIntOrNull() ?: continue
                                        val isEnd = tok[c2 + 1] == 'E'
                                        val wordText = tok.substring(c3 + 1)
                                        words.add(
                                            MushafLineWord(
                                                surahNumber = s,
                                                ayahNumber = a,
                                                text = wordText,
                                                isEndMarker = isEnd
                                            )
                                        )
                                    }
                                    if (words.isNotEmpty()) {
                                        layouts.getOrPut(page) { ArrayList(15) }
                                            .add(MushafLine(lineNumber = lineNum, words = words))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val pageLayoutMap = HashMap<Int, MushafPageLineLayout>(layouts.size)
        for ((p, lineList) in layouts) {
            lineList.sortBy { it.lineNumber }
            pageLayoutMap[p] = MushafPageLineLayout(pageNumber = p, lines = lineList)
        }

        return LoadedMedinaMushaf(
            allVerses = verses,
            versesBySurah = bySurah,
            versesByPage = byPage,
            pageLayouts = pageLayoutMap
        )
    }

    companion object {
        const val ASSET_FILE_NAME = "medina_mushaf_604.dat"

        internal fun normalizeArabicForSearch(input: String): String {
            val sb = StringBuilder(input.length)
            for (ch in input) {
                when (ch) {
                    in '\u064B'..'\u065F',
                    '\u0670',
                    in '\u06D6'..'\u06ED',
                    '\u0640' -> Unit
                    '\u0671', '\u0622', '\u0623', '\u0625' -> sb.append('\u0627')
                    '\u0649' -> sb.append('\u064A')
                    '\u0629' -> sb.append('\u0647')
                    else -> sb.append(ch)
                }
            }
            return sb.toString().replace(Regex("\\s+"), " ").trim()
        }
    }
}
