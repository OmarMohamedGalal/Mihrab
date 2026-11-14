package com.example.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.ArabicStrings
import com.example.core.i18n.EnglishStrings
import com.example.core.util.FormattingUtils
import com.example.data.preferences.AdhanSoundStyle
import com.example.data.preferences.UserPreferences
import com.example.domain.prayer.PrayerTimeCalculator
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object PrayerNotificationHelper {
    const val CHANNEL_ADHAN = "mihrab_prayer_default_v2"
    const val CHANNEL_ALARM_SOUND = "mihrab_prayer_alarm_v2"
    const val CHANNEL_RINGTONE_SOUND = "mihrab_prayer_ringtone_v2"
    const val CHANNEL_SILENT = "mihrab_silent_channel"
    const val CHANNEL_REMINDER = "mihrab_reminder_channel"
    const val CHANNEL_LOCKSCREEN_STATUS = "mihrab_lockscreen_prayer_status"
    const val ACTION_STOP_ADHAN = "com.example.ACTION_STOP_ADHAN"
    const val LOCKSCREEN_NOTIFICATION_ID = 7700

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val defaultNotifUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_NOTIFICATION_URI
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: defaultNotifUri
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: defaultNotifUri

        val notifAudioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alarmAudioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val defaultPrayerChannel = NotificationChannel(
            CHANNEL_ADHAN,
            "Prayer Time Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Phone notification alerts when obligatory prayer time enters"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(defaultNotifUri, notifAudioAttr)
        }

        val alarmPrayerChannel = NotificationChannel(
            CHANNEL_ALARM_SOUND,
            "Prayer Alarm Sound Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Phone alarm sound alerts when obligatory prayer time enters"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(alarmUri, alarmAudioAttr)
        }

        val ringtonePrayerChannel = NotificationChannel(
            CHANNEL_RINGTONE_SOUND,
            "Prayer Ringtone Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Phone ringtone alerts when obligatory prayer time enters"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(ringtoneUri, notifAudioAttr)
        }

        val silentChannel = NotificationChannel(
            CHANNEL_SILENT,
            "Silent Prayer Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Silent visual notifications for prayer times"
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            "Pre-Prayer Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Advance reminders before prayer time"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(defaultNotifUri, notifAudioAttr)
        }

        val lockscreenStatusChannel = NotificationChannel(
            CHANNEL_LOCKSCREEN_STATUS,
            "Lock Screen Prayer Schedule",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Displays next prayer and daily prayer schedule on the lock screen"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        nm.createNotificationChannels(
            listOf(
                defaultPrayerChannel,
                alarmPrayerChannel,
                ringtonePrayerChannel,
                silentChannel,
                reminderChannel,
                lockscreenStatusChannel
            )
        )
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showPrayerNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        isSilent: Boolean,
        isReminder: Boolean,
        vibrate: Boolean,
        includeStopAction: Boolean = false,
        soundStyle: AdhanSoundStyle = AdhanSoundStyle.MAQAM_HIJAZ
    ) {
        if (!hasNotificationPermission(context)) return

        ensureChannels(context)
        val channelId = when {
            isReminder -> CHANNEL_REMINDER
            isSilent || soundStyle == AdhanSoundStyle.SILENT -> CHANNEL_SILENT
            soundStyle == AdhanSoundStyle.MAKKAH_HARAM -> CHANNEL_ALARM_SOUND
            soundStyle == AdhanSoundStyle.MADINAH_TONE -> CHANNEL_RINGTONE_SOUND
            else -> CHANNEL_ADHAN
        }

        val openIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = when (soundStyle) {
            AdhanSoundStyle.MAKKAH_HARAM -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            AdhanSoundStyle.MADINAH_TONE -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            AdhanSoundStyle.MAQAM_HIJAZ -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AdhanSoundStyle.SILENT -> null
        } ?: Settings.System.DEFAULT_NOTIFICATION_URI

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_art)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(if (isReminder) NotificationCompat.CATEGORY_REMINDER else NotificationCompat.CATEGORY_ALARM)
            .setPriority(if (isSilent) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_HIGH)

        if (isSilent || soundStyle == AdhanSoundStyle.SILENT) {
            builder.setSilent(true)
            builder.setVibrate(longArrayOf(0L))
        } else {
            builder.setSound(defaultSoundUri)
            val defaults = if (vibrate) {
                NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS
            } else {
                NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS
            }
            builder.setDefaults(defaults)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    /**
     * Posts/updates a public lock-screen notification showing the Next Prayer and today's 5 prayer times
     * so the user can always view prayer times directly from their phone's lock screen.
     */
    fun updateLockScreenPrayerNotification(context: Context, prefs: UserPreferences) {
        if (!hasNotificationPermission(context)) return
        ensureChannels(context)

        val anyEnabled = prefs.notifyFajr || prefs.notifyDhuhr || prefs.notifyAsr || prefs.notifyMaghrib || prefs.notifyIsha
        if (!anyEnabled) {
            NotificationManagerCompat.from(context).cancel(LOCKSCREEN_NOTIFICATION_ID)
            return
        }

        val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zoneId)
        val today = LocalDate.now(zoneId)
        val params = PrayerTimeCalculator.Params(
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            method = prefs.calculationMethod,
            asrMethod = prefs.asrMethod,
            highLatitudeRule = prefs.highLatitudeRule,
            offsets = prefs.offsets,
            hijriAdjustmentDays = prefs.hijriOffsetDays
        )
        val todayTimes = PrayerTimeCalculator.calculate(today, zoneId, params)
        val tomorrowTimes = PrayerTimeCalculator.calculate(today.plusDays(1), zoneId, params)
        val yesterdayTimes = PrayerTimeCalculator.calculate(today.minusDays(1), zoneId, params)
        val next = PrayerTimeCalculator.resolveNextPrayer(now, todayTimes, tomorrowTimes, yesterdayTimes)

        val isArabic = prefs.language == AppLanguage.ARABIC
        val strings = if (isArabic) ArabicStrings else EnglishStrings
        val nextName = strings.prayerName(next.nextPrayer)
        val nextTimeStr = FormattingUtils.formatPrayerTime(next.nextTime, prefs.use24Hour, isArabic)

        val title = if (isArabic) {
            "${strings.nextPrayer}: $nextName • $nextTimeStr (${prefs.cityName})"
        } else {
            "${strings.nextPrayer}: $nextName at $nextTimeStr (${prefs.cityName})"
        }

        val scheduleSummary = todayTimes.obligatoryEntries.joinToString("  •  ") { entry ->
            val label = strings.prayerName(entry.type)
            val time = FormattingUtils.formatPrayerTime(entry.time, prefs.use24Hour, isArabic)
            "$label $time"
        }

        val openIntent = PendingIntent.getActivity(
            context,
            LOCKSCREEN_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_LOCKSCREEN_STATUS)
            .setSmallIcon(R.drawable.ic_launcher_art)
            .setContentTitle(title)
            .setContentText(scheduleSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(scheduleSummary))
            .setContentIntent(openIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .setShowWhen(false)
            .setSilent(true)
            .build()

        NotificationManagerCompat.from(context).notify(LOCKSCREEN_NOTIFICATION_ID, notification)
    }
}
