package com.example.widgets

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.example.MihrabApplication
import com.example.data.azkar.AzkarRepository
import com.example.domain.calendar.HijriCalendarEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

internal val GLANCE_MONTH_SHOW_BOTTOM_DATE_KEY = booleanPreferencesKey("glance_month_show_bottom_date")
internal val GLANCE_WEEKDAY_SHOW_BOTTOM_DATE_KEY = booleanPreferencesKey("glance_weekday_show_bottom_date")

/**
 * Coordinates single-tap (open Calendar screen) vs double-tap (toggle bottom date visibility)
 * on the transparent Month and Weekday home-screen widgets.
 */
internal object CalendarWidgetTapCoordinator {
    internal const val DEFAULT_DOUBLE_TAP_WINDOW_MS = 550L

    private val lock = Any()
    private val lastTapMs = mutableMapOf<String, Long>()
    private val pendingTokens = mutableMapOf<String, Long>()
    private val tokenSeq = AtomicLong(0L)

    internal fun resetForTest() {
        synchronized(lock) {
            lastTapMs.clear()
            pendingTokens.clear()
        }
    }

    suspend fun handleTap(
        context: Context,
        glanceId: GlanceId,
        isMonthWidget: Boolean,
        doubleTapWindowMs: Long = DEFAULT_DOUBLE_TAP_WINDOW_MS
    ) {
        val key = if (isMonthWidget) "month" else "weekday"
        val now = System.currentTimeMillis()
        var isDoubleTap = false
        var myToken = 0L

        synchronized(lock) {
            val prev = lastTapMs[key] ?: 0L
            if (prev > 0L && (now - prev) <= doubleTapWindowMs) {
                isDoubleTap = true
                lastTapMs[key] = 0L
                pendingTokens[key] = 0L
            } else {
                myToken = tokenSeq.incrementAndGet()
                lastTapMs[key] = now
                pendingTokens[key] = myToken
            }
        }

        if (isDoubleTap) {
            val app = context.applicationContext as MihrabApplication
            val nextVisible = if (isMonthWidget) {
                app.container.preferencesDataStore.toggleMonthWidgetBottomDate()
            } else {
                app.container.preferencesDataStore.toggleWeekdayWidgetBottomDate()
            }

            val manager = GlanceAppWidgetManager(context)
            val stateKey = if (isMonthWidget) GLANCE_MONTH_SHOW_BOTTOM_DATE_KEY else GLANCE_WEEKDAY_SHOW_BOTTOM_DATE_KEY
            val targetClass = if (isMonthWidget) CalligraphyMonthWidget::class.java else CalligraphyWeekdayWidget::class.java
            val ids = manager.getGlanceIds(targetClass)

            if (ids.isEmpty()) {
                runCatching {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { state ->
                        state.toMutablePreferences().apply { this[stateKey] = nextVisible }
                    }
                    if (isMonthWidget) {
                        CalligraphyMonthWidget().update(context, glanceId)
                    } else {
                        CalligraphyWeekdayWidget().update(context, glanceId)
                    }
                }
            } else {
                ids.forEach { id ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { state ->
                        state.toMutablePreferences().apply { this[stateKey] = nextVisible }
                    }
                }
                if (isMonthWidget) {
                    CalligraphyMonthWidget().updateAll(context)
                } else {
                    CalligraphyWeekdayWidget().updateAll(context)
                }
            }
            return
        }

        delay(doubleTapWindowMs)

        val shouldLaunch = synchronized(lock) {
            if (pendingTokens[key] == myToken) {
                pendingTokens[key] = 0L
                lastTapMs[key] = 0L
                true
            } else {
                false
            }
        }
        if (shouldLaunch) {
            context.startActivity(WidgetDestinations.openAppIntent(context, WidgetDestinations.CALENDAR))
        }
    }
}

class MonthWidgetTapAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        CalendarWidgetTapCoordinator.handleTap(
            context = context,
            glanceId = glanceId,
            isMonthWidget = true
        )
    }
}

class WeekdayWidgetTapAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        CalendarWidgetTapCoordinator.handleTap(
            context = context,
            glanceId = glanceId,
            isMonthWidget = false
        )
    }
}

/**
 * Widget 1 (transparent): Faded day-of-month stacked behind Dima Thuluth Arabic month
 * calligraphy (shadow-free) with weekday + Hijri full date directly below.
 * Single tap opens the Calendar screen; double tap toggles bottom date visibility.
 */
class CalligraphyMonthWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()
        val tapAction = actionRunCallback<MonthWidgetTapAction>()

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val glancePrefs = currentState<Preferences>()
            val showBottomDate = glancePrefs[GLANCE_MONTH_SHOW_BOTTOM_DATE_KEY] ?: prefs.monthWidgetShowBottomDate

            val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
            val today = LocalDate.now(zoneId)
            val hijri = HijriCalendarEngine.fromGregorian(today, prefs.hijriOffsetDays)

            val size = LocalSize.current
            val scale = context.resources.displayMetrics.density.coerceIn(2.0f, 3.2f)
            val widthDp = size.width.value.takeIf { it > 1f } ?: 260f
            val heightDp = size.height.value.takeIf { it > 1f } ?: 120f
            val widthPx = (widthDp * scale).roundToInt()
            val heightPx = (heightDp * scale).roundToInt()

            val bitmap = WidgetShapeRenderer.createTransparentMonthDateBitmap(
                context = context,
                widthPx = widthPx,
                heightPx = heightPx,
                today = today,
                hijri = hijri,
                showBottomDate = showBottomDate
            )
            val contentDesc = "${today.dayOfMonth} ${WidgetShapeRenderer.gregorianMonthPlainAr(today.monthValue)}, " +
                "${hijri.day} ${HijriCalendarEngine.monthName(hijri.month, true)} ${hijri.year}"

            GlanceTheme {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color.Transparent))
                        .clickable(tapAction)
                ) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = contentDesc,
                        contentScale = ContentScale.Fit,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

class CalligraphyMonthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalligraphyMonthWidget()
}

/**
 * Widget 2 (transparent): Ornate scalloped Quran brackets around QCF1-Madina Arabic Ayah +
 * calligraphic Surah name & Ayah rosette underneath. Tapping cycles to the next Quran Ayah (does not open app).
 */
class TransparentQuranVerseWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val glancePrefs = currentState<Preferences>()
            val verses = AzkarRepository.dailyVerses
            val rawIndex = glancePrefs[GLANCE_VERSE_INDEX_KEY] ?: prefs.quranWidgetVerseIndex
            val index = ((rawIndex % verses.size) + verses.size) % verses.size
            val verse = verses[index]

            val size = LocalSize.current
            val scale = context.resources.displayMetrics.density.coerceIn(2.0f, 3.2f)
            val widthDp = size.width.value.takeIf { it > 1f } ?: 300f
            val heightDp = size.height.value.takeIf { it > 1f } ?: 88f
            val widthPx = (widthDp * scale).roundToInt()
            val heightPx = (heightDp * scale).roundToInt()

            val bitmap = WidgetShapeRenderer.createTransparentQuranAyahBitmap(
                context = context,
                widthPx = widthPx,
                heightPx = heightPx,
                verse = verse
            )
            val tapAction = actionRunCallback<ChangeQuranVerseAction>()

            GlanceTheme {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color.Transparent))
                        .clickable(tapAction)
                ) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = "${verse.arabicText} — ${verse.referenceAr}",
                        contentScale = ContentScale.Fit,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

class TransparentQuranVerseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransparentQuranVerseWidget()
}

/**
 * Widget 3 (transparent): Pure white bespoke calligraphic weekday artwork (shadow-free) with
 * Gregorian • Hijri date line directly below.
 * Single tap opens the Calendar screen; double tap toggles bottom date visibility.
 */
class CalligraphyWeekdayWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()
        val tapAction = actionRunCallback<WeekdayWidgetTapAction>()

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val glancePrefs = currentState<Preferences>()
            val showBottomDate = glancePrefs[GLANCE_WEEKDAY_SHOW_BOTTOM_DATE_KEY] ?: prefs.weekdayWidgetShowBottomDate

            val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
            val today = LocalDate.now(zoneId)
            val hijri = HijriCalendarEngine.fromGregorian(today, prefs.hijriOffsetDays)

            val size = LocalSize.current
            val scale = context.resources.displayMetrics.density.coerceIn(2.0f, 3.2f)
            val widthDp = size.width.value.takeIf { it > 1f } ?: 260f
            val heightDp = size.height.value.takeIf { it > 1f } ?: 120f
            val widthPx = (widthDp * scale).roundToInt()
            val heightPx = (heightDp * scale).roundToInt()

            val bitmap = WidgetShapeRenderer.createTransparentWeekdayBitmap(
                context = context,
                widthPx = widthPx,
                heightPx = heightPx,
                today = today,
                hijri = hijri,
                showBottomDate = showBottomDate
            )
            val contentDesc = "${WidgetShapeRenderer.weekdayPlainAr(today.dayOfWeek)} — " +
                "${today.dayOfMonth} ${WidgetShapeRenderer.gregorianMonthPlainAr(today.monthValue)}, " +
                "${hijri.day} ${HijriCalendarEngine.monthName(hijri.month, true)}"

            GlanceTheme {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color.Transparent))
                        .clickable(tapAction)
                ) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = contentDesc,
                        contentScale = ContentScale.Fit,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

class CalligraphyWeekdayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalligraphyWeekdayWidget()
}
