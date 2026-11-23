package com.example.presentation.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.AppStrings
import com.example.data.local.QuranVerseEntity
import com.example.data.quran.MushafLine
import com.example.data.quran.MushafPageLineLayout
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranScriptFormatter
import com.example.data.quran.SurahMeta
import com.example.data.quran.TajweedRule
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.AmiriQuranFamily
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.ScheherazadeNewFamily

internal data class MushafPalette(
    val pageBg: Color,
    val sheetBg: Color,
    val innerPageBg: Color,
    val chromeBg: Color,
    val chromeInk: Color,
    val frameBorder: Color,
    val innerFrameBorder: Color,
    val inkColor: Color,
    val secondaryInk: Color,
    val goldAccent: Color,
    val emeraldAccent: Color,
    val highlightBg: Color,
    val ornamentColor: Color,
    val ghunnahColor: Color,
    val qalqalahColor: Color,
    val maddColor: Color,
    val ikhfaColor: Color
)

@Composable
internal fun rememberMushafPalette(theme: QuranReadingTheme): MushafPalette {
    val scheme = MaterialTheme.colorScheme
    return when (theme) {
        QuranReadingTheme.KSU_MUSHAF -> MushafPalette(
            pageBg = scheme.background,
            sheetBg = scheme.surfaceContainerLow,
            // True ivory mushaf paper in light mode (the Material surface is lavender-tinted);
            // keep the dark surface when the app itself is in dark mode.
            innerPageBg = if (scheme.background.luminance() > 0.5f) Color(0xFFFAF6EC) else scheme.surface,
            chromeBg = scheme.primaryContainer,
            chromeInk = scheme.onPrimaryContainer,
            frameBorder = scheme.primary.copy(alpha = 0.72f),
            innerFrameBorder = scheme.outlineVariant,
            inkColor = scheme.onSurface,
            secondaryInk = scheme.onSurfaceVariant,
            goldAccent = scheme.tertiary,
            emeraldAccent = scheme.primary,
            highlightBg = scheme.primary.copy(alpha = 0.16f),
            ornamentColor = scheme.tertiary,
            ghunnahColor = Color(0xFF156E31),
            qalqalahColor = Color(0xFF0E5A9E),
            maddColor = Color(0xFFB21B1B),
            ikhfaColor = Color(0xFFC25104)
        )
        QuranReadingTheme.DARK -> MushafPalette(
            pageBg = Color(0xFF121417),
            sheetBg = Color(0xFF1B1E22),
            innerPageBg = Color(0xFF1E2228),
            chromeBg = scheme.primaryContainer,
            chromeInk = scheme.onPrimaryContainer,
            frameBorder = scheme.primary.copy(alpha = 0.55f),
            innerFrameBorder = scheme.outlineVariant,
            inkColor = Color(0xFFEDEFF2),
            secondaryInk = Color(0xFFA2AAB2),
            goldAccent = scheme.tertiary,
            emeraldAccent = scheme.primary,
            highlightBg = scheme.primary.copy(alpha = 0.18f),
            ornamentColor = scheme.tertiary,
            ghunnahColor = Color(0xFF66BB6A),
            qalqalahColor = Color(0xFF42A5F5),
            maddColor = Color(0xFFEF5350),
            ikhfaColor = Color(0xFFFFA726)
        )
        QuranReadingTheme.SEPIA -> MushafPalette(
            pageBg = Color(0xFFF4E7CE),
            sheetBg = Color(0xFFFBF0D9),
            innerPageBg = Color(0xFFFCF4E2),
            chromeBg = scheme.primaryContainer,
            chromeInk = scheme.onPrimaryContainer,
            frameBorder = scheme.primary.copy(alpha = 0.65f),
            innerFrameBorder = Color(0xFFCDA767),
            inkColor = Color(0xFF3B2C1B),
            secondaryInk = Color(0xFF705C44),
            goldAccent = scheme.tertiary,
            emeraldAccent = scheme.primary,
            highlightBg = scheme.primary.copy(alpha = 0.16f),
            ornamentColor = Color(0xFF7B571A),
            ghunnahColor = Color(0xFF1B7A34),
            qalqalahColor = Color(0xFF0D5C9E),
            maddColor = Color(0xFFB71C1C),
            ikhfaColor = Color(0xFFC65102)
        )
        QuranReadingTheme.LIGHT -> MushafPalette(
            pageBg = Color(0xFFF5F6F7),
            sheetBg = Color(0xFFFFFFFF),
            innerPageBg = Color(0xFFFFFFFF),
            chromeBg = scheme.primaryContainer,
            chromeInk = scheme.onPrimaryContainer,
            frameBorder = scheme.primary.copy(alpha = 0.65f),
            innerFrameBorder = scheme.outlineVariant,
            inkColor = Color(0xFF0D0F10),
            secondaryInk = Color(0xFF5F6368),
            goldAccent = scheme.tertiary,
            emeraldAccent = scheme.primary,
            highlightBg = scheme.primary.copy(alpha = 0.16f),
            ornamentColor = Color(0xFF8C661A),
            ghunnahColor = Color(0xFF1B7A34),
            qalqalahColor = Color(0xFF0D5C9E),
            maddColor = Color(0xFFB71C1C),
            ikhfaColor = Color(0xFFC65102)
        )
    }
}

private fun buildPaperInkColorFilter(paper: Color, ink: Color): ColorFilter {
    val pr = paper.red * 255f
    val pg = paper.green * 255f
    val pb = paper.blue * 255f
    val ir = ink.red * 255f
    val ig = ink.green * 255f
    val ib = ink.blue * 255f
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                (pr - ir) / 255f, 0f, 0f, 0f, ir,
                0f, (pg - ig) / 255f, 0f, 0f, ig,
                0f, 0f, (pb - ib) / 255f, 0f, ib,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )
}

internal fun quranFontFamilyFor(style: QuranFontStyle): FontFamily = when (style) {
    QuranFontStyle.UTHMANI_HAFS -> AmiriQuranFamily
    QuranFontStyle.NASKH_SIL -> ScheherazadeNewFamily
    QuranFontStyle.AMIRI_CLASSIC -> AmiriFamily
}

private data class SurahVerseRun(
    val surahNumber: Int,
    val verses: List<QuranVerseEntity>
)

private fun groupConsecutiveSurahs(verses: List<QuranVerseEntity>): List<SurahVerseRun> {
    if (verses.isEmpty()) return emptyList()
    val runs = mutableListOf<SurahVerseRun>()
    var currentSurah = verses.first().surahNumber
    var currentList = mutableListOf<QuranVerseEntity>()
    for (v in verses) {
        if (v.surahNumber != currentSurah) {
            runs += SurahVerseRun(currentSurah, currentList)
            currentSurah = v.surahNumber
            currentList = mutableListOf(v)
        } else {
            currentList += v
        }
    }
    if (currentList.isNotEmpty()) {
        runs += SurahVerseRun(currentSurah, currentList)
    }
    return runs
}

private data class LineSurahRun(
    val surahNumber: Int,
    val startsAtAyahOne: Boolean,
    val lines: List<MushafLine>
)

private fun groupLinesBySurahRun(lines: List<MushafLine>): List<LineSurahRun> {
    if (lines.isEmpty()) return emptyList()
    val result = mutableListOf<LineSurahRun>()
    var currentSurah = lines.first().words.firstOrNull()?.surahNumber ?: 1
    var startsAyahOne = (lines.first().words.firstOrNull()?.ayahNumber == 1)
    var buffer = mutableListOf<MushafLine>()

    for (line in lines) {
        val segmentsInLine = mutableListOf<Pair<Int, MutableList<com.example.data.quran.MushafLineWord>>>()
        for (word in line.words) {
            if (segmentsInLine.isEmpty() || segmentsInLine.last().first != word.surahNumber) {
                segmentsInLine += word.surahNumber to mutableListOf(word)
            } else {
                segmentsInLine.last().second.add(word)
            }
        }
        for ((segSurah, segWords) in segmentsInLine) {
            if (segSurah != currentSurah) {
                if (buffer.isNotEmpty()) {
                    result += LineSurahRun(currentSurah, startsAyahOne, buffer)
                }
                currentSurah = segSurah
                startsAyahOne = (segWords.firstOrNull()?.ayahNumber == 1)
                buffer = mutableListOf(MushafLine(line.lineNumber, segWords))
            } else {
                buffer.add(MushafLine(line.lineNumber, segWords))
            }
        }
    }
    if (buffer.isNotEmpty()) {
        result += LineSurahRun(currentSurah, startsAyahOne, buffer)
    }
    return result
}

private fun dominantWordColor(
    word: String,
    tajweedEnabled: Boolean,
    defaultInkColor: Color,
    palette: MushafPalette
): Color {
    if (!tajweedEnabled) return defaultInkColor
    val spans = QuranScriptFormatter.findTajweedSpans(word)
    val firstRule = spans.minByOrNull { it.rule.ordinal }?.rule ?: return defaultInkColor
    return when (firstRule) {
        TajweedRule.GHUNNAH -> palette.ghunnahColor
        TajweedRule.QALQALAH -> palette.qalqalahColor
        TajweedRule.MADD -> palette.maddColor
        TajweedRule.IKHFA -> palette.ikhfaColor
    }
}

internal fun AnnotatedString.Builder.appendFormattedVerseWithTajweed(
    formattedArabic: String,
    tajweedEnabled: Boolean,
    defaultInkColor: Color,
    palette: MushafPalette
) {
    if (!tajweedEnabled) {
        withStyle(SpanStyle(color = defaultInkColor)) {
            append(formattedArabic)
        }
        return
    }
    val tokens = formattedArabic.split(' ')
    tokens.forEachIndexed { idx, token ->
        val tokenColor = dominantWordColor(token, tajweedEnabled = true, defaultInkColor, palette)
        withStyle(SpanStyle(color = tokenColor)) {
            append(token)
        }
        if (idx != tokens.lastIndex) {
            append(" ")
        }
    }
}

@Composable
internal fun TajweedLegendBar(
    palette: MushafPalette,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp)
            .testTag("tajweed_legend_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            Triple(TajweedRule.GHUNNAH, palette.ghunnahColor, if (isArabic) "غنة" else "Ghunnah"),
            Triple(TajweedRule.QALQALAH, palette.qalqalahColor, if (isArabic) "قلقلة" else "Qalqalah"),
            Triple(TajweedRule.MADD, palette.maddColor, if (isArabic) "مد" else "Madd"),
            Triple(TajweedRule.IKHFA, palette.ikhfaColor, if (isArabic) "إخفاء/إدغام" else "Ikhfa/Idgham")
        )
        items.forEach { (_, color, label) ->
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                border = BorderStroke(0.8.dp, color.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, CircleShape)
                    )
                    Text(
                        text = label,
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }
        }
    }
}

/**
 * Illuminates the Surah nameplate in classic KSU / King Fahd Madinah Mushaf style
 * with emerald/gold double cartouche bands and side arabesque rosettes.
 */
@Composable
internal fun OrnamentalSurahBanner(
    surahMeta: SurahMeta,
    strings: AppStrings,
    palette: MushafPalette,
    compact: Boolean = false
) {
    val lineColor = palette.frameBorder.copy(alpha = 0.85f)
    val goldLine = palette.innerFrameBorder.copy(alpha = 0.9f)
    val bandFill = palette.goldAccent.copy(alpha = 0.12f)
    val revelationAr = if (surahMeta.isMakkan) "مَكِّيَّة" else "مَدَنِيَّة"
    val ayahCountAr = QuranScriptFormatter.toArabicIndic(surahMeta.ayahCount)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 4.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 56.dp else 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val strokeMain = Stroke(width = 1.6.dp.toPx())
                val strokeThin = Stroke(width = 0.9.dp.toPx())
                val corner = 8.dp.toPx()

                drawRoundRect(
                    color = bandFill,
                    topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                    size = Size(w - 6.dp.toPx(), h - 6.dp.toPx()),
                    cornerRadius = CornerRadius(corner, corner)
                )
                drawRoundRect(
                    color = lineColor,
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(w - 4.dp.toPx(), h - 4.dp.toPx()),
                    cornerRadius = CornerRadius(corner, corner),
                    style = strokeMain
                )
                drawRoundRect(
                    color = goldLine,
                    topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
                    size = Size(w - 12.dp.toPx(), h - 12.dp.toPx()),
                    cornerRadius = CornerRadius(corner - 2.dp.toPx(), corner - 2.dp.toPx()),
                    style = strokeThin
                )

                val diamondSize = 9.dp.toPx()
                listOf(18.dp.toPx(), w - 18.dp.toPx()).forEach { cx ->
                    val cy = h / 2f
                    val path = Path().apply {
                        moveTo(cx, cy - diamondSize)
                        lineTo(cx + diamondSize, cy)
                        lineTo(cx, cy + diamondSize)
                        lineTo(cx - diamondSize, cy)
                        close()
                    }
                    drawPath(path = path, color = goldLine, style = strokeMain)
                    drawCircle(color = lineColor, radius = 2.4.dp.toPx(), center = Offset(cx, cy))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = revelationAr,
                    fontFamily = NotoSansArabicFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.secondaryInk
                )
                Text(
                    text = "سُورَةُ ${surahMeta.nameAr}",
                    fontFamily = AmiriQuranFamily,
                    fontSize = if (compact) 23.sp else 26.sp,
                    fontWeight = FontWeight.Normal,
                    color = palette.inkColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$ayahCountAr آيات",
                    fontFamily = NotoSansArabicFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.secondaryInk
                )
            }
        }
    }
}

@Composable
internal fun StandaloneBismillahLine(
    fontSizeSp: Float,
    inkColor: Color,
    fontFamily: FontFamily = AmiriQuranFamily
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
            fontFamily = fontFamily,
            fontSize = (fontSizeSp + 1f).sp,
            fontWeight = FontWeight.Normal,
            color = inkColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}

/**
 * Renders one physical Madinah Mushaf line (1..15) in strict RTL order, automatically scaling slightly
 * only when needed to keep line breaks faithful to the King Fahd Madinah Mushaf (15 lines/page).
 */
@Composable
private fun MushafJustifiedLineRow(
    line: MushafLine,
    pageVerses: List<QuranVerseEntity>,
    fontSizeSp: Float,
    fontFamily: FontFamily,
    tajweedEnabled: Boolean,
    isCenteredPage: Boolean,
    selectedVerseKey: Pair<Int, Int>?,
    activeAudioGlobal: Int,
    isAudioActive: Boolean,
    palette: MushafPalette,
    onSelectVerse: (QuranVerseEntity) -> Unit,
    onSelectSurahAyah: (Int, Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val wordCount = line.words.size.coerceAtLeast(1)

        val (effectiveSizeSp, shouldCenterLine) = remember(line.words, fontSizeSp, maxWidthPx, fontFamily, isCenteredPage) {
            val minGapPx = with(density) { 4.dp.toPx() } * (wordCount - 1)
            val measuredTotalPx = line.words.sumOf { w ->
                textMeasurer.measure(
                    text = w.text,
                    style = TextStyle(fontFamily = fontFamily, fontSize = fontSizeSp.sp),
                    maxLines = 1
                ).size.width
            }.toFloat()

            val scaledSize = if (measuredTotalPx + minGapPx > maxWidthPx && measuredTotalPx > 0f) {
                val ratio = ((maxWidthPx - minGapPx).coerceAtLeast(maxWidthPx * 0.5f)) / measuredTotalPx
                (fontSizeSp * ratio * 0.96f).coerceIn(13f, fontSizeSp)
            } else {
                fontSizeSp
            }
            val sparseShortLine = wordCount <= 3 && measuredTotalPx < maxWidthPx * 0.55f
            scaledSize to (isCenteredPage || sparseShortLine)
        }

        val arrangement = if (shouldCenterLine) {
            Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        } else {
            Arrangement.SpaceBetween
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = arrangement,
                verticalAlignment = Alignment.CenterVertically
            ) {
                line.words.forEach { w ->
                    val matchedVerse = pageVerses.firstOrNull {
                        it.surahNumber == w.surahNumber && it.ayahNumber == w.ayahNumber
                    }
                    val isCurrent = (isAudioActive && matchedVerse?.globalNumber == activeAudioGlobal) ||
                        (selectedVerseKey?.first == w.surahNumber && selectedVerseKey.second == w.ayahNumber)

                    val wordColor = when {
                        isCurrent -> palette.emeraldAccent
                        w.isEndMarker -> palette.ornamentColor
                        else -> dominantWordColor(w.text, tajweedEnabled, palette.inkColor, palette)
                    }

                    Text(
                        text = w.text,
                        fontFamily = fontFamily,
                        fontSize = effectiveSizeSp.sp,
                        lineHeight = (effectiveSizeSp * 1.75f).sp,
                        color = wordColor,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrent) palette.highlightBg else Color.Transparent)
                            .clickable {
                                if (matchedVerse != null) {
                                    onSelectVerse(matchedVerse)
                                } else {
                                    onSelectSurahAyah(w.surahNumber, w.ayahNumber)
                                }
                            }
                    )
                }
            }
        }
    }
}

/**
 * Draws the ornamental King Saud University (Ayat) Madinah Mushaf double-line frame
 * with corner medallions on every page.
 */
@Composable
private fun KsuMadinahPageBorderBox(
    palette: MushafPalette,
    isCeremonialOpeningPage: Boolean,
    content: @Composable () -> Unit
) {
    val outerColor = palette.frameBorder.copy(alpha = if (isCeremonialOpeningPage) 0.9f else 0.7f)
    val innerColor = palette.innerFrameBorder.copy(alpha = if (isCeremonialOpeningPage) 0.95f else 0.78f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.innerPageBg, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val outerPad = 3.dp.toPx()
            val innerPad = 7.dp.toPx()
            val radiusOuter = 8.dp.toPx()
            val radiusInner = 5.dp.toPx()

            drawRoundRect(
                color = outerColor,
                topLeft = Offset(outerPad, outerPad),
                size = Size(w - outerPad * 2, h - outerPad * 2),
                cornerRadius = CornerRadius(radiusOuter, radiusOuter),
                style = Stroke(width = if (isCeremonialOpeningPage) 2.2.dp.toPx() else 1.5.dp.toPx())
            )
            drawRoundRect(
                color = innerColor,
                topLeft = Offset(innerPad, innerPad),
                size = Size(w - innerPad * 2, h - innerPad * 2),
                cornerRadius = CornerRadius(radiusInner, radiusInner),
                style = Stroke(width = 1.dp.toPx())
            )

            // Four KSU-style corner geometric medallions
            val r = 6.dp.toPx()
            val corners = listOf(
                Offset(innerPad + 4.dp.toPx(), innerPad + 4.dp.toPx()),
                Offset(w - innerPad - 4.dp.toPx(), innerPad + 4.dp.toPx()),
                Offset(innerPad + 4.dp.toPx(), h - innerPad - 4.dp.toPx()),
                Offset(w - innerPad - 4.dp.toPx(), h - innerPad - 4.dp.toPx())
            )
            corners.forEach { c ->
                val path = Path().apply {
                    moveTo(c.x, c.y - r)
                    lineTo(c.x + r, c.y)
                    lineTo(c.x, c.y + r)
                    lineTo(c.x - r, c.y)
                    close()
                }
                drawPath(path, color = innerColor, style = Stroke(width = 1.1.dp.toPx()))
                drawCircle(color = outerColor, radius = 2.dp.toPx(), center = c)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = if (isCeremonialOpeningPage) 16.dp else 12.dp)
        ) {
            content()
        }
    }
}

/**
 * Digital Madinah mushaf page (offline KFGQPC Hafs text, 15 lines per page like the printed
 * 604-page Madinah mushaf, rendered by the bundled MIT-licensed mushaf-text engine) styled as a calm,
 * full-bleed printed sheet: the text block keeps the actual KSU Madinah plate
 * proportions (622x917) centered on plain paper, surah banners are thin ink
 * ornaments rather than heavy accent boxes, and tapping an ayah selects it so
 * the reader can show its actions (tafsir / copy / share).
 */
@Composable
internal fun DigitalMadinahPage(
    pageNumber: Int,
    palette: MushafPalette,
    onAyahClick: (io.github.sherifshabans.mushaf.Ayah) -> Unit,
    modifier: Modifier = Modifier,
    selectedAyahId: Int? = null
) {
    val mushafColors = remember(palette) {
        io.github.sherifshabans.mushaf.MushafColors(
            paper = palette.innerPageBg,
            ink = palette.inkColor,
            marker = palette.secondaryInk,
            bannerFill = androidx.compose.ui.graphics.Color.Transparent,
            bannerBorder = palette.secondaryInk.copy(alpha = 0.35f),
            bannerText = palette.inkColor,
            selection = palette.highlightBg,
            playing = palette.emeraldAccent.copy(alpha = 0.25f)
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.innerPageBg)
            .testTag("mushaf_page_card_$pageNumber"),
        contentAlignment = Alignment.Center
    ) {
        io.github.sherifshabans.mushaf.MushafPage(
            page = pageNumber,
            colors = mushafColors,
            selectedAyahIds = setOfNotNull(selectedAyahId),
            onAyahClick = onAyahClick,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Full book-page scanned plate matching https://app.quranflash.com/book/Medina1?ar#/reader
 * with theme-matched ink/paper filter, double-tap zoom & pan, book spine shading, and offline retry/fallback.
 */
@Composable
internal fun QuranFlashMedinaBookPageSheet(
    pageNumber: Int,
    imageState: MedinaPageImageState?,
    pageVerses: List<QuranVerseEntity>,
    pageLineLayout: MushafPageLineLayout?,
    readingTheme: QuranReadingTheme,
    fontSizeSp: Float,
    fontStyle: QuranFontStyle,
    tajweedEnabled: Boolean,
    isPageMarkedRead: Boolean,
    selectedVerseKey: Pair<Int, Int>?,
    activeAudioGlobal: Int,
    isAudioActive: Boolean,
    strings: AppStrings,
    palette: MushafPalette,
    onRetryImage: (Int) -> Unit,
    onToggleFullscreen: () -> Unit,
    onSelectVerse: (QuranVerseEntity) -> Unit,
    onSelectSurahAyah: (Int, Int) -> Unit,
    onTogglePageRead: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic = strings.language == AppLanguage.ARABIC
    var zoomScale by remember(pageNumber) { mutableFloatStateOf(1f) }
    var panOffset by remember(pageNumber) { mutableStateOf(Offset.Zero) }

    val scheme = MaterialTheme.colorScheme
    val (bookPageSurfaceColor, bookInkColor) = when (readingTheme) {
        QuranReadingTheme.KSU_MUSHAF -> scheme.surface to scheme.onSurface
        QuranReadingTheme.DARK -> {
            if (scheme.surface.luminance() < 0.25f) {
                scheme.surface to scheme.onSurface
            } else {
                Color(0xFF181A1E) to Color(0xFFEDEFF2)
            }
        }
        QuranReadingTheme.SEPIA -> Color(0xFFF6EBD4) to Color(0xFF3A2B1A)
        QuranReadingTheme.LIGHT -> Color(0xFFFFFFFF) to Color(0xFF111315)
    }

    val pageColorFilter = remember(bookPageSurfaceColor, bookInkColor, readingTheme) {
        if (readingTheme == QuranReadingTheme.LIGHT) {
            null
        } else {
            buildPaperInkColorFilter(paper = bookPageSurfaceColor, ink = bookInkColor)
        }
    }

    val isEvenPage = pageNumber % 2 == 0
    val gutterBrush = if (isEvenPage) {
        Brush.horizontalGradient(
            listOf(Color.Black.copy(alpha = 0.08f), Color.Transparent),
            startX = 0f,
            endX = 36f
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.08f)),
            startX = 0f,
            endX = 36f
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("mushaf_page_card_$pageNumber"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bookPageSurfaceColor,
            shadowElevation = 6.dp,
            border = BorderStroke(0.8.dp, palette.innerFrameBorder.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(pageNumber) {
                        detectTapGestures(
                            onTap = { onToggleFullscreen() },
                            onDoubleTap = {
                                if (zoomScale > 1.05f) {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                } else {
                                    zoomScale = 1.75f
                                }
                            }
                        )
                    }
                    .then(
                        if (zoomScale > 1.05f) {
                            Modifier.pointerInput(pageNumber, zoomScale) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val maxX = size.width * 0.4f
                                    val maxY = size.height * 0.4f
                                    panOffset = Offset(
                                        x = (panOffset.x + dragAmount.x).coerceIn(-maxX, maxX),
                                        y = (panOffset.y + dragAmount.y).coerceIn(-maxY, maxY)
                                    )
                                }
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (imageState) {
                    is MedinaPageImageState.Ready -> {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageState.file)
                                .crossfade(true)
                                .build(),
                            contentDescription = if (isArabic) {
                                "صفحة ${QuranScriptFormatter.toArabicIndic(pageNumber)} من مصحف المدينة"
                            } else {
                                "Medina Mushaf Page $pageNumber"
                            },
                            contentScale = ContentScale.Fit,
                            colorFilter = pageColorFilter,
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = palette.emeraldAccent)
                                }
                            },
                            error = {
                                MedinaOfflinePageFallback(
                                    pageNumber = pageNumber,
                                    pageVerses = pageVerses,
                                    pageLineLayout = pageLineLayout,
                                    fontSizeSp = fontSizeSp,
                                    fontStyle = fontStyle,
                                    tajweedEnabled = tajweedEnabled,
                                    isPageMarkedRead = isPageMarkedRead,
                                    selectedVerseKey = selectedVerseKey,
                                    activeAudioGlobal = activeAudioGlobal,
                                    isAudioActive = isAudioActive,
                                    strings = strings,
                                    palette = palette,
                                    isArabic = isArabic,
                                    onRetryImage = { onRetryImage(pageNumber) },
                                    onSelectVerse = onSelectVerse,
                                    onSelectSurahAyah = onSelectSurahAyah,
                                    onTogglePageRead = onTogglePageRead
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    translationX = panOffset.x
                                    translationY = panOffset.y
                                }
                        )

                        // Book gutter shadow edge (matching physical Mushaf book spread)
                        Box(
                            modifier = Modifier
                                .align(if (isEvenPage) Alignment.CenterStart else Alignment.CenterEnd)
                                .size(width = 12.dp, height = 900.dp)
                                .background(gutterBrush)
                        )
                    }

                    is MedinaPageImageState.Loading, null -> {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = palette.emeraldAccent)
                            Text(
                                text = if (isArabic) {
                                    "جاري تحميل صفحة ${QuranScriptFormatter.toArabicIndic(pageNumber)} من مصحف المدينة..."
                                } else {
                                    "Loading Medina Mushaf Page $pageNumber..."
                                },
                                fontFamily = NotoSansArabicFamily,
                                fontSize = 14.sp,
                                color = palette.secondaryInk,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is MedinaPageImageState.Error -> {
                        MedinaOfflinePageFallback(
                            pageNumber = pageNumber,
                            pageVerses = pageVerses,
                            pageLineLayout = pageLineLayout,
                            fontSizeSp = fontSizeSp,
                            fontStyle = fontStyle,
                            tajweedEnabled = tajweedEnabled,
                            isPageMarkedRead = isPageMarkedRead,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioGlobal = activeAudioGlobal,
                            isAudioActive = isAudioActive,
                            strings = strings,
                            palette = palette,
                            isArabic = isArabic,
                            onRetryImage = { onRetryImage(pageNumber) },
                            onSelectVerse = onSelectVerse,
                            onSelectSurahAyah = onSelectSurahAyah,
                            onTogglePageRead = onTogglePageRead
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedinaOfflinePageFallback(
    pageNumber: Int,
    pageVerses: List<QuranVerseEntity>,
    pageLineLayout: MushafPageLineLayout?,
    fontSizeSp: Float,
    fontStyle: QuranFontStyle,
    tajweedEnabled: Boolean,
    isPageMarkedRead: Boolean,
    selectedVerseKey: Pair<Int, Int>?,
    activeAudioGlobal: Int,
    isAudioActive: Boolean,
    strings: AppStrings,
    palette: MushafPalette,
    isArabic: Boolean,
    onRetryImage: () -> Unit,
    onSelectVerse: (QuranVerseEntity) -> Unit,
    onSelectSurahAyah: (Int, Int) -> Unit,
    onTogglePageRead: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = onRetryImage,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = if (isArabic) "إعادة تحميل صورة صفحة المصحف" else "Retry Mushaf Page Image",
                fontFamily = NotoSansArabicFamily,
                fontSize = 12.sp
            )
        }
        if (pageVerses.isNotEmpty() || pageLineLayout != null) {
            MedinaMushafPageCard(
                pageNumber = pageNumber,
                pageVerses = pageVerses,
                pageLineLayout = pageLineLayout,
                fontSizeSp = fontSizeSp,
                fontStyle = fontStyle,
                isSimpleScript = false,
                tajweedEnabled = tajweedEnabled,
                ksuScanImageMode = false,
                isPageMarkedRead = isPageMarkedRead,
                selectedVerseKey = selectedVerseKey,
                activeAudioGlobal = activeAudioGlobal,
                isAudioActive = isAudioActive,
                strings = strings,
                palette = palette,
                onSelectVerse = onSelectVerse,
                onSelectSurahAyah = onSelectSurahAyah,
                onTogglePageRead = onTogglePageRead,
                includeTestTag = false
            )
        }
    }
}

@Composable
internal fun MedinaMushafPageCard(
    pageNumber: Int,
    pageVerses: List<QuranVerseEntity>,
    pageLineLayout: MushafPageLineLayout?,
    fontSizeSp: Float,
    fontStyle: QuranFontStyle,
    isSimpleScript: Boolean,
    tajweedEnabled: Boolean,
    ksuScanImageMode: Boolean = false,
    isPageMarkedRead: Boolean,
    selectedVerseKey: Pair<Int, Int>?,
    activeAudioGlobal: Int,
    isAudioActive: Boolean,
    strings: AppStrings,
    palette: MushafPalette,
    onSelectVerse: (QuranVerseEntity) -> Unit,
    onSelectSurahAyah: (Int, Int) -> Unit = { _, _ -> },
    onTogglePageRead: (Int) -> Unit = {},
    includeTestTag: Boolean = true
) {
    val isArabic = strings.language == AppLanguage.ARABIC
    val juzMeta = remember(pageNumber) { QuranMetadata.juzForPage(pageNumber) }
    val hizbNumber = remember(pageNumber) { QuranScriptFormatter.hizbForPage(pageNumber) }
    val primarySurah = remember(pageVerses, pageNumber) {
        val num = pageVerses.firstOrNull()?.surahNumber
            ?: QuranMetadata.surahsOnPage(pageNumber).firstOrNull()?.number
            ?: 1
        QuranMetadata.surahByNumber(num)
    }
    val fontFamily = remember(fontStyle) { quranFontFamilyFor(fontStyle) }
    val isCeremonialOpeningPage = pageNumber == 1 || pageNumber == 2
    val useApiLines = !isSimpleScript && pageLineLayout != null && pageLineLayout.lines.isNotEmpty()
    val runs = remember(pageVerses) { groupConsecutiveSurahs(pageVerses) }
    val lineRuns = remember(pageLineLayout) {
        pageLineLayout?.let { groupLinesBySurahRun(it.lines) }.orEmpty()
    }
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = palette.sheetBg,
        shadowElevation = 2.dp,
        border = BorderStroke(0.6.dp, palette.innerFrameBorder.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (includeTestTag) Modifier.testTag("mushaf_page_card_$pageNumber") else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. KSU Ayat Running Header Ribbon (Surah on Right • Juz/Hizb on Left)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = palette.emeraldAccent.copy(alpha = 0.10f),
                        border = BorderStroke(0.6.dp, palette.innerFrameBorder.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = "سُورَةُ ${primarySurah.nameAr}",
                            fontFamily = AmiriQuranFamily,
                            fontSize = 14.sp,
                            color = palette.emeraldAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "الجزء ${QuranScriptFormatter.toArabicIndic(juzMeta.number)} • الحزب ${QuranScriptFormatter.toArabicIndic(hizbNumber)}",
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.secondaryInk
                    )
                }
            }

            // 2. KSU Scan Image Mode OR Interactive Framed 15-Line Madinah Mushaf
            if (ksuScanImageMode) {
                var useBackupHost by remember(pageNumber) { mutableStateOf(false) }
                var imageFailedCompletely by remember(pageNumber) { mutableStateOf(false) }

                if (!imageFailedCompletely) {
                    val url = if (!useBackupHost) {
                        "https://quran.ksu.edu.sa/ayat/safahat2/$pageNumber.png"
                    } else {
                        "https://quran.ksu.edu.sa/ayat/safahat1/$pageNumber.png"
                    }
                    KsuMadinahPageBorderBox(palette = palette, isCeremonialOpeningPage = isCeremonialOpeningPage) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(true)
                                .build(),
                            contentDescription = if (isArabic) {
                                "صفحة ${QuranScriptFormatter.toArabicIndic(pageNumber)}"
                            } else {
                                "Mushaf Page $pageNumber"
                            },
                            contentScale = ContentScale.FillWidth,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.66f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = palette.emeraldAccent)
                                }
                            },
                            error = {
                                if (!useBackupHost) {
                                    useBackupHost = true
                                } else {
                                    imageFailedCompletely = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                } else {
                    KsuInteractiveMushafBody(
                        useApiLines = useApiLines,
                        lineRuns = lineRuns,
                        runs = runs,
                        pageVerses = pageVerses,
                        fontSizeSp = fontSizeSp,
                        fontFamily = fontFamily,
                        fontStyle = fontStyle,
                        isSimpleScript = isSimpleScript,
                        tajweedEnabled = tajweedEnabled,
                        isCeremonialOpeningPage = isCeremonialOpeningPage,
                        selectedVerseKey = selectedVerseKey,
                        activeAudioGlobal = activeAudioGlobal,
                        isAudioActive = isAudioActive,
                        strings = strings,
                        palette = palette,
                        onSelectVerse = onSelectVerse,
                        onSelectSurahAyah = onSelectSurahAyah
                    )
                }
            } else {
                KsuInteractiveMushafBody(
                    useApiLines = useApiLines,
                    lineRuns = lineRuns,
                    runs = runs,
                    pageVerses = pageVerses,
                    fontSizeSp = fontSizeSp,
                    fontFamily = fontFamily,
                    fontStyle = fontStyle,
                    isSimpleScript = isSimpleScript,
                    tajweedEnabled = tajweedEnabled,
                    isCeremonialOpeningPage = isCeremonialOpeningPage,
                    selectedVerseKey = selectedVerseKey,
                    activeAudioGlobal = activeAudioGlobal,
                    isAudioActive = isAudioActive,
                    strings = strings,
                    palette = palette,
                    onSelectVerse = onSelectVerse,
                    onSelectSurahAyah = onSelectSurahAyah
                )
            }

            // 3. KSU Medallion Page Footer (Arabic-Indic Page Number Medallion + Khatmah Check Pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isPageMarkedRead) {
                        palette.emeraldAccent.copy(alpha = 0.14f)
                    } else {
                        Color.Transparent
                    },
                    border = BorderStroke(
                        width = 0.9.dp,
                        color = if (isPageMarkedRead) palette.emeraldAccent else palette.innerFrameBorder.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .clickable { onTogglePageRead(pageNumber) }
                        .testTag("mushaf_mark_page_read_$pageNumber")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPageMarkedRead) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isPageMarkedRead) palette.emeraldAccent else palette.secondaryInk,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isPageMarkedRead) {
                                if (isArabic) "مقروءة" else "Read"
                            } else {
                                if (isArabic) "ختمة" else "Mark"
                            },
                            fontFamily = NotoSansArabicFamily,
                            fontSize = 11.sp,
                            color = if (isPageMarkedRead) palette.emeraldAccent else palette.secondaryInk
                        )
                    }
                }

                // Centered classic KSU Page Number Medallion
                Surface(
                    shape = CircleShape,
                    color = palette.goldAccent.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, palette.innerFrameBorder)
                ) {
                    Text(
                        text = QuranScriptFormatter.toArabicIndic(pageNumber),
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.inkColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = if (isArabic) {
                        "ص ${QuranScriptFormatter.toArabicIndic(pageNumber)}"
                    } else {
                        "p.$pageNumber"
                    },
                    fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
                    fontSize = 11.sp,
                    color = palette.secondaryInk
                )
            }
        }
    }
}

@Composable
private fun KsuInteractiveMushafBody(
    useApiLines: Boolean,
    lineRuns: List<LineSurahRun>,
    runs: List<SurahVerseRun>,
    pageVerses: List<QuranVerseEntity>,
    fontSizeSp: Float,
    fontFamily: FontFamily,
    fontStyle: QuranFontStyle,
    isSimpleScript: Boolean,
    tajweedEnabled: Boolean,
    isCeremonialOpeningPage: Boolean,
    selectedVerseKey: Pair<Int, Int>?,
    activeAudioGlobal: Int,
    isAudioActive: Boolean,
    strings: AppStrings,
    palette: MushafPalette,
    onSelectVerse: (QuranVerseEntity) -> Unit,
    onSelectSurahAyah: (Int, Int) -> Unit
) {
    KsuMadinahPageBorderBox(
        palette = palette,
        isCeremonialOpeningPage = isCeremonialOpeningPage
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (useApiLines) {
                lineRuns.forEach { run ->
                    val runSurahMeta = QuranMetadata.surahByNumber(run.surahNumber)
                    if (run.startsAtAyahOne) {
                        OrnamentalSurahBanner(
                            surahMeta = runSurahMeta,
                            strings = strings,
                            palette = palette,
                            compact = true
                        )
                        if (run.surahNumber != 1 && run.surahNumber != 9) {
                            StandaloneBismillahLine(
                                fontSizeSp = fontSizeSp,
                                inkColor = palette.inkColor,
                                fontFamily = fontFamily
                            )
                        }
                    }

                    run.lines.forEach { mushafLine ->
                        MushafJustifiedLineRow(
                            line = mushafLine,
                            pageVerses = pageVerses,
                            fontSizeSp = fontSizeSp,
                            fontFamily = fontFamily,
                            tajweedEnabled = tajweedEnabled,
                            isCenteredPage = isCeremonialOpeningPage,
                            selectedVerseKey = selectedVerseKey,
                            activeAudioGlobal = activeAudioGlobal,
                            isAudioActive = isAudioActive,
                            palette = palette,
                            onSelectVerse = onSelectVerse,
                            onSelectSurahAyah = onSelectSurahAyah
                        )
                    }
                }
            } else {
                runs.forEach { run ->
                    val runSurahMeta = QuranMetadata.surahByNumber(run.surahNumber)
                    val startsSurahHere = run.verses.firstOrNull()?.ayahNumber == 1

                    if (startsSurahHere) {
                        OrnamentalSurahBanner(
                            surahMeta = runSurahMeta,
                            strings = strings,
                            palette = palette,
                            compact = true
                        )
                        if (run.surahNumber != 1 && run.surahNumber != 9) {
                            StandaloneBismillahLine(
                                fontSizeSp = fontSizeSp,
                                inkColor = palette.inkColor,
                                fontFamily = fontFamily
                            )
                        }
                    }

                    val annotatedMushaf = buildAnnotatedString {
                        run.verses.forEachIndexed { idx, verse ->
                            val isCurrent = (isAudioActive && verse.globalNumber == activeAudioGlobal) ||
                                (selectedVerseKey?.first == verse.surahNumber &&
                                    selectedVerseKey.second == verse.ayahNumber)
                            val stripped = QuranScriptFormatter.stripLeadingBismillah(
                                surahNumber = verse.surahNumber,
                                ayahNumber = verse.ayahNumber,
                                text = verse.arabicText
                            )
                            val formattedArabic = QuranScriptFormatter.formatTanzilScript(
                                uthmaniText = stripped,
                                simple = isSimpleScript
                            )
                            val hasSajdah = QuranScriptFormatter.hasSajdahMark(stripped)
                            val arabicNum = QuranScriptFormatter.toArabicIndic(verse.ayahNumber)
                            val verseMarker = if (fontStyle == QuranFontStyle.AMIRI_CLASSIC) {
                                if (hasSajdah) " ۩ ﴿$arabicNum﴾" else " ﴿$arabicNum﴾"
                            } else {
                                if (hasSajdah) " ۩ \u06DD$arabicNum" else " \u06DD$arabicNum"
                            }

                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = "ayah_${verse.surahNumber}_${verse.ayahNumber}",
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            background = if (isCurrent) palette.highlightBg else Color.Transparent
                                        )
                                    ),
                                    linkInteractionListener = { onSelectVerse(verse) }
                                )
                            ) {
                                appendFormattedVerseWithTajweed(
                                    formattedArabic = formattedArabic,
                                    tajweedEnabled = tajweedEnabled,
                                    defaultInkColor = if (isCurrent) palette.emeraldAccent else palette.inkColor,
                                    palette = palette
                                )
                                withStyle(
                                    SpanStyle(
                                        color = if (isCurrent) palette.emeraldAccent else palette.ornamentColor
                                    )
                                ) {
                                    append(verseMarker)
                                }
                            }
                            if (idx != run.verses.lastIndex) {
                                append(" ")
                            }
                        }
                    }

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = annotatedMushaf,
                            fontFamily = fontFamily,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 2.0f).sp,
                            textAlign = if (isCeremonialOpeningPage) TextAlign.Center else TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SurahOrPagePickerDialog(
    currentSurahNumber: Int?,
    currentPageNumber: Int,
    strings: AppStrings,
    onSelectSurah: (Int) -> Unit,
    onSelectPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isArabic = strings.language == AppLanguage.ARABIC
    var tabIndex by remember { mutableIntStateOf(if (currentSurahNumber == null) 1 else 0) }
    var pageInput by remember { mutableStateOf(currentPageNumber.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (tabIndex == 0) strings.surahsTab else strings.mushafPagesTab,
                fontFamily = if (isArabic) NotoSansArabicFamily else MaterialTheme.typography.headlineSmall.fontFamily
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryTabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text(strings.surahsTab, maxLines = 1) }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = {
                            Text(
                                text = if (isArabic) "${strings.mushafPagesTab} (٦٠٤)" else "${strings.mushafPagesTab} (604)",
                                maxLines = 1
                            )
                        }
                    )
                }

                when (tabIndex) {
                    0 -> {
                        LazyColumn(modifier = Modifier.height(340.dp)) {
                            items(QuranMetadata.surahs, key = { it.number }) { s ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDismiss()
                                            onSelectSurah(s.number)
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isArabic) {
                                            "${QuranScriptFormatter.toArabicIndic(s.number)}. سورة ${s.nameAr}"
                                        } else {
                                            "${s.number}. ${s.nameEn}"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = if (isArabic) NotoSansArabicFamily else MaterialTheme.typography.bodyLarge.fontFamily,
                                        fontWeight = if (s.number == currentSurahNumber) FontWeight.Bold else FontWeight.Normal,
                                        color = if (s.number == currentSurahNumber) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = if (isArabic) {
                                            "${strings.pageLabel} ${QuranScriptFormatter.toArabicIndic(s.startPage)}"
                                        } else {
                                            "${s.nameAr} • ${strings.pageLabel} ${s.startPage}"
                                        },
                                        fontFamily = NotoSansArabicFamily,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = pageInput,
                                onValueChange = { pageInput = it.filter { ch -> ch.isDigit() }.take(3) },
                                label = {
                                    Text(
                                        if (isArabic) "${strings.pageLabel} (١–٦٠٤)" else "${strings.pageLabel} (1–604)"
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val parsed = pageInput.toIntOrNull()?.coerceIn(1, 604) ?: 1
                                    onDismiss()
                                    onSelectPage(parsed)
                                }
                            ) {
                                Text(if (isArabic) "انتقال" else "Go")
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(modifier = Modifier.height(260.dp)) {
                            items(QuranMetadata.juzList, key = { it.number }) { juz ->
                                val startSurah = QuranMetadata.surahByNumber(juz.startSurah)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDismiss()
                                            onSelectPage(juz.startPage)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isArabic) {
                                            "الجزء ${QuranScriptFormatter.toArabicIndic(juz.number)} • سورة ${startSurah.nameAr}"
                                        } else {
                                            "${strings.juzTab} ${juz.number} • ${startSurah.nameEn}"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = if (isArabic) NotoSansArabicFamily else MaterialTheme.typography.bodyMedium.fontFamily
                                    )
                                    Text(
                                        text = if (isArabic) {
                                            "${strings.pageLabel} ${QuranScriptFormatter.toArabicIndic(juz.startPage)}"
                                        } else {
                                            "${strings.pageLabel} ${juz.startPage}"
                                        },
                                        fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إغلاق" else "Close")
            }
        }
    )
}
