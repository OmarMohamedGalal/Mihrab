package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MihrabApplication
import com.example.widgets.PrayerGlanceWidget
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PrayerRescheduleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? MihrabApplication ?: return Result.success()
        val prefs = app.container.preferencesDataStore.preferencesFlow.first()
        PrayerAlarmScheduler.scheduleUpcomingPrayers(applicationContext, prefs)
        PrayerGlanceWidget.updateAllWidgets(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "mihrab_daily_prayer_reschedule"

        fun enqueueDailyRefresh(context: Context) {
            val request = PeriodicWorkRequestBuilder<PrayerRescheduleWorker>(
                6, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
