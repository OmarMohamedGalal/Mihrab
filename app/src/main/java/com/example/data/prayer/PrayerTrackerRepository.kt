package com.example.data.prayer

import com.example.data.local.PrayerLogDao
import com.example.data.local.PrayerLogEntity
import com.example.domain.prayer.PrayerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class PrayerTrackerRepository(private val dao: PrayerLogDao) {

    fun observeCompletedForDate(date: LocalDate): Flow<Set<PrayerType>> {
        return dao.observeDayLogs(date.toString()).map { logs ->
            logs.filter { it.completed }
                .mapNotNull { runCatching { PrayerType.valueOf(it.prayerType) }.getOrNull() }
                .toSet()
        }
    }

    fun observeStreakDays(today: LocalDate = LocalDate.now()): Flow<Int> {
        return dao.observeAllCompletedLogs().map { logs ->
            val byDate = logs.groupBy { it.dateIso }
                .mapValues { (_, list) -> list.map { it.prayerType }.toSet() }
            var streak = 0
            var cursor = today
            // Count today if all 5 are completed, otherwise start streak check from yesterday
            val todayDone = PrayerType.obligatoryPrayers.all {
                byDate[today.toString()]?.contains(it.name) == true
            }
            if (!todayDone) {
                cursor = today.minusDays(1)
            }
            while (true) {
                val set = byDate[cursor.toString()] ?: break
                val allFive = PrayerType.obligatoryPrayers.all { set.contains(it.name) }
                if (!allFive) break
                streak++
                cursor = cursor.minusDays(1)
            }
            streak
        }
    }

    suspend fun togglePrayerCompleted(
        date: LocalDate,
        prayerType: PrayerType,
        completed: Boolean
    ) = withContext(Dispatchers.IO) {
        if (!prayerType.isObligatory) return@withContext
        dao.upsert(
            PrayerLogEntity(
                dateIso = date.toString(),
                prayerType = prayerType.name,
                completed = completed,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }
}
