package com.example.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
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
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.ArabicStrings
import com.example.core.i18n.EnglishStrings
import com.example.core.util.FormattingUtils
import com.example.domain.calendar.HijriCalendarEngine
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.prayer.PrayerType
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

private data class TimetableChipData(
    val type: PrayerType,
    val label: String,
    val timeLabel: String,
    val isNext: Boolean,
    val isPassed: Boolean
)

class PrayerTimetableWidget : GlanceAppWidget() {

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
            val hijri = HijriCalendarEngine.fromGregorian(today, prefs.hijriOffsetDays).formatted(isArabic)
            val palette = WidgetThemeResolver.resolve(context, prefs)

            val chips = todayTimes.obligatoryEntries.map { entry ->
                val isNext = entry.type == next.nextPrayer && entry.time.toLocalDate() == next.nextTime.toLocalDate()
                TimetableChipData(
                    type = entry.type,
                    label = strings.prayerName(entry.type),
                    timeLabel = FormattingUtils.formatPrayerTime(entry.time, prefs.use24Hour, isArabic),
                    isNext = isNext,
                    isPassed = entry.time.isBefore(now) && !isNext
                )
            }

            val size = LocalSize.current
            val isTall = size.height >= 120.dp
            val isCompactWidth = size.width < 220.dp

            GlanceTheme {
                PrayerTimetableContent(
                    city = prefs.cityName,
                    hijri = hijri,
                    chips = chips,
                    isTall = isTall,
                    isCompactWidth = isCompactWidth,
                    palette = palette,
                    clickAction = clickAction
                )
            }
        }
    }

    @Composable
    private fun PrayerTimetableContent(
        city: String,
        hijri: String,
        chips: List<TimetableChipData>,
        isTall: Boolean,
        isCompactWidth: Boolean,
        palette: ResolvedWidgetPalette,
        clickAction: Action
    ) {
        if (isTall) {
            // M3 Expressive Inbox-style stacked pill rows
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(palette.surfaceContainerColor))
                    .cornerRadius(26.dp)
                    .clickable(clickAction)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = city,
                        style = TextStyle(
                            color = ColorProvider(palette.onPrimaryContainerColor),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = hijri,
                        style = TextStyle(
                            color = ColorProvider(palette.primaryColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                chips.forEachIndexed { idx, chip ->
                    val rowBg = if (chip.isNext) palette.primaryColor else palette.primaryContainerColor
                    val nameColor = if (chip.isNext) palette.onPrimaryColor else palette.onPrimaryContainerColor
                    val timeColor = if (chip.isNext) palette.onPrimaryColor else palette.primaryColor

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .background(ColorProvider(rowBg))
                            .cornerRadius(16.dp)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chip.label,
                            style = TextStyle(
                                color = ColorProvider(nameColor),
                                fontSize = 13.sp,
                                fontWeight = if (chip.isNext) FontWeight.Bold else FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = chip.timeLabel,
                            style = TextStyle(
                                color = ColorProvider(timeColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                    if (idx != chips.lastIndex) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                }
            }
        } else {
            // 1x4 Horizontal Expressive Pill Bar (adapts smoothly to 2x1/3x1 narrow widths too)
            val displayedChips = if (isCompactWidth) {
                val nextIdx = chips.indexOfFirst { it.isNext }.let { if (it >= 0) it else 0 }
                val start = nextIdx.coerceAtMost((chips.size - 3).coerceAtLeast(0))
                chips.subList(start, (start + 3).coerceAtMost(chips.size))
            } else {
                chips
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(palette.surfaceContainerColor))
                    .cornerRadius(28.dp)
                    .clickable(clickAction)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayedChips.forEachIndexed { idx, chip ->
                    val pillBg = if (chip.isNext) palette.primaryColor else palette.primaryContainerColor
                    val labelColor = if (chip.isNext) palette.onPrimaryColor else palette.onSurfaceVariantColor
                    val timeColor = if (chip.isNext) palette.onPrimaryColor else palette.onPrimaryContainerColor

                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .background(ColorProvider(pillBg))
                            .cornerRadius(18.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chip.label,
                            style = TextStyle(
                                color = ColorProvider(labelColor),
                                fontSize = 10.sp,
                                fontWeight = if (chip.isNext) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = chip.timeLabel,
                            style = TextStyle(
                                color = ColorProvider(timeColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                    }
                    if (idx != displayedChips.lastIndex) {
                        Spacer(modifier = GlanceModifier.width(5.dp))
                    }
                }
            }
        }
    }
}

class PrayerTimetableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerTimetableWidget()
}
