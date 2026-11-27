package com.example.widgets

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import com.example.MainActivity
import com.example.data.preferences.UserPreferences
import com.example.domain.prayer.PrayerType
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.EmeraldBackgroundDark
import com.example.ui.theme.EmeraldBackgroundLight
import com.example.ui.theme.EmeraldPrimaryContainerDark
import com.example.ui.theme.EmeraldPrimaryContainerLight
import com.example.ui.theme.EmeraldPrimaryDark
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.GoldBackgroundDark
import com.example.ui.theme.GoldBackgroundLight
import com.example.ui.theme.GoldPrimaryContainerDark
import com.example.ui.theme.GoldPrimaryContainerLight
import com.example.ui.theme.GoldPrimaryDark
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.PlumBackgroundDark
import com.example.ui.theme.PlumBackgroundLight
import com.example.ui.theme.PlumOnPrimaryContainerDark
import com.example.ui.theme.PlumOnPrimaryContainerLight
import com.example.ui.theme.PlumPrimaryContainerDark
import com.example.ui.theme.PlumPrimaryContainerLight
import com.example.ui.theme.PlumPrimaryDark
import com.example.ui.theme.PlumPrimaryLight
import com.example.ui.theme.PlumSecondaryContainerDark
import com.example.ui.theme.PlumSecondaryContainerLight
import com.example.ui.theme.PlumSurfaceContainerHighDark
import com.example.ui.theme.PlumSurfaceContainerHighLight
import com.example.ui.theme.ThemeMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object WidgetDestinations {
    const val EXTRA_WIDGET_DESTINATION = "extra_widget_destination"
    const val PRAYER = "prayer"
    const val QIBLA = "qibla"
    const val CALENDAR = "calendar"

    fun openAppAction(context: Context, destination: String): Action {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_WIDGET_DESTINATION, destination)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return actionStartActivity(intent)
    }
}

data class ResolvedWidgetPalette(
    @ColorInt val primary: Int,
    @ColorInt val onPrimary: Int,
    @ColorInt val primaryContainer: Int,
    @ColorInt val onPrimaryContainer: Int,
    @ColorInt val secondaryContainer: Int,
    @ColorInt val surfaceContainer: Int,
    @ColorInt val onSurfaceVariant: Int,
    @ColorInt val sunGold: Int,
    @ColorInt val sunHighlight: Int,
    val isDark: Boolean
) {
    val primaryColor: Color get() = Color(primary)
    val onPrimaryColor: Color get() = Color(onPrimary)
    val primaryContainerColor: Color get() = Color(primaryContainer)
    val onPrimaryContainerColor: Color get() = Color(onPrimaryContainer)
    val secondaryContainerColor: Color get() = Color(secondaryContainer)
    val surfaceContainerColor: Color get() = Color(surfaceContainer)
    val onSurfaceVariantColor: Color get() = Color(onSurfaceVariant)
}

object WidgetThemeResolver {
    fun resolve(context: Context, prefs: UserPreferences): ResolvedWidgetPalette {
        val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val isDark = when (prefs.themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> systemDark
        }

        if (prefs.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return if (isDark) {
                ResolvedWidgetPalette(
                    primary = ContextCompat.getColor(context, android.R.color.system_accent1_200),
                    onPrimary = ContextCompat.getColor(context, android.R.color.system_accent1_800),
                    primaryContainer = ContextCompat.getColor(context, android.R.color.system_accent1_700),
                    onPrimaryContainer = ContextCompat.getColor(context, android.R.color.system_accent1_50),
                    secondaryContainer = ContextCompat.getColor(context, android.R.color.system_accent2_700),
                    surfaceContainer = if (prefs.amoledDark) Color(0xFF0C0C0F).toArgb()
                    else ContextCompat.getColor(context, android.R.color.system_neutral1_900),
                    onSurfaceVariant = ContextCompat.getColor(context, android.R.color.system_neutral2_200),
                    sunGold = Color(0xFFFFCA28).toArgb(),
                    sunHighlight = Color(0xFFFFF3C4).toArgb(),
                    isDark = true
                )
            } else {
                ResolvedWidgetPalette(
                    primary = ContextCompat.getColor(context, android.R.color.system_accent1_600),
                    onPrimary = ContextCompat.getColor(context, android.R.color.system_neutral1_0),
                    primaryContainer = ContextCompat.getColor(context, android.R.color.system_accent1_100),
                    onPrimaryContainer = ContextCompat.getColor(context, android.R.color.system_accent1_900),
                    secondaryContainer = ContextCompat.getColor(context, android.R.color.system_accent2_100),
                    surfaceContainer = ContextCompat.getColor(context, android.R.color.system_neutral1_50),
                    onSurfaceVariant = ContextCompat.getColor(context, android.R.color.system_neutral2_700),
                    sunGold = Color(0xFFF6B700).toArgb(),
                    sunHighlight = Color(0xFFFFF6D6).toArgb(),
                    isDark = false
                )
            }
        }

        return when (prefs.palette) {
            AppColorPalette.OASIS_EMERALD -> if (isDark) {
                ResolvedWidgetPalette(
                    primary = EmeraldPrimaryDark.toArgb(),
                    onPrimary = Color(0xFF00382A).toArgb(),
                    primaryContainer = EmeraldPrimaryContainerDark.toArgb(),
                    onPrimaryContainer = Color(0xFFA2F2D6).toArgb(),
                    secondaryContainer = Color(0xFF1E3A30).toArgb(),
                    surfaceContainer = if (prefs.amoledDark) Color.Black.toArgb() else EmeraldBackgroundDark.toArgb(),
                    onSurfaceVariant = Color(0xFFB7CCC3).toArgb(),
                    sunGold = Color(0xFFFFCA28).toArgb(),
                    sunHighlight = Color(0xFFFFF1C1).toArgb(),
                    isDark = true
                )
            } else {
                ResolvedWidgetPalette(
                    primary = EmeraldPrimaryLight.toArgb(),
                    onPrimary = Color.White.toArgb(),
                    primaryContainer = EmeraldPrimaryContainerLight.toArgb(),
                    onPrimaryContainer = Color(0xFF002117).toArgb(),
                    secondaryContainer = Color(0xFFDDEEE6).toArgb(),
                    surfaceContainer = EmeraldBackgroundLight.toArgb(),
                    onSurfaceVariant = Color(0xFF3E524A).toArgb(),
                    sunGold = Color(0xFFF6B700).toArgb(),
                    sunHighlight = Color(0xFFFFF4CC).toArgb(),
                    isDark = false
                )
            }

            AppColorPalette.MAKKAH_GOLD -> if (isDark) {
                ResolvedWidgetPalette(
                    primary = GoldPrimaryDark.toArgb(),
                    onPrimary = Color(0xFF422C00).toArgb(),
                    primaryContainer = GoldPrimaryContainerDark.toArgb(),
                    onPrimaryContainer = Color(0xFFFFDF9F).toArgb(),
                    secondaryContainer = Color(0xFF3B2F18).toArgb(),
                    surfaceContainer = if (prefs.amoledDark) Color.Black.toArgb() else GoldBackgroundDark.toArgb(),
                    onSurfaceVariant = Color(0xFFD5C7AE).toArgb(),
                    sunGold = Color(0xFFFFCA28).toArgb(),
                    sunHighlight = Color(0xFFFFF1C1).toArgb(),
                    isDark = true
                )
            } else {
                ResolvedWidgetPalette(
                    primary = GoldPrimaryLight.toArgb(),
                    onPrimary = Color.White.toArgb(),
                    primaryContainer = GoldPrimaryContainerLight.toArgb(),
                    onPrimaryContainer = Color(0xFF271900).toArgb(),
                    secondaryContainer = Color(0xFFF5E6C8).toArgb(),
                    surfaceContainer = GoldBackgroundLight.toArgb(),
                    onSurfaceVariant = Color(0xFF564A36).toArgb(),
                    sunGold = Color(0xFFF6B700).toArgb(),
                    sunHighlight = Color(0xFFFFF4CC).toArgb(),
                    isDark = false
                )
            }

            AppColorPalette.TWILIGHT_PLUM -> if (isDark) {
                ResolvedWidgetPalette(
                    primary = PlumPrimaryDark.toArgb(),
                    onPrimary = Color(0xFF331478).toArgb(),
                    primaryContainer = PlumPrimaryContainerDark.toArgb(),
                    onPrimaryContainer = PlumOnPrimaryContainerDark.toArgb(),
                    secondaryContainer = PlumSecondaryContainerDark.toArgb(),
                    surfaceContainer = if (prefs.amoledDark) Color.Black.toArgb() else PlumSurfaceContainerHighDark.toArgb(),
                    onSurfaceVariant = Color(0xFFCCC2D8).toArgb(),
                    sunGold = Color(0xFFFFCA28).toArgb(),
                    sunHighlight = Color(0xFFFFF1C1).toArgb(),
                    isDark = true
                )
            } else {
                ResolvedWidgetPalette(
                    primary = PlumPrimaryLight.toArgb(),
                    onPrimary = Color.White.toArgb(),
                    primaryContainer = PlumPrimaryContainerLight.toArgb(),
                    onPrimaryContainer = PlumOnPrimaryContainerLight.toArgb(),
                    secondaryContainer = PlumSecondaryContainerLight.toArgb(),
                    surfaceContainer = PlumSurfaceContainerHighLight.toArgb(),
                    onSurfaceVariant = Color(0xFF494554).toArgb(),
                    sunGold = Color(0xFFF6B700).toArgb(),
                    sunHighlight = Color(0xFFFFF4CC).toArgb(),
                    isDark = false
                )
            }
        }
    }
}

object WidgetShapeRenderer {

    /**
     * Generates the Material 3 Expressive asymmetric tilted Weather Pebble / Blob shape
     * with a two-tone prayer sun/crescent emblem in the lower-left corner.
     */
    fun createWeatherPebbleBitmap(
        widthPx: Int,
        heightPx: Int,
        prayerType: PrayerType,
        colors: ResolvedWidgetPalette,
        includeEmblem: Boolean = true
    ): Bitmap {
        val w = widthPx.coerceIn(120, 900)
        val h = heightPx.coerceIn(120, 900)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = w / 2f
        val cy = h / 2f
        val rx = w * 0.485f
        val ry = h * 0.475f

        // Smooth organic pebble contour (subtle 2-lobe & 3-lobe asymmetry matching M3 Expressive weather pill)
        val pebblePath = Path()
        val steps = 128
        for (i in 0..steps) {
            val theta = (2.0 * PI * i) / steps
            val radialMod = 1f +
                0.055f * cos(2.0 * (theta - 0.65)).toFloat() +
                0.032f * sin(3.0 * (theta + 0.35)).toFloat()
            val x = cx + rx * radialMod * cos(theta).toFloat()
            val y = cy + ry * radialMod * sin(theta).toFloat()
            if (i == 0) pebblePath.moveTo(x, y) else pebblePath.lineTo(x, y)
        }
        pebblePath.close()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primaryContainer
            style = Paint.Style.FILL
        }
        canvas.drawPath(pebblePath, bgPaint)

        // Subtle inner highlight ring
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primary
            alpha = 40
            style = Paint.Style.STROKE
            strokeWidth = (min(w, h) * 0.012f).coerceAtLeast(1.5f)
        }
        canvas.drawPath(pebblePath, strokePaint)

        if (includeEmblem) {
            val emblemR = min(w, h) * 0.145f
            val ex = w * 0.27f
            val ey = h * 0.68f
            val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.sunGold
                style = Paint.Style.FILL
            }
            val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.sunHighlight
                alpha = 215
                style = Paint.Style.FILL
            }

            when (prayerType) {
                PrayerType.ISHA, PrayerType.FAJR -> {
                    // Crescent moon + small soft mist/glow
                    canvas.drawCircle(ex, ey, emblemR, sunPaint)
                    val cutout = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colors.primaryContainer
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(ex + emblemR * 0.38f, ey - emblemR * 0.22f, emblemR * 0.85f, cutout)
                    if (prayerType == PrayerType.FAJR) {
                        canvas.drawOval(
                            RectF(ex - emblemR * 0.4f, ey + emblemR * 0.35f, ex + emblemR * 1.15f, ey + emblemR * 1.05f),
                            cloudPaint
                        )
                    }
                }
                else -> {
                    // Two-tone Weather-style Sun + translucent mist/cloud overlapping!
                    canvas.drawCircle(ex, ey, emblemR, sunPaint)
                    canvas.drawCircle(ex + emblemR * 0.42f, ey + emblemR * 0.42f, emblemR * 0.62f, cloudPaint)
                }
            }
        }

        return bitmap
    }

    /**
     * Generates the Material 3 Expressive Circle Qibla Compass bitmap inspired by the
     * expressive circular clock widget, showing bold N/E/S/W labels, bearing needle, and Kaaba badge.
     */
    fun createQiblaCircleBitmap(
        sizePx: Int,
        qiblaBearingDeg: Double,
        colors: ResolvedWidgetPalette
    ): Bitmap {
        val s = sizePx.coerceIn(140, 720)
        val bitmap = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = s / 2f
        val cy = s / 2f
        val radius = s * 0.47f

        // Base circular background
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primaryContainer
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius, circlePaint)

        // Inner subtle compass track ring
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primary
            alpha = 45
            style = Paint.Style.STROKE
            strokeWidth = s * 0.018f
        }
        canvas.drawCircle(cx, cy, radius * 0.86f, trackPaint)

        // Cardinal letters matching the oversized clock numeral feel
        val cardinalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primary
            alpha = 140
            textSize = s * 0.135f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val fm = cardinalPaint.fontMetrics
        val textOffsetY = -(fm.ascent + fm.descent) / 2f
        val rimDist = radius * 0.72f
        canvas.drawText("N", cx, cy - rimDist + textOffsetY, cardinalPaint)
        canvas.drawText("E", cx + rimDist, cy + textOffsetY, cardinalPaint)
        canvas.drawText("S", cx, cy + rimDist + textOffsetY, cardinalPaint)
        canvas.drawText("W", cx - rimDist, cy + textOffsetY, cardinalPaint)

        // Rotated Qibla bearing pointer needle
        canvas.save()
        canvas.rotate(qiblaBearingDeg.toFloat(), cx, cy)

        val needlePath = Path().apply {
            moveTo(cx, cy - radius * 0.64f)
            lineTo(cx + s * 0.055f, cy - radius * 0.14f)
            lineTo(cx - s * 0.055f, cy - radius * 0.14f)
            close()
        }
        val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primary
            style = Paint.Style.FILL
        }
        canvas.drawPath(needlePath, needlePaint)

        // Kaaba gold square tip at the Qibla azimuth
        val kaabaSize = s * 0.075f
        val kaabaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.sunGold
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(
                cx - kaabaSize / 2f,
                cy - radius * 0.73f,
                cx + kaabaSize / 2f,
                cy - radius * 0.73f + kaabaSize
            ),
            kaabaSize * 0.25f,
            kaabaSize * 0.25f,
            kaabaPaint
        )
        canvas.restore()

        // Center hub circle
        val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.primary
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, s * 0.045f, hubPaint)

        return bitmap
    }
}

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        runCatching {
            val w = PrayerGlanceWidget()
            manager.getGlanceIds(PrayerGlanceWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = QuranVerseWidget()
            manager.getGlanceIds(QuranVerseWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = HijriDateWidget()
            manager.getGlanceIds(HijriDateWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = QiblaCircleWidget()
            manager.getGlanceIds(QiblaCircleWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = PrayerTimetableWidget()
            manager.getGlanceIds(PrayerTimetableWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = DayCardWidget()
            manager.getGlanceIds(DayCardWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = QuranAyahWidget()
            manager.getGlanceIds(QuranAyahWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = DayDateWidget()
            manager.getGlanceIds(DayDateWidget::class.java).forEach { w.update(context, it) }
        }
    }
}
