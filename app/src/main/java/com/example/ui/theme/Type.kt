package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val BricolageDisplayFamily = FontFamily(
    Font(R.font.bricolage_grotesque, FontWeight.Normal),
    Font(R.font.bricolage_grotesque, FontWeight.Medium),
    Font(R.font.bricolage_grotesque, FontWeight.SemiBold),
    Font(R.font.bricolage_grotesque, FontWeight.Bold),
    Font(R.font.bricolage_grotesque, FontWeight.ExtraBold)
)

val NotoSansArabicFamily = FontFamily(
    Font(R.font.noto_sans_arabic, FontWeight.Light),
    Font(R.font.noto_sans_arabic, FontWeight.Normal),
    Font(R.font.noto_sans_arabic, FontWeight.Medium),
    Font(R.font.noto_sans_arabic, FontWeight.SemiBold),
    Font(R.font.noto_sans_arabic, FontWeight.Bold),
    Font(R.font.noto_sans_arabic, FontWeight.ExtraBold)
)

val RobotoFlexFamily = FontFamily(
    Font(R.font.roboto_flex, FontWeight.Light),
    Font(R.font.roboto_flex, FontWeight.Normal),
    Font(R.font.roboto_flex, FontWeight.Medium),
    Font(R.font.roboto_flex, FontWeight.SemiBold),
    Font(R.font.roboto_flex, FontWeight.Bold),
    Font(R.font.roboto_flex, FontWeight.ExtraBold)
)

val RobotoSerifFamily = FontFamily(
    Font(R.font.roboto_serif, FontWeight.Normal),
    Font(R.font.roboto_serif, FontWeight.Medium),
    Font(R.font.roboto_serif, FontWeight.SemiBold),
    Font(R.font.roboto_serif, FontWeight.Bold)
)

val RobotoMonoFamily = FontFamily(
    Font(R.font.roboto_mono, FontWeight.Light),
    Font(R.font.roboto_mono, FontWeight.Normal),
    Font(R.font.roboto_mono, FontWeight.Medium),
    Font(R.font.roboto_mono, FontWeight.SemiBold),
    Font(R.font.roboto_mono, FontWeight.Bold)
)

val AmiriFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Bold)
)

val AmiriQuranFamily = FontFamily(
    Font(R.font.amiri_quran, FontWeight.Normal),
    Font(R.font.amiri_quran, FontWeight.Bold)
)

val ScheherazadeNewFamily = FontFamily(
    Font(R.font.scheherazade_new, FontWeight.Normal),
    Font(R.font.scheherazade_new, FontWeight.Bold)
)

fun mihrabTypography(isArabic: Boolean = false): Typography {
    val displayTitleFont = if (isArabic) NotoSansArabicFamily else BricolageDisplayFamily
    val uiFont = if (isArabic) NotoSansArabicFamily else RobotoFlexFamily

    return Typography(
        displayLarge = TextStyle(
            fontFamily = displayTitleFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isArabic) 48.sp else 56.sp,
            lineHeight = if (isArabic) 56.sp else 58.sp,
            letterSpacing = if (isArabic) 0.sp else (-1.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = displayTitleFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isArabic) 42.sp else 46.sp,
            lineHeight = if (isArabic) 50.sp else 48.sp,
            letterSpacing = if (isArabic) 0.sp else (-1.2).sp
        ),
        displaySmall = TextStyle(
            fontFamily = displayTitleFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isArabic) 36.sp else 40.sp,
            lineHeight = if (isArabic) 44.sp else 42.sp,
            letterSpacing = if (isArabic) 0.sp else (-1.0).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = displayTitleFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isArabic) 34.sp else 38.sp,
            lineHeight = if (isArabic) 42.sp else 42.sp,
            letterSpacing = if (isArabic) 0.sp else (-1.0).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = if (isArabic) 0.sp else (-0.4).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = displayTitleFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = if (isArabic) 0.sp else (-0.3).sp
        ),
        titleLarge = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 26.sp
        ),
        titleMedium = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = if (isArabic) 0.sp else 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = if (isArabic) 0.sp else 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = if (isArabic) 0.sp else 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = if (isArabic) 0.sp else 0.2.sp
        ),
        bodySmall = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            letterSpacing = if (isArabic) 0.sp else 0.3.sp
        ),
        labelLarge = TextStyle(
            fontFamily = uiFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = if (isArabic) 0.sp else 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = if (isArabic) 0.sp else 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = if (isArabic) 0.sp else 0.4.sp
        )
    )
}

val MihrabTypography = mihrabTypography(isArabic = false)
