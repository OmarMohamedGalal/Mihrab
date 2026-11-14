package com.example.data.quran

import com.example.data.local.BookmarkDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.QuranDao
import com.example.data.local.QuranVerseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

data class DownloadProgress(
    val completedSurahs: Int,
    val totalSurahs: Int = 114,
    val currentSurahName: String = "",
    val isFinished: Boolean = false,
    val error: String? = null
)

class QuranRepository(
    private val quranDao: QuranDao,
    private val bookmarkDao: BookmarkDao,
    private val remoteApi: QuranRemoteApi
) {
    fun observeSurahVerses(surahNumber: Int): Flow<List<QuranVerseEntity>> =
        quranDao.observeSurahVerses(surahNumber)

    fun observePageVerses(pageNumber: Int): Flow<List<QuranVerseEntity>> =
        quranDao.observePageVerses(pageNumber)

    fun observeCachedSurahNumbers(): Flow<List<Int>> =
        quranDao.observeCachedSurahNumbers()

    fun observeTotalVerseCount(): Flow<Int> =
        quranDao.observeTotalVerseCount()

    fun observeBookmarks(): Flow<List<BookmarkEntity>> =
        bookmarkDao.observeAll()

    fun observeIsBookmarked(surah: Int, ayah: Int): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(surah, ayah)

    suspend fun ensureStarterDataSeeded() = withContext(Dispatchers.IO) {
        if (quranDao.getSurahVerses(1).isEmpty()) {
            quranDao.insertVerses(QuranMetadata.bundledStarterVerses)
        }
    }

    suspend fun fetchAndCacheSurahIfMissing(surahNumber: Int): Result<List<QuranVerseEntity>> =
        withContext(Dispatchers.IO) {
            val existing = quranDao.getSurahVerses(surahNumber)
            val expectedCount = QuranMetadata.surahByNumber(surahNumber).ayahCount
            if (existing.size >= expectedCount) {
                return@withContext Result.success(existing)
            }
            fetchSurahFromRemote(surahNumber)
        }

    suspend fun fetchAndCachePageIfMissing(pageNumber: Int): Result<List<QuranVerseEntity>> =
        withContext(Dispatchers.IO) {
            val existing = quranDao.getPageVerses(pageNumber)
            val surahsOnPage = QuranMetadata.surahsOnPage(pageNumber)
            val allSurahsFullyCached = surahsOnPage.isNotEmpty() && surahsOnPage.all { surah ->
                quranDao.getSurahVerses(surah.number).size >= surah.ayahCount
            }
            if (existing.isNotEmpty() && allSurahsFullyCached) {
                return@withContext Result.success(existing)
            }
            fetchPageFromRemote(pageNumber).recoverCatching { err ->
                if (existing.isNotEmpty()) existing else throw err
            }
        }

    suspend fun fetchSurahFromRemote(surahNumber: Int): Result<List<QuranVerseEntity>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = remoteApi.getSurahArabicAndEnglish(surahNumber)
                val arabicEdition = response.data.getOrNull(0)
                    ?: error("Arabic Quran edition missing")
                val englishEdition = response.data.getOrNull(1)
                val entities = arabicEdition.ayahs.mapIndexed { idx, arAyah ->
                    val enText = englishEdition?.ayahs?.getOrNull(idx)?.text.orEmpty()
                    val cleanArabic = QuranScriptFormatter.stripLeadingBismillah(
                        surahNumber = surahNumber,
                        ayahNumber = arAyah.numberInSurah,
                        text = arAyah.text
                    )
                    QuranVerseEntity(
                        globalNumber = arAyah.number,
                        surahNumber = surahNumber,
                        ayahNumber = arAyah.numberInSurah,
                        arabicText = cleanArabic,
                        englishTranslation = enText,
                        juzNumber = arAyah.juz,
                        pageNumber = arAyah.page.coerceIn(1, 604)
                    )
                }
                quranDao.insertVerses(entities)
                entities
            }
        }

    suspend fun fetchPageFromRemote(pageNumber: Int): Result<List<QuranVerseEntity>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = remoteApi.getMushafPageArabicAndEnglish(pageNumber.coerceIn(1, 604))
                val arabicEdition = response.data.getOrNull(0)
                    ?: error("Arabic Mushaf page missing")
                val englishEdition = response.data.getOrNull(1)
                val entities = arabicEdition.ayahs.mapIndexed { idx, arAyah ->
                    val enText = englishEdition?.ayahs?.getOrNull(idx)?.text.orEmpty()
                    val cleanArabic = QuranScriptFormatter.stripLeadingBismillah(
                        surahNumber = arAyah.surah.number,
                        ayahNumber = arAyah.numberInSurah,
                        text = arAyah.text
                    )
                    QuranVerseEntity(
                        globalNumber = arAyah.number,
                        surahNumber = arAyah.surah.number,
                        ayahNumber = arAyah.numberInSurah,
                        arabicText = cleanArabic,
                        englishTranslation = enText,
                        juzNumber = arAyah.juz,
                        pageNumber = arAyah.page.coerceIn(1, 604)
                    )
                }
                quranDao.insertVerses(entities)
                entities
            }
        }

    fun downloadAllSurahsOffline(): Flow<DownloadProgress> = flow {
        var completed = 0
        for (surah in QuranMetadata.surahs) {
            val cached = quranDao.getSurahVerses(surah.number)
            if (cached.size >= surah.ayahCount) {
                completed++
                emit(DownloadProgress(completed, 114, surah.nameEn))
                continue
            }
            emit(DownloadProgress(completed, 114, surah.nameEn))
            val res = fetchSurahFromRemote(surah.number)
            if (res.isFailure) {
                emit(
                    DownloadProgress(
                        completedSurahs = completed,
                        totalSurahs = 114,
                        currentSurahName = surah.nameEn,
                        isFinished = true,
                        error = res.exceptionOrNull()?.localizedMessage ?: "Network unavailable"
                    )
                )
                return@flow
            }
            completed++
            emit(DownloadProgress(completed, 114, surah.nameEn))
        }
        emit(DownloadProgress(114, 114, "", isFinished = true))
    }

    suspend fun searchVerses(query: String): List<QuranVerseEntity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) emptyList() else quranDao.searchVerses(query.trim())
    }

    suspend fun setBookmark(verse: QuranVerseEntity, bookmarked: Boolean) = withContext(Dispatchers.IO) {
        if (bookmarked) {
            val meta = QuranMetadata.surahByNumber(verse.surahNumber)
            bookmarkDao.upsert(
                BookmarkEntity(
                    surahNumber = verse.surahNumber,
                    ayahNumber = verse.ayahNumber,
                    surahNameEn = meta.nameEn,
                    surahNameAr = meta.nameAr,
                    arabicSnippet = verse.arabicText,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        } else {
            bookmarkDao.deleteBySurahAyah(verse.surahNumber, verse.ayahNumber)
        }
    }
}
