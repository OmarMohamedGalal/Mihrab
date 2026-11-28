package com.example.widgets

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.FontRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import com.example.MainActivity
import com.example.R
import com.example.data.azkar.DailyVerseItem
import com.example.data.preferences.UserPreferences
import com.example.domain.calendar.HijriCalendarEngine
import com.example.domain.calendar.HijriDate
import com.example.domain.prayer.PrayerType
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.max
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

    fun openAppIntent(context: Context, destination: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_WIDGET_DESTINATION, destination)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

    fun openAppAction(context: Context, destination: String): Action {
        return actionStartActivity(openAppIntent(context, destination))
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

    private fun loadTypeface(context: Context, @FontRes fontRes: Int, fallbackStyle: Int = Typeface.NORMAL): Typeface {
        val loaded = runCatching { ResourcesCompat.getFont(context, fontRes) }.getOrNull()
        return loaded ?: Typeface.create(Typeface.DEFAULT, fallbackStyle)
    }

    internal fun gregorianMonthPlainAr(month: Int): String = when (month) {
        1 -> "يناير"
        2 -> "فبراير"
        3 -> "مارس"
        4 -> "أبريل"
        5 -> "مايو"
        6 -> "يونيو"
        7 -> "يوليو"
        8 -> "أغسطس"
        9 -> "سبتمبر"
        10 -> "أكتوبر"
        11 -> "نوفمبر"
        12 -> "ديسمبر"
        else -> ""
    }

    internal fun toArabicIndicDigits(value: String): String = buildString {
        value.forEach { ch ->
            if (ch in '0'..'9') append(('٠'.code + (ch - '0')).toChar()) else append(ch)
        }
    }

    private fun gregorianMonthCalligraphyTemplate(month: Int): String = when (month) {
        1 -> "يَ|نَايِر"
        2 -> "فِبْ|رَايِر"
        3 -> "مَ|ارِس"
        4 -> "أَبْ|رِيل"
        5 -> "مَ|ايُو"
        6 -> "يُونْ|يُو"
        7 -> "يُولْ|يُو"
        8 -> "أَغُسْ|طُس"
        9 -> "سِبْتَمْ|بِر"
        10 -> "أُكْ|تُوبَر"
        11 -> "نُوفَ|مْبِر"
        else -> "دِيسَ|مْبِر"
    }

    internal fun gregorianMonthCalligraphyAr(month: Int): String =
        gregorianMonthCalligraphyTemplate(month).replace("|", "\u0640".repeat(10))

    internal fun weekdayPlainAr(day: DayOfWeek): String = when (day) {
        DayOfWeek.SATURDAY -> "السبت"
        DayOfWeek.SUNDAY -> "الأحد"
        DayOfWeek.MONDAY -> "الاثنين"
        DayOfWeek.TUESDAY -> "الثلاثاء"
        DayOfWeek.WEDNESDAY -> "الأربعاء"
        DayOfWeek.THURSDAY -> "الخميس"
        DayOfWeek.FRIDAY -> "الجمعة"
    }

    private fun weekdayCalligraphyTemplate(day: DayOfWeek): String = when (day) {
        DayOfWeek.SATURDAY -> "السَّ|بْت"
        DayOfWeek.SUNDAY -> "الأَحَ|د"
        DayOfWeek.MONDAY -> "الاثْ|نَيْن"
        DayOfWeek.TUESDAY -> "الثُّ|لَاثَاء"
        DayOfWeek.WEDNESDAY -> "الأَرْبِ|عَاء"
        DayOfWeek.THURSDAY -> "الخَمِ|يس"
        DayOfWeek.FRIDAY -> "الجُمُ|عَة"
    }

    internal fun weekdayCalligraphyAr(day: DayOfWeek): String =
        weekdayCalligraphyTemplate(day).replace("|", "\u0640".repeat(10))

    private fun fitCalligraphyTextAndSize(
        template: String,
        paint: TextPaint,
        targetWidth: Float,
        maxHeight: Float,
        initialSize: Float
    ): Pair<String, Rect> {
        var size = initialSize
        val minProbe = template.replace("|", "\u0640\u0640")
        paint.textSize = size
        val probeBounds = Rect()
        paint.getTextBounds(minProbe, 0, minProbe.length, probeBounds)

        if (probeBounds.height() > maxHeight && probeBounds.height() > 0) {
            size *= (maxHeight / probeBounds.height().toFloat())
            paint.textSize = size
            paint.getTextBounds(minProbe, 0, minProbe.length, probeBounds)
        }

        val minW = max(paint.measureText(minProbe), probeBounds.width().toFloat())
        if (minW > targetWidth && minW > 0f) {
            size *= (targetWidth / minW)
            paint.textSize = size
        }

        // Expand horizontal Kashida sweep so short and long Arabic words fill ~88-90% width uniformly
        var bestText = minProbe
        val bestBounds = Rect()
        paint.getTextBounds(bestText, 0, bestText.length, bestBounds)

        for (k in 2..48) {
            val candidate = template.replace("|", "\u0640".repeat(k))
            val b = Rect()
            paint.getTextBounds(candidate, 0, candidate.length, b)
            val wMeasured = max(paint.measureText(candidate), b.width().toFloat())
            if (wMeasured > targetWidth) break
            bestText = candidate
            bestBounds.set(b)
            if (wMeasured >= targetWidth * 0.96f) break
        }

        if (bestBounds.height() > maxHeight && bestBounds.height() > 0) {
            size *= (maxHeight / bestBounds.height().toFloat())
            paint.textSize = size
            paint.getTextBounds(bestText, 0, bestText.length, bestBounds)
        }

        return bestText to bestBounds
    }

    internal fun normalizeVerseForThuluth(raw: String): String {
        return raw
            .replace('\u0671', 'ا') // Alif Wasla -> standard Alif
            .replace("\u0640\u064E\u0654\u0627", "آ")
            .replace("\u0640\u0654\u0627", "آ")
            .replace("\u0627\u0653", "آ")
            .replace("\u0649\u0670\u0653", "ى")
            .replace("\u0649\u0653", "ي")
            .replace("\u0649\u0670", "ى")
            .replace("\u0648\u064E\u0670\u062A", "\u0648\u064E\u0627\u062A")
            .replace("\u0648\u0670\u062A", "\u0648\u064E\u0627\u062A")
            .replace("\u0648\u0670\u0629", "\u0627\u0629")
            .replace("\u0640\u0670", "ا")
            .replace("\u0650\u0649", "\u0650\u064A")
            .replace("\u0670", "")
            .replace("\u0653", "")
            .replace("\u0654", "")
            .replace("\u06DF", "")
            .replace("\u06E5", "")
            .replace("\u06D6", "")
            .replace("\u06D7", "")
            .replace("\u06DA", "")
            .replace(Regex("\\s*\u06DD\\s*"), "\n")
            .trim()
    }

    internal fun formatVerseForQcf1Madina(raw: String): String {
        return raw
            .replace(Regex("\\s*\u06DD\\s*"), "\n")
            .trim()
            .lines()
            .joinToString("\n") { line ->
                line.trim().replace(Regex(" +"), "  ")
            }
    }

    internal fun parseSurahAndAyahAr(referenceEn: String): Pair<Int, String> {
        val match = Regex("(\\d+):(\\d+(?:-\\d+)?)").find(referenceEn)
        val surahNum = match?.groupValues?.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 114) ?: 1
        val ayahRaw = match?.groupValues?.getOrNull(2).orEmpty()
        val ayahAr = buildString {
            ayahRaw.forEach { ch ->
                if (ch in '0'..'9') append(('٠'.code + (ch - '0')).toChar())
                else append(ch)
            }
        }
        return surahNum to ayahAr
    }

    internal fun surahNameGlyph(surahNumber: Int): String {
        val clamped = surahNumber.coerceIn(1, 114)
        return String(charArrayOf((0xF100 + clamped - 1).toChar()))
    }

    private fun drawAyahRosette(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        ayahNumberAr: String,
        numeralTypeface: Typeface,
        paint: Paint
    ) {
        val outerRing = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(1.0f, radius * 0.11f)
        }
        val innerRing = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(0.75f, radius * 0.065f)
        }
        val lobes = 8
        val petalPath = Path()
        val steps = 64
        for (i in 0..steps) {
            val a = (2.0 * PI * i) / steps
            val r = radius * (1.0f + 0.12f * cos(lobes * a).toFloat())
            val px = centerX + r * cos(a).toFloat()
            val py = centerY + r * sin(a).toFloat()
            if (i == 0) petalPath.moveTo(px, py) else petalPath.lineTo(px, py)
        }
        petalPath.close()
        canvas.drawPath(petalPath, outerRing)
        canvas.drawCircle(centerX, centerY, radius * 0.76f, innerRing)

        if (ayahNumberAr.isNotEmpty()) {
            val numPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(numeralTypeface, Typeface.BOLD)
                color = paint.color
                textAlign = Paint.Align.CENTER
                textSize = when {
                    ayahNumberAr.length >= 4 -> radius * 0.62f
                    ayahNumberAr.length == 3 -> radius * 0.74f
                    else -> radius * 0.88f
                }
            }
            val bounds = Rect()
            numPaint.getTextBounds(ayahNumberAr, 0, ayahNumberAr.length, bounds)
            val baselineY = centerY - (bounds.top + bounds.bottom) * 0.5f
            canvas.drawText(ayahNumberAr, centerX, baselineY, numPaint)
        }
    }

    private fun drawOrnateQuranBracket(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        height: Float,
        openRightSide: Boolean,
        paint: Paint
    ) {
        val dir = if (openRightSide) 1f else -1f
        val halfH = height * 0.5f
        val curveW = height * 0.28f

        // Outer scalloped crescent bracket matching Mushaf / reference Thuluth brackets (﴿ ﴾)
        val outer = Path().apply {
            moveTo(centerX - dir * curveW * 0.20f, centerY - halfH)
            cubicTo(
                centerX + dir * curveW * 0.74f, centerY - halfH * 0.62f,
                centerX + dir * curveW * 0.74f, centerY + halfH * 0.62f,
                centerX - dir * curveW * 0.20f, centerY + halfH
            )
        }
        val inner = Path().apply {
            moveTo(centerX - dir * curveW * 0.34f, centerY - halfH * 0.80f)
            cubicTo(
                centerX + dir * curveW * 0.30f, centerY - halfH * 0.40f,
                centerX + dir * curveW * 0.30f, centerY + halfH * 0.40f,
                centerX - dir * curveW * 0.34f, centerY + halfH * 0.80f
            )
        }

        val outerStroke = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(1.4f, height * 0.044f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val innerStroke = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(1.1f, height * 0.032f)
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(outer, outerStroke)
        canvas.drawPath(inner, innerStroke)

        // Delicate 3-point floral finial along outer spine
        val crestX = centerX + dir * curveW * 0.44f
        val petalFill = Paint(paint).apply {
            style = Paint.Style.FILL
        }
        val midLobe = Path().apply {
            moveTo(crestX, centerY - halfH * 0.14f)
            lineTo(crestX + dir * curveW * 0.44f, centerY)
            lineTo(crestX, centerY + halfH * 0.14f)
            close()
        }
        canvas.drawPath(midLobe, petalFill)

        listOf(-0.36f, 0.36f).forEach { yFactor ->
            val py = centerY + halfH * yFactor
            val px = centerX + dir * curveW * 0.32f
            canvas.drawCircle(px + dir * curveW * 0.14f, py, max(1.2f, height * 0.034f), petalFill)
        }
    }

    /**
     * Widget 1 (transparent): Faded Arabic-Indic day-of-month stacked behind wide sweeping
     * Dima Thuluth month calligraphy (shadow-free) with optional small Arabic-Indic Hijri date below.
     */
    fun createTransparentMonthDateBitmap(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        today: LocalDate,
        hijri: HijriDate,
        showBottomDate: Boolean = true
    ): Bitmap {
        val w = widthPx.coerceIn(320, 1280)
        val h = heightPx.coerceIn(140, 720)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val calliTypeface = loadTypeface(context, R.font.dima_thuluth)
        val subTypeface = loadTypeface(context, R.font.noto_sans_arabic)

        val dayDigits = toArabicIndicDigits("${today.dayOfMonth}")
        val hijriMonth = HijriCalendarEngine.monthName(hijri.month, true)
        val hijriDayAr = toArabicIndicDigits("${hijri.day}")
        val hijriYearAr = toArabicIndicDigits("${hijri.year}")
        val subText = "${weekdayPlainAr(today.dayOfWeek)}، $hijriDayAr $hijriMonth $hijriYearAr"

        val calliPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = calliTypeface
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
        }
        val maxCalliW = w * 0.90f
        val maxCalliH = h * 0.72f
        val template = gregorianMonthCalligraphyTemplate(today.monthValue)
        val (monthText, calliBounds) = fitCalligraphyTextAndSize(
            template = template,
            paint = calliPaint,
            targetWidth = maxCalliW,
            maxHeight = maxCalliH,
            initialSize = h * 0.62f
        )
        val calliSize = calliPaint.textSize

        // Measure exact horizontal Kashida bar vertical & horizontal center
        val kashidaBounds = Rect()
        val kashidaProbe = "\u0640\u0640\u0640"
        calliPaint.getTextBounds(kashidaProbe, 0, kashidaProbe.length, kashidaBounds)
        val kashidaW = calliPaint.measureText("\u0640")
        val prefixPart = template.substringBefore("|")
        val suffixPart = template.substringAfter("|")
        val prefixW = (calliPaint.measureText("$prefixPart\u0640") - kashidaW).coerceAtLeast(0f)
        val suffixW = (calliPaint.measureText("\u0640$suffixPart") - kashidaW).coerceAtLeast(0f)

        // Small, crisp bottom date line (no shadow)
        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = subTypeface
            color = 0xE0FFFFFF.toInt()
            textAlign = Paint.Align.CENTER
        }
        var subSize = min(h * 0.088f, calliSize * 0.24f).coerceAtLeast(13f)
        subPaint.textSize = subSize
        val maxSubW = w * 0.76f
        val measuredSubW = subPaint.measureText(subText)
        if (measuredSubW > maxSubW && measuredSubW > 0f) {
            subSize *= (maxSubW / measuredSubW)
            subPaint.textSize = subSize
        }

        val subBounds = Rect()
        subPaint.getTextBounds(subText, 0, subText.length, subBounds)

        val gapPx = max(6f, h * 0.048f)
        val calliH = calliBounds.height().toFloat().coerceAtLeast(calliSize * 0.64f)
        val subH = if (showBottomDate) subBounds.height().toFloat().coerceAtLeast(subSize) else 0f
        val totalStackH = if (showBottomDate) calliH + gapPx + subH else calliH

        val cx = w * 0.5f
        val stackTopY = ((h - totalStackH) * 0.50f).coerceIn(
            h * 0.04f,
            (h - totalStackH - h * 0.04f).coerceAtLeast(h * 0.04f)
        )
        val calliBaselineY = stackTopY - calliBounds.top
        val calliVisualBottom = calliBaselineY + calliBounds.bottom

        // Anchor Arabic-Indic day numerals directly over the open Kashida span,
        // sitting cleanly below the top diacritics and fading smoothly into the bar.
        val openSpanW = (calliBounds.width().toFloat() - prefixW - suffixW).coerceAtLeast(w * 0.18f)
        val kashidaCenterX = (cx + (suffixW - prefixW) * 0.5f).coerceIn(w * 0.36f, w * 0.64f)
        val kashidaBottomY = calliBaselineY + kashidaBounds.bottom.toFloat()
        val digitsTopY = stackTopY + calliH * 0.14f
        val digitsBottomY = kashidaBottomY.coerceAtLeast(digitsTopY + h * 0.20f)
        val targetDigitsH = (digitsBottomY - digitsTopY).coerceAtLeast(h * 0.22f)

        val digitsPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = calliTypeface
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            textSize = targetDigitsH * 1.45f
        }
        val digitsBounds = Rect()
        digitsPaint.getTextBounds(dayDigits, 0, dayDigits.length, digitsBounds)
        if (digitsBounds.height() > 0) {
            digitsPaint.textSize *= (targetDigitsH / digitsBounds.height().toFloat())
            digitsPaint.getTextBounds(dayDigits, 0, dayDigits.length, digitsBounds)
        }
        val maxDigitsW = min(w * 0.26f, openSpanW * 0.80f)
        if (digitsBounds.width() > maxDigitsW && digitsBounds.width() > 0) {
            digitsPaint.textSize *= (maxDigitsW / digitsBounds.width().toFloat())
            digitsPaint.getTextBounds(dayDigits, 0, dayDigits.length, digitsBounds)
        }

        val actualDigitsBottomY = digitsBottomY
        val actualDigitsTopY = actualDigitsBottomY - digitsBounds.height().toFloat()
        val digitsBaselineY = actualDigitsBottomY - digitsBounds.bottom
        digitsPaint.shader = LinearGradient(
            0f,
            actualDigitsTopY,
            0f,
            actualDigitsBottomY,
            intArrayOf(0xADFFFFFF.toInt(), 0x78FFFFFF, 0x22FFFFFF, 0x00FFFFFF),
            floatArrayOf(0f, 0.45f, 0.85f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawText(dayDigits, kashidaCenterX, digitsBaselineY, digitsPaint)

        canvas.drawText(monthText, cx, calliBaselineY, calliPaint)

        if (showBottomDate) {
            val subBaselineY = (calliVisualBottom + gapPx - subBounds.top).coerceAtMost(h - h * 0.04f)
            canvas.drawText(subText, cx, subBaselineY, subPaint)
        }

        return bitmap
    }

    /**
     * Widget 2 (transparent): QCF1-Madina Quran Ayah with ornate brackets anchored at the
     * exact start (top-right) and end (bottom-left) of the Ayah, plus calligraphic Surah name
     * & ornate Ayah rosette centered below.
     */
    fun createTransparentQuranAyahBitmap(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        verse: DailyVerseItem
    ): Bitmap {
        val w = widthPx.coerceIn(320, 1280)
        val h = heightPx.coerceIn(110, 640)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val quranTypeface = loadTypeface(context, R.font.qcf1_madina)
        val surahTypeface = loadTypeface(context, R.font.elgharib_surah_name)
        val numeralTypeface = loadTypeface(context, R.font.noto_sans_arabic)

        val arText = formatVerseForQcf1Madina(verse.arabicText)
        val (surahNumber, ayahAr) = parseSurahAndAyahAr(verse.referenceEn)
        val surahGlyph = surahNameGlyph(surahNumber)

        val arPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = quranTypeface
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(max(1.5f, h * 0.014f), 0f, 1f, 0x38000000)
        }
        val surahPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = surahTypeface
            color = 0xF5FFFFFF.toInt()
            textAlign = Paint.Align.CENTER
            setShadowLayer(max(1.2f, h * 0.011f), 0f, 1f, 0x38000000)
        }

        var arFontSize = (h * 0.28f).coerceAtLeast(20f)
        var arLayout: StaticLayout
        var surahBounds: Rect
        var gapPx: Float
        var rosetteR: Float
        var rosetteGap: Float
        val maxArCanvasW = (w * 0.70f).toInt().coerceAtLeast(140)
        val maxContentH = h * 0.88f

        var attempts = 0
        while (true) {
            arPaint.textSize = arFontSize
            val hasExplicitNewline = arText.contains('\n')
            val singleLineW = if (hasExplicitNewline) Float.MAX_VALUE else arPaint.measureText(arText)
            val targetArW = if (singleLineW <= w * 0.70f) {
                (singleLineW + 12f).toInt().coerceAtMost(maxArCanvasW)
            } else {
                maxArCanvasW
            }

            arLayout = StaticLayout.Builder.obtain(arText, 0, arText.length, arPaint, targetArW.coerceAtLeast(100))
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .setLineSpacing(0f, 1.08f)
                .build()

            // Scale Surah name glyph visually to ~50% of Ayah font size (matching reference photo proportion)
            val targetSurahH = (arFontSize * 0.50f).coerceIn(16f, h * 0.15f)
            surahPaint.textSize = targetSurahH
            surahBounds = Rect()
            surahPaint.getTextBounds(surahGlyph, 0, surahGlyph.length, surahBounds)
            if (surahBounds.height() > 0) {
                surahPaint.textSize *= (targetSurahH / surahBounds.height().toFloat())
                surahPaint.getTextBounds(surahGlyph, 0, surahGlyph.length, surahBounds)
            }

            rosetteR = (surahBounds.height().toFloat().coerceAtLeast(16f) * 0.40f).coerceAtLeast(8.5f)
            rosetteGap = max(6f, rosetteR * 0.55f)
            gapPx = max(6f, h * 0.055f)

            val footerH = max(surahBounds.height().toFloat(), rosetteR * 2.2f)
            val totalH = arLayout.height.toFloat() + gapPx + footerH
            val bracketSpace = arFontSize * 1.25f
            val fitsHorizontally = (0 until arLayout.lineCount).all { lineIdx ->
                (arLayout.getLineRight(lineIdx) - arLayout.getLineLeft(lineIdx)) + bracketSpace * 2f <= w * 0.94f
            }

            if ((totalH <= maxContentH && arLayout.lineCount <= 2 && fitsHorizontally) ||
                arFontSize <= 16f ||
                attempts >= 12
            ) {
                if (totalH > maxContentH && arFontSize > 14f && attempts < 18) {
                    arFontSize *= 0.88f
                    attempts++
                    continue
                }
                break
            }
            arFontSize *= if (arLayout.lineCount > 2) 0.84f else 0.90f
            attempts++
        }

        val arHeight = arLayout.height.toFloat()
        val footerH = max(surahBounds.height().toFloat(), rosetteR * 2.2f)
        val totalH = arHeight + gapPx + footerH
        val topY = ((h - totalH) * 0.5f).coerceAtLeast(4f)
        val cx = w * 0.5f
        val arOffsetX = cx - arLayout.width * 0.5f

        canvas.save()
        canvas.translate(arOffsetX, topY)
        arLayout.draw(canvas)
        canvas.restore()

        // Anchor opening bracket directly at the RTL start of the Ayah (line 0 right edge)
        // and closing bracket directly at the RTL end of the Ayah (last line left edge)
        val arFm = arPaint.fontMetrics
        val opticalCenterOffset = (arFm.ascent + arFm.descent) * 0.34f
        val bracketH = (arFontSize * 1.22f).coerceAtLeast(26f)
        val bracketPad = arFontSize * 0.54f

        val firstLine = 0
        val lastLine = (arLayout.lineCount - 1).coerceAtLeast(0)
        val startRightX = (arOffsetX + arLayout.getLineRight(firstLine) + bracketPad)
            .coerceAtMost(w - bracketH * 0.35f)
        val startCenterY = topY + arLayout.getLineBaseline(firstLine).toFloat() + opticalCenterOffset

        val endLeftX = (arOffsetX + arLayout.getLineLeft(lastLine) - bracketPad * 1.12f)
            .coerceAtLeast(bracketH * 0.35f)
        val endCenterY = topY + arLayout.getLineBaseline(lastLine).toFloat() + opticalCenterOffset

        drawOrnateQuranBracket(canvas, startRightX, startCenterY, bracketH, openRightSide = true, paint = arPaint)
        drawOrnateQuranBracket(canvas, endLeftX, endCenterY, bracketH, openRightSide = false, paint = arPaint)

        // Centered calligraphic Surah name + Ayah rosette medallion directly under the Ayah
        val footerCenterY = topY + arHeight + gapPx + footerH * 0.5f
        val surahW = max(surahPaint.measureText(surahGlyph), surahBounds.width().toFloat())
        val rosetteD = rosetteR * 2f
        val groupW = rosetteD + rosetteGap + surahW
        val groupLeft = cx - groupW * 0.5f
        val rosetteCenterX = groupLeft + rosetteR
        val surahCenterX = groupLeft + rosetteD + rosetteGap + surahW * 0.5f
        val surahBaselineY = footerCenterY - (surahBounds.top + surahBounds.bottom) * 0.5f

        drawAyahRosette(
            canvas = canvas,
            centerX = rosetteCenterX,
            centerY = footerCenterY,
            radius = rosetteR,
            ayahNumberAr = ayahAr,
            numeralTypeface = numeralTypeface,
            paint = surahPaint
        )
        canvas.drawText(surahGlyph, surahCenterX, surahBaselineY, surahPaint)

        return bitmap
    }

    /**
     * Widget 3 (transparent): Wide sweeping Dima Thuluth Arabic weekday calligraphy
     * (shadow-free) + optional small Gregorian • Hijri date line directly below.
     */
    fun createTransparentWeekdayBitmap(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        today: LocalDate,
        hijri: HijriDate,
        showBottomDate: Boolean = true
    ): Bitmap {
        val w = widthPx.coerceIn(320, 1280)
        val h = heightPx.coerceIn(140, 720)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val calliTypeface = loadTypeface(context, R.font.dima_thuluth)
        val subTypeface = loadTypeface(context, R.font.noto_sans_arabic)

        val monthPlain = gregorianMonthPlainAr(today.monthValue)
        val hijriMonth = HijriCalendarEngine.monthName(hijri.month, true)
        val subText = "${today.dayOfMonth} $monthPlain  •  ${hijri.day} $hijriMonth"

        val calliPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = calliTypeface
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
        }
        val maxCalliW = w * 0.90f
        val maxCalliH = h * 0.72f
        val (weekdayText, calliBounds) = fitCalligraphyTextAndSize(
            template = weekdayCalligraphyTemplate(today.dayOfWeek),
            paint = calliPaint,
            targetWidth = maxCalliW,
            maxHeight = maxCalliH,
            initialSize = h * 0.66f
        )
        val calliSize = calliPaint.textSize

        // Small, crisp bottom date line (no shadow)
        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = subTypeface
            color = 0xE0FFFFFF.toInt()
            textAlign = Paint.Align.CENTER
        }
        var subSize = min(h * 0.095f, calliSize * 0.25f).coerceAtLeast(13f)
        subPaint.textSize = subSize
        val maxSubW = w * 0.76f
        val measuredSubW = subPaint.measureText(subText)
        if (measuredSubW > maxSubW && measuredSubW > 0f) {
            subSize *= (maxSubW / measuredSubW)
            subPaint.textSize = subSize
        }

        val subBounds = Rect()
        subPaint.getTextBounds(subText, 0, subText.length, subBounds)

        val gapPx = max(8f, h * 0.070f)
        val calliH = calliBounds.height().toFloat().coerceAtLeast(calliSize * 0.64f)
        val subH = if (showBottomDate) subBounds.height().toFloat().coerceAtLeast(subSize) else 0f
        val totalStackH = if (showBottomDate) calliH + gapPx + subH else calliH

        val cx = w * 0.5f
        val stackTopY = ((h - totalStackH) * 0.50f).coerceIn(
            h * 0.04f,
            (h - totalStackH - h * 0.04f).coerceAtLeast(h * 0.04f)
        )
        val calliBaselineY = stackTopY - calliBounds.top
        val calliVisualBottom = calliBaselineY + calliBounds.bottom

        canvas.drawText(weekdayText, cx, calliBaselineY, calliPaint)

        if (showBottomDate) {
            val subBaselineY = (calliVisualBottom + gapPx - subBounds.top).coerceAtMost(h - h * 0.04f)
            canvas.drawText(subText, cx, subBaselineY, subPaint)
        }

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
            val w = DayCardWidget()
            manager.getGlanceIds(DayCardWidget::class.java).forEach { w.update(context, it) }
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
            val w = CalligraphyMonthWidget()
            manager.getGlanceIds(CalligraphyMonthWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = TransparentQuranVerseWidget()
            manager.getGlanceIds(TransparentQuranVerseWidget::class.java).forEach { w.update(context, it) }
        }
        runCatching {
            val w = CalligraphyWeekdayWidget()
            manager.getGlanceIds(CalligraphyWeekdayWidget::class.java).forEach { w.update(context, it) }
        }
    }
}
