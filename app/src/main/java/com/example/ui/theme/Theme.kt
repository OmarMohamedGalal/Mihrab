package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppColorPalette {
    TWILIGHT_PLUM,
    OASIS_EMERALD,
    MAKKAH_GOLD
}

private val TwilightLightScheme = lightColorScheme(
    primary = PlumPrimaryLight,
    onPrimary = PlumOnPrimaryLight,
    primaryContainer = PlumPrimaryContainerLight,
    onPrimaryContainer = PlumOnPrimaryContainerLight,
    secondary = PlumSecondaryLight,
    onSecondary = PlumOnSecondaryLight,
    secondaryContainer = PlumSecondaryContainerLight,
    onSecondaryContainer = PlumOnSecondaryContainerLight,
    tertiary = PlumTertiaryLight,
    onTertiary = PlumOnTertiaryLight,
    tertiaryContainer = PlumTertiaryContainerLight,
    onTertiaryContainer = PlumOnTertiaryContainerLight,
    background = PlumBackgroundLight,
    onBackground = PlumOnSurfaceLight,
    surface = PlumSurfaceLight,
    onSurface = PlumOnSurfaceLight,
    surfaceVariant = PlumSurfaceVariantLight,
    onSurfaceVariant = PlumOnSurfaceVariantLight,
    surfaceContainerLow = PlumSurfaceContainerLowLight,
    surfaceContainer = PlumSurfaceContainerLight,
    surfaceContainerHigh = PlumSurfaceContainerHighLight,
    outline = PlumOutlineLight,
    outlineVariant = PlumOutlineVariantLight
)

private val TwilightDarkScheme = darkColorScheme(
    primary = PlumPrimaryDark,
    onPrimary = PlumOnPrimaryDark,
    primaryContainer = PlumPrimaryContainerDark,
    onPrimaryContainer = PlumOnPrimaryContainerDark,
    secondary = PlumSecondaryDark,
    onSecondary = PlumOnSecondaryDark,
    secondaryContainer = PlumSecondaryContainerDark,
    onSecondaryContainer = PlumOnSecondaryContainerDark,
    tertiary = PlumTertiaryDark,
    onTertiary = PlumOnTertiaryDark,
    tertiaryContainer = PlumTertiaryContainerDark,
    onTertiaryContainer = PlumOnTertiaryContainerDark,
    background = PlumBackgroundDark,
    onBackground = PlumOnSurfaceDark,
    surface = PlumSurfaceDark,
    onSurface = PlumOnSurfaceDark,
    surfaceVariant = PlumSurfaceVariantDark,
    onSurfaceVariant = PlumOnSurfaceVariantDark,
    surfaceContainerLow = PlumSurfaceContainerLowDark,
    surfaceContainer = PlumSurfaceContainerDark,
    surfaceContainerHigh = PlumSurfaceContainerHighDark,
    outline = PlumOutlineDark,
    outlineVariant = PlumOutlineVariantDark
)

private val EmeraldLightScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = EmeraldPrimaryContainerLight,
    onPrimaryContainer = EmeraldOnPrimaryContainerLight,
    secondary = EmeraldSecondaryLight,
    onSecondary = EmeraldOnSecondaryLight,
    secondaryContainer = EmeraldSecondaryContainerLight,
    onSecondaryContainer = EmeraldOnSecondaryContainerLight,
    tertiary = EmeraldTertiaryLight,
    onTertiary = EmeraldOnTertiaryLight,
    tertiaryContainer = EmeraldTertiaryContainerLight,
    onTertiaryContainer = EmeraldOnTertiaryContainerLight,
    background = EmeraldBackgroundLight,
    surface = EmeraldBackgroundLight,
    surfaceContainerLow = EmeraldSurfaceContainerLowLight,
    surfaceContainer = EmeraldSurfaceContainerLight,
    surfaceContainerHigh = EmeraldSurfaceContainerHighLight
)

private val EmeraldDarkScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldPrimaryContainerDark,
    onPrimaryContainer = EmeraldOnPrimaryContainerDark,
    secondary = EmeraldSecondaryDark,
    onSecondary = EmeraldOnSecondaryDark,
    secondaryContainer = EmeraldSecondaryContainerDark,
    onSecondaryContainer = EmeraldOnSecondaryContainerDark,
    background = EmeraldBackgroundDark,
    surface = EmeraldBackgroundDark,
    surfaceContainerLow = EmeraldSurfaceContainerLowDark,
    surfaceContainer = EmeraldSurfaceContainerDark,
    surfaceContainerHigh = EmeraldSurfaceContainerHighDark
)

private val GoldLightScheme = lightColorScheme(
    primary = GoldPrimaryLight,
    onPrimary = GoldOnPrimaryLight,
    primaryContainer = GoldPrimaryContainerLight,
    onPrimaryContainer = GoldOnPrimaryContainerLight,
    secondary = GoldSecondaryLight,
    onSecondary = GoldOnSecondaryLight,
    secondaryContainer = GoldSecondaryContainerLight,
    onSecondaryContainer = GoldOnSecondaryContainerLight,
    tertiary = GoldTertiaryLight,
    onTertiary = GoldOnTertiaryLight,
    tertiaryContainer = GoldTertiaryContainerLight,
    onTertiaryContainer = GoldOnTertiaryContainerLight,
    background = GoldBackgroundLight,
    surface = GoldBackgroundLight,
    surfaceContainerLow = GoldSurfaceContainerLowLight,
    surfaceContainer = GoldSurfaceContainerLight,
    surfaceContainerHigh = GoldSurfaceContainerHighLight
)

private val GoldDarkScheme = darkColorScheme(
    primary = GoldPrimaryDark,
    onPrimary = GoldOnPrimaryDark,
    primaryContainer = GoldPrimaryContainerDark,
    onPrimaryContainer = GoldOnPrimaryContainerDark,
    secondary = PlumPrimaryDark,
    onSecondary = PlumOnPrimaryDark,
    background = GoldBackgroundDark,
    surface = GoldBackgroundDark,
    surfaceContainerLow = GoldSurfaceContainerLowDark,
    surfaceContainer = GoldSurfaceContainerDark,
    surfaceContainerHigh = GoldSurfaceContainerHighDark
)

val MihrabShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private fun ColorScheme.asAmoledBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A090F),
    surfaceContainer = Color(0xFF12101A),
    surfaceContainerHigh = Color(0xFF1A1724),
    surfaceContainerHighest = Color(0xFF231F30)
)

@Composable
fun MihrabTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    amoledDark: Boolean = false,
    palette: AppColorPalette = AppColorPalette.TWILIGHT_PLUM,
    isArabic: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (palette) {
            AppColorPalette.TWILIGHT_PLUM -> if (isDark) TwilightDarkScheme else TwilightLightScheme
            AppColorPalette.OASIS_EMERALD -> if (isDark) EmeraldDarkScheme else EmeraldLightScheme
            AppColorPalette.MAKKAH_GOLD -> if (isDark) GoldDarkScheme else GoldLightScheme
        }
    }

    val finalScheme = if (isDark && amoledDark) baseScheme.asAmoledBlack() else baseScheme

    MaterialTheme(
        colorScheme = finalScheme,
        typography = mihrabTypography(isArabic = isArabic),
        shapes = MihrabShapes,
        content = content
    )
}
