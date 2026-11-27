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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MihrabApplication
import com.example.core.i18n.AppLanguage
import com.example.core.util.FormattingUtils
import com.example.domain.calendar.HijriCalendarEngine
import com.example.domain.calendar.HijriDate
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class HijriDateWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()
        val clickAction = WidgetDestinations.openAppAction(context, WidgetDestinations.CALENDAR)

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
            val today = LocalDate.now(zoneId)
            val hijri = HijriCalendarEngine.fromGregorian(today, prefs.hijriOffsetDays)
            val isArabic = prefs.language == AppLanguage.ARABIC
            val monthName = HijriCalendarEngine.monthName(hijri.month, isArabic)
            val gregorianLabel = FormattingUtils.formatGregorianDate(today, isArabic)
            val upcomingOccasion = HijriCalendarEngine.upcomingOccasions(today, prefs.hijriOffsetDays).firstOrNull()
            val occasionText = upcomingOccasion?.let {
                val title = if (isArabic) it.occasion.titleAr else it.occasion.titleEn
                if (isArabic) "$title بعد ${it.daysUntil} يوم" else "$title in ${it.daysUntil}d"
            }.orEmpty()
            val palette = WidgetThemeResolver.resolve(context, prefs)
            val size = LocalSize.current
            val isTiny = size.width < 115.dp && size.height < 105.dp
            val isTall = size.height >= 110.dp

            GlanceTheme {
                HijriDateContent(
                    hijri = hijri,
                    monthName = monthName,
                    gregorianLabel = gregorianLabel,
                    occasionText = occasionText,
                    isArabic = isArabic,
                    isTiny = isTiny,
                    isTall = isTall,
                    widthDp = size.width.value,
                    palette = palette,
                    clickAction = clickAction
                )
            }
        }
    }

    @Composable
    private fun HijriDateContent(
        hijri: HijriDate,
        monthName: String,
        gregorianLabel: String,
        occasionText: String,
        isArabic: Boolean,
        isTiny: Boolean,
        isTall: Boolean,
        widthDp: Float,
        palette: ResolvedWidgetPalette,
        clickAction: Action
    ) {
        val suffix = if (isArabic) "هـ" else "AH"

        if (isTiny) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(palette.primaryColor))
                    .cornerRadius(24.dp)
                    .clickable(clickAction)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${hijri.day}",
                    style = TextStyle(
                        color = ColorProvider(palette.onPrimaryColor),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = monthName,
                    style = TextStyle(
                        color = ColorProvider(palette.onPrimaryColor),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }
        } else if (isTall) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(palette.primaryContainerColor))
                    .cornerRadius(28.dp)
                    .clickable(clickAction)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = GlanceModifier
                            .size(46.dp)
                            .background(ColorProvider(palette.primaryColor))
                            .cornerRadius(23.dp)
                    ) {
                        Text(
                            text = "${hijri.day}",
                            style = TextStyle(
                                color = ColorProvider(palette.onPrimaryColor),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = monthName,
                            style = TextStyle(
                                color = ColorProvider(palette.onPrimaryContainerColor),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${hijri.year} $suffix",
                            style = TextStyle(
                                color = ColorProvider(palette.primaryColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = gregorianLabel,
                    style = TextStyle(
                        color = ColorProvider(palette.onSurfaceVariantColor),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
                if (occasionText.isNotBlank()) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = occasionText,
                        style = TextStyle(
                            color = ColorProvider(palette.primaryColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }
        } else {
            // 1x2 capsule: same layout, but text sizes shrink to the width that is really available
            // so month, year and the Gregorian line are always fully visible (no ellipsis).
            val badge = 32f
            val textWidth = (widthDp - 22f - badge - 6f).coerceAtLeast(40f)
            val line1 = "$monthName ${hijri.year} $suffix"
            val line2 = gregorianLabel
            val fs1 = (textWidth / (line1.length * 0.62f)).coerceIn(7f, 13f)
            val fs2 = (textWidth / (line2.length * 0.62f)).coerceIn(7f, 10f)
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(palette.primaryColor))
                    .cornerRadius(99.dp)
                    .clickable(clickAction)
                    .padding(start = 11.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = line1,
                        style = TextStyle(
                            color = ColorProvider(palette.onPrimaryColor),
                            fontSize = fs1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = line2,
                        style = TextStyle(
                            color = ColorProvider(palette.primaryContainerColor),
                            fontSize = fs2.sp
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = GlanceModifier.width(6.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .size(badge.dp)
                        .background(ColorProvider(palette.primaryContainerColor))
                        .cornerRadius((badge / 2).dp)
                ) {
                    Text(
                        text = "${hijri.day}",
                        style = TextStyle(
                            color = ColorProvider(palette.onPrimaryContainerColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

class HijriDateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HijriDateWidget()
}
