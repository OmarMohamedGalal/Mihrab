package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_verses WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun observeSurahVerses(surahNumber: Int): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    suspend fun getSurahVerses(surahNumber: Int): List<QuranVerseEntity>

    @Query("SELECT * FROM quran_verses WHERE juzNumber = :juzNumber ORDER BY globalNumber ASC")
    suspend fun getJuzVerses(juzNumber: Int): List<QuranVerseEntity>

    @Query("SELECT * FROM quran_verses WHERE pageNumber = :pageNumber ORDER BY globalNumber ASC")
    fun observePageVerses(pageNumber: Int): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE pageNumber = :pageNumber ORDER BY globalNumber ASC")
    suspend fun getPageVerses(pageNumber: Int): List<QuranVerseEntity>

    @Query("SELECT DISTINCT surahNumber FROM quran_verses")
    fun observeCachedSurahNumbers(): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM quran_verses")
    fun observeTotalVerseCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM quran_verses
        WHERE arabicText LIKE '%' || :query || '%'
           OR englishTranslation LIKE '%' || :query || '%'
        ORDER BY globalNumber ASC
        LIMIT 60
        """
    )
    suspend fun searchVerses(query: String): List<QuranVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<QuranVerseEntity>)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah)")
    fun observeIsBookmarked(surah: Int, ayah: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah")
    suspend fun deleteBySurahAyah(surah: Int, ayah: Int)
}

@Dao
interface PrayerLogDao {
    @Query("SELECT * FROM prayer_logs WHERE dateIso = :dateIso")
    fun observeDayLogs(dateIso: String): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE completed = 1 ORDER BY dateIso DESC")
    fun observeAllCompletedLogs(): Flow<List<PrayerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: PrayerLogEntity)
}

@Dao
interface DhikrDao {
    @Query("SELECT * FROM custom_dhikr ORDER BY id DESC")
    fun observeCustomDhikrs(): Flow<List<CustomDhikrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomDhikr(item: CustomDhikrEntity): Long

    @Delete
    suspend fun deleteCustomDhikr(item: CustomDhikrEntity)

    @Query("SELECT * FROM tasbeeh_stats WHERE dateIso = :dateIso")
    fun observeDayStats(dateIso: String): Flow<TasbeehStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayStats(stats: TasbeehStatsEntity)
}

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE id = 1")
    fun observeCache(): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE id = 1")
    suspend fun getCache(): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCache(cache: WeatherCacheEntity)
}
