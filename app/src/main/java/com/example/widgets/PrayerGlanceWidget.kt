package com.example.widgets

import android.content.Context
import android.os.SystemClock
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MihrabApplication
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.ArabicStrings
import com.example.core.i18n.EnglishStrings
import com.example.core.util.FormattingUtils
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.prayer.PrayerType
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class NextWidgetPrayerRow(
    val type: PrayerType,
    val shortLabel: String,
    val fullLabel: String,
    val compactTime: String,
    val isNext: Boolean
)

class PrayerGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()
        val clickAction = WidgetDestinations.openAppAction(context, WidgetDestinations.PRAYER)

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
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
            val nextPrayerName = strings.prayerName(next.nextPrayer)
            val nextPrayerTime = FormattingUtils.formatPrayerTime(next.nextTime, prefs.use24Hour, isArabic)
            val remainingMillis = next.remaining.toMillis().coerceAtLeast(1000L)
            val palette = WidgetThemeResolver.resolve(context, prefs)

            val compactTimePattern = if (prefs.use24Hour) "HH:mm" else "h:mm"
            val compactFormatter = DateTimeFormatter.ofPattern(
                compactTimePattern,
                if (isArabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
            )
            val allPrayers = todayTimes.obligatoryEntries.map { entry ->
                val isNext = entry.type == next.nextPrayer &&
                    entry.time.toLocalDate() == next.nextTime.toLocalDate()
                NextWidgetPrayerRow(
                    type = entry.type,
                    shortLabel = compactPrayerLabel(entry.type, isArabic),
                    fullLabel = strings.prayerName(entry.type),
                    compactTime = compactFormatter.format(entry.time),
                    isNext = isNext
                )
            }

            val size: DpSize = LocalSize.current

            GlanceTheme {
                ExpressiveNextPrayerContent(
                    context = context,
                    size = size,
                    palette = palette,
                    isArabic = isArabic,
                    prayerName = nextPrayerName,
                    prayerTime = nextPrayerTime,
                    remainingMillis = remainingMillis,
                    allPrayers = allPrayers,
                    clickAction = clickAction
                )
            }
        }
    }

    private fun compactPrayerLabel(type: PrayerType, isArabic: Boolean): String = if (isArabic) {
        when (type) {
            PrayerType.FAJR -> "فجر"
            PrayerType.SUNRISE -> "شروق"
            PrayerType.DHUHR -> "ظهر"
            PrayerType.ASR -> "عصر"
            PrayerType.MAGHRIB -> "مغرب"
            PrayerType.ISHA -> "عشاء"
        }
    } else {
        when (type) {
            PrayerType.FAJR -> "Fjr"
            PrayerType.SUNRISE -> "Sun"
            PrayerType.DHUHR -> "Dhr"
            PrayerType.ASR -> "Asr"
            PrayerType.MAGHRIB -> "Mgh"
            PrayerType.ISHA -> "Ish"
        }
    }

    @Composable
    private fun ExpressiveNextPrayerContent(
        context: Context,
        size: DpSize,
        palette: ResolvedWidgetPalette,
        isArabic: Boolean,
        prayerName: String,
        prayerTime: String,
        remainingMillis: Long,
        allPrayers: List<NextWidgetPrayerRow>,
        clickAction: Action
    ) {
        val isTiny = size.width < 120.dp || size.height < 110.dp
        val isWide = size.width >= 230.dp
        val isTall = size.height >= 195.dp

        val nameFontSize = when {
            isTiny -> 22.sp
            isTall && isWide -> 40.sp
            isTall || isWide -> 34.sp
            else -> 30.sp
        }
        val timeFontSize = when {
            isTiny -> 13.sp
            isTall -> 19.sp
            else -> 16.sp
        }
        val countdownSp = when {
            isTiny -> 11f
            isTall -> 14f
            else -> 12f
        }

        val chronometerViews = RemoteViews(context.packageName, R.layout.widget_countdown_chronometer).apply {
            setChronometerCountDown(R.id.widget_chronometer, true)
            setChronometer(
                R.id.widget_chronometer,
                SystemClock.elapsedRealtime() + remainingMillis,
                null,
                true
            )
            setTextColor(R.id.widget_chronometer, palette.onPrimaryContainer)
            setTextViewTextSize(R.id.widget_chronometer, TypedValue.COMPLEX_UNIT_SP, countdownSp)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(palette.surfaceContainerColor))
                .cornerRadius(28.dp)
                .clickable(clickAction)
                .padding(horizontal = if (isTiny) 10.dp else 12.dp, vertical = if (isTiny) 8.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top Header: Oversized Next Prayer Name + Time under it
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = prayerName,
                        style = TextStyle(
                            color = ColorProvider(palette.primaryColor),
                            fontSize = nameFontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(1.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayerTime,
                            style = TextStyle(
                                color = ColorProvider(palette.onPrimaryContainerColor),
                                fontSize = timeFontSize,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        if (!isTiny) {
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Box(
                                modifier = GlanceModifier
                                    .background(ColorProvider(palette.primaryContainerColor))
                                    .cornerRadius(10.dp)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                AndroidRemoteViews(remoteViews = chronometerViews)
                            }
                        }
                    }
                }
            }

            if (!isTiny) {
                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Section: Small All Prayer Times Table
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(if (isTall) 52.dp else 40.dp)
                        .background(ColorProvider(palette.primaryContainerColor))
                        .cornerRadius(16.dp)
                        .padding(horizontal = 3.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allPrayers.forEach { item ->
                        val cellBg = if (item.isNext) palette.primaryColor else palette.primaryContainerColor
                        val labelColor = if (item.isNext) palette.onPrimaryColor else palette.onSurfaceVariantColor
                        val timeColor = if (item.isNext) palette.onPrimaryColor else palette.onPrimaryContainerColor

                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .background(ColorProvider(cellBg))
                                .cornerRadius(12.dp)
                                .padding(horizontal = 1.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isWide) item.fullLabel else item.shortLabel,
                                style = TextStyle(
                                    color = ColorProvider(labelColor),
                                    fontSize = if (isWide) 10.sp else 8.5.sp,
                                    fontWeight = if (item.isNext) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = item.compactTime,
                                style = TextStyle(
                                    color = ColorProvider(timeColor),
                                    fontSize = if (isWide) 11.sp else 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        suspend fun updateAllWidgets(context: Context) {
            WidgetUpdater.updateAll(context)
        }
    }
}

class PrayerGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerGlanceWidget()
}
