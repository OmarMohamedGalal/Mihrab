package com.example

import android.app.Application
import com.example.core.AppContainer
import com.example.notifications.PrayerNotificationHelper
import com.example.notifications.PrayerRescheduleWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MihrabApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PrayerNotificationHelper.ensureChannels(this)
        runCatching { PrayerRescheduleWorker.enqueueDailyRefresh(this) }
        appScope.launch {
            container.quranRepository.ensureStarterDataSeeded()
        }
    }
}
