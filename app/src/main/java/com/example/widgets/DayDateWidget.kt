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
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MihrabApplication
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Transparent weekday + date widget: the same day card typography as [DayCardWidget],
 * with no background so the wallpaper shows through. Tapping opens the app on the calendar.
 */
class DayDateWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()
        val clickAction: Action = WidgetDestinations.openAppAction(context, WidgetDestinations.CALENDAR)

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
            val today = LocalDate.now(zoneId)
            val dayName = arabicDayName(today.dayOfWeek)
            val dateText = "%02d / %02d / %d".format(today.dayOfMonth, today.monthValue, today.year)
            val palette = WidgetThemeResolver.resolve(context, prefs)

            GlanceTheme {
                DayDateContent(
                    dayName = dayName,
                    dateText = dateText,
                    palette = palette,
                    clickAction = clickAction
                )
            }
        }
    }

    @Composable
    private fun DayDateContent(
        dayName: String,
        dateText: String,
        palette: ResolvedWidgetPalette,
        clickAction: Action
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(clickAction)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayName,
                style = TextStyle(
                    color = ColorProvider(palette.onPrimaryContainerColor),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
            Text(
                text = dateText,
                style = TextStyle(
                    color = ColorProvider(palette.primaryColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

class DayDateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayDateWidget()
}
