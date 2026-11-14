package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MihrabApplication
import com.example.data.preferences.AdhanSoundStyle
import com.example.widgets.PrayerGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? MihrabApplication ?: return
        val container = app.container

        if (intent.action == PrayerNotificationHelper.ACTION_STOP_ADHAN) {
            container.adhanAudioManager.stop()
            return
        }

        val title = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TITLE) ?: "Prayer Time"
        val body = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_BODY) ?: ""
        val isReminder = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_REMINDER, false)
        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_TYPE) ?: "PRAYER"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = container.preferencesDataStore.preferencesFlow.first()
                val isSilent = prefs.adhanStyle == AdhanSoundStyle.SILENT

                PrayerNotificationHelper.showPrayerNotification(
                    context = context,
                    notificationId = prayerName.hashCode() + (if (isReminder) 1 else 0),
                    title = title,
                    body = body,
                    isSilent = isSilent,
                    isReminder = isReminder,
                    vibrate = prefs.vibrationEnabled,
                    includeStopAction = false,
                    soundStyle = prefs.adhanStyle
                )

                // Reschedule upcoming days, update lock screen schedule & refresh widgets
                PrayerAlarmScheduler.scheduleUpcomingPrayers(context, prefs)
                PrayerGlanceWidget.updateAllWidgets(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
