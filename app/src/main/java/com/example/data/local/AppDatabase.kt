package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuranVerseEntity::class,
        BookmarkEntity::class,
        PrayerLogEntity::class,
        CustomDhikrEntity::class,
        TasbeehStatsEntity::class,
        WeatherCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun prayerLogDao(): PrayerLogDao
    abstract fun dhikrDao(): DhikrDao
    abstract fun weatherDao(): WeatherDao
}
