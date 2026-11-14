package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quran_verses",
    indices = [
        Index(value = ["surahNumber", "ayahNumber"], unique = true),
        Index(value = ["pageNumber"])
    ]
)
data class QuranVerseEntity(
    @PrimaryKey val globalNumber: Int,
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val englishTranslation: String,
    val juzNumber: Int,
    val pageNumber: Int = 1
)

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["surahNumber", "ayahNumber"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameEn: String,
    val surahNameAr: String,
    val arabicSnippet: String,
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "prayer_logs",
    indices = [Index(value = ["dateIso", "prayerType"], unique = true)]
)
data class PrayerLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateIso: String,
    val prayerType: String,
    val completed: Boolean,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "custom_dhikr")
data class CustomDhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val arabicText: String,
    val meaning: String,
    val targetCount: Int,
    val currentCount: Int = 0
)

@Entity(tableName = "tasbeeh_stats")
data class TasbeehStatsEntity(
    @PrimaryKey val dateIso: String,
    val totalCount: Int
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val weatherCode: Int,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val fetchedAtEpochMs: Long
)
