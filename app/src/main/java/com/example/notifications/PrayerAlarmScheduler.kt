package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.ArabicStrings
import com.example.core.i18n.EnglishStrings
import com.example.core.util.FormattingUtils
import com.example.data.preferences.UserPreferences
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.prayer.PrayerType
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object PrayerAlarmScheduler {

    const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    const val EXTRA_IS_REMINDER = "extra_is_reminder"

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun scheduleUpcomingPrayers(context: Context, prefs: UserPreferences) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zoneId)
        val params = PrayerTimeCalculator.Params(
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            method = prefs.calculationMethod,
            asrMethod = prefs.asrMethod,
            highLatitudeRule = prefs.highLatitudeRule,
            offsets = prefs.offsets,
            hijriAdjustmentDays = prefs.hijriOffsetDays
        )

        val today = LocalDate.now(zoneId)
        val days = listOf(
            PrayerTimeCalculator.calculate(today, zoneId, params),
            PrayerTimeCalculator.calculate(today.plusDays(1), zoneId, params)
        )

        val isArabic = prefs.language == AppLanguage.ARABIC
        val strings = if (isArabic) ArabicStrings else EnglishStrings

        // Cancel existing slots first (5 prayers x 2 days x 2 types)
        for (dayOffset in 0..1) {
            for (prayer in PrayerType.obligatoryPrayers) {
                cancelSlot(context, alarmManager, dayOffset * 100 + prayer.ordinal)
                cancelSlot(context, alarmManager, 1000 + dayOffset * 100 + prayer.ordinal)
            }
        }

        days.forEachIndexed { dayIndex, dailyTimes ->
            PrayerType.obligatoryPrayers.forEach { prayer ->
                if (!prefs.isPrayerNotificationEnabled(prayer)) return@forEach

                val prayerTime = dailyTimes.timeFor(prayer)
                val formattedTime = FormattingUtils.formatPrayerTime(prayerTime, prefs.use24Hour, isArabic)
                val prayerLabel = strings.prayerName(prayer)

                if (prayerTime.isAfter(now)) {
                    val title = if (isArabic) "حان الآن موعد صلاة $prayerLabel" else "$prayerLabel Prayer Time"
                    val body = if (isArabic) "${prefs.cityName} • $formattedTime" else "${prefs.cityName} • $formattedTime"
                    scheduleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        requestCode = dayIndex * 100 + prayer.ordinal,
                        triggerAtMs = prayerTime.toInstant().toEpochMilli(),
                        prayer = prayer,
                        title = title,
                        body = body,
                        isReminder = false
                    )
                }

                if (prefs.reminderMinutesBefore > 0) {
                    val reminderTime = prayerTime.minusMinutes(prefs.reminderMinutesBefore.toLong())
                    if (reminderTime.isAfter(now)) {
                        val title = if (isArabic) {
                            "تذكير: $prayerLabel بعد ${prefs.reminderMinutesBefore} دقيقة"
                        } else {
                            "$prayerLabel in ${prefs.reminderMinutesBefore} min"
                        }
                        val body = "${prefs.cityName} • $formattedTime"
                        scheduleAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            requestCode = 1000 + dayIndex * 100 + prayer.ordinal,
                            triggerAtMs = reminderTime.toInstant().toEpochMilli(),
                            prayer = prayer,
                            title = title,
                            body = body,
                            isReminder = true
                        )
                    }
                }
            }
        }

        runCatching {
            PrayerNotificationHelper.updateLockScreenPrayerNotification(context, prefs)
        }
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerAtMs: Long,
        prayer: PrayerType,
        title: String,
        body: String,
        isReminder: Boolean
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_TYPE, prayer.name)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_IS_REMINDER, isReminder)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        runCatching {
            if (canScheduleExactAlarms(context)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
            }
        }
    }

    private fun cancelSlot(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, PrayerAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }
}
