package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MihrabApplication
import com.example.widgets.PrayerGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED
        ) {
            val app = context.applicationContext as? MihrabApplication ?: return
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = app.container.preferencesDataStore.preferencesFlow.first()
                    PrayerAlarmScheduler.scheduleUpcomingPrayers(context, prefs)
                    PrayerGlanceWidget.updateAllWidgets(context)
                } finally {
                    pending.finish()
                }
            }
        }
    }
}
