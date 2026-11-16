package com.example.presentation.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.AppStrings
import com.example.data.local.QuranVerseEntity
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranScriptFormatter
import com.example.data.quran.SurahMeta
import com.example.data.quran.TajweedRule
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily

internal data class MushafPalette(
    val pageBg: Color,
    val sheetBg: Color,
    val innerPageBg: Color,
    val frameBorder: Color,
    val innerFrameBorder: Color,
    val inkColor: Color,
    val secondaryInk: Color,
    val goldAccent: Color,
    val emeraldAccent: Color,
    val highlightBg: Color,
    val ghunnahColor: Color,
    val qalqalahColor: Color,
    val maddColor: Color,
    val ikhfaColor: Color
)

internal fun rememberMushafPalette(nightMode: Boolean): MushafPalette = if (nightMode) {
    MushafPalette(
        pageBg = Color(0xFF100E0A),
        sheetBg = Color(0xFF18140F),
        innerPageBg = Color(0xFF1D1812),
        frameBorder = Color(0xFF6B5636),
        innerFrameBorder = Color(0xFF4A3B24),
        inkColor = Color(0xFFF5ECD7),
        secondaryInk = Color(0xFFC2B59B),
        goldAccent = Color(0xFFE6C883),
        emeraldAccent = Color(0xFF56C596),
        highlightBg = Color(0xFF332917),
        ghunnahColor = Color(0xFF66BB6A),
        qalqalahColor = Color(0xFF42A5F5),
        maddColor = Color(0xFFEF5350),
        ikhfaColor = Color(0xFFFFA726)
    )
} else {
    MushafPalette(
        pageBg = Color(0xFFF6EFE0),
        sheetBg = Color(0xFFFDF9EE),
        innerPageBg = Color(0xFFFFFDF7),
        frameBorder = Color(0xFFC9AE74),
        innerFrameBorder = Color(0xFFE0CCA0),
        inkColor = Color(0xFF1B170F),
        secondaryInk = Color(0xFF5D523F),
        goldAccent = Color(0xFF836016),
        emeraldAccent = Color(0xFF146C43),
        highlightBg = Color(0xFFF2E4BE),
        ghunnahColor = Color(0xFF1B7A34),
        qalqalahColor = Color(0xFF0D5C9E),
        maddColor = Color(0xFFB71C1C),
        ikhfaColor = Color(0xFFC65102)
    )
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
    val spans = QuranScriptFormatter.findTajweedSpans(formattedArabic)
    if (spans.isEmpty()) {
        withStyle(SpanStyle(color = defaultInkColor)) {
            append(formattedArabic)
        }
        return
    }
    var cursor = 0
    for (span in spans) {
        if (span.start > cursor) {
            withStyle(SpanStyle(color = defaultInkColor)) {
                append(formattedArabic.substring(cursor, span.start))
            }
        }
        val ruleColor = when (span.rule) {
            TajweedRule.GHUNNAH -> palette.ghunnahColor
            TajweedRule.QALQALAH -> palette.qalqalahColor
            TajweedRule.MADD -> palette.maddColor
            TajweedRule.IKHFA -> palette.ikhfaColor
        }
        withStyle(SpanStyle(color = ruleColor)) {
            append(formattedArabic.substring(span.start, span.endExclusive))
        }
        cursor = span.endExclusive
    }
    if (cursor < formattedArabic.length) {
        withStyle(SpanStyle(color = defaultInkColor)) {
            append(formattedArabic.substring(cursor))
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
                color = color.copy(alpha = 0.14f),
                border = BorderStroke(0.8.dp, color.copy(alpha = 0.55f))
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

@Composable
internal fun OrnamentalSurahBanner(
    surahMeta: SurahMeta,
    strings: AppStrings,
    palette: MushafPalette,
    compact: Boolean = false
) {
    val manzil = remember(surahMeta.number) { QuranScriptFormatter.manzilForSurah(surahMeta.number) }
    val revelationAr = if (surahMeta.isMakkan) "مَكِّيَّة" else "مَدَنِيَّة"
    val ayahCountAr = QuranScriptFormatter.toArabicIndic(surahMeta.ayahCount)

    Surface(
        shape = RoundedCornerShape(if (compact) 14.dp else 18.dp),
        color = palette.highlightBg.copy(alpha = if (compact) 0.65f else 0.5f),
        border = BorderStroke(1.4.dp, palette.frameBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
                .border(0.8.dp, palette.goldAccent.copy(alpha = 0.55f), RoundedCornerShape(if (compact) 11.dp else 15.dp))
                .padding(horizontal = 12.dp, vertical = if (compact) 8.dp else 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Cartouche Medallion (Verse count)
                Surface(
                    shape = CircleShape,
                    color = palette.sheetBg.copy(alpha = 0.85f),
                    border = BorderStroke(0.8.dp, palette.frameBorder)
                ) {
                    Text(
                        text = "آياتها $ayahCountAr",
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.goldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Center Surah Title Cartouche
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "۞ سُورَةُ ${surahMeta.nameAr} ۞",
                        fontFamily = AmiriFamily,
                        fontSize = if (compact) 22.sp else 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldAccent,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${surahMeta.nameEn} • ${surahMeta.meaningEn} • Manzil $manzil",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryInk,
                        textAlign = TextAlign.Center
                    )
                }

                // Right Cartouche Medallion (Makki / Madani)
                Surface(
                    shape = CircleShape,
                    color = palette.sheetBg.copy(alpha = 0.85f),
                    border = BorderStroke(0.8.dp, palette.frameBorder)
                ) {
                    Text(
                        text = revelationAr,
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.goldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun StandaloneBismillahLine(
    fontSizeSp: Float,
    goldAccent: Color
) {
    Text(
        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
        fontFamily = AmiriFamily,
        fontSize = (fontSizeSp + 1f).sp,
        fontWeight = FontWeight.Bold,
        color = goldAccent,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
internal fun MedinaMushafPageCard(
    pageNumber: Int,
    pageVerses: List<QuranVerseEntity>,
    fontSizeSp: Float,
    isSimpleScript: Boolean,
    tajweedEnabled: Boolean,
    isPageMarkedRead: Boolean,
    selectedVerseKey: Pair<Int, Int>?,
    activeAudioGlobal: Int,
    isAudioActive: Boolean,
    strings: AppStrings,
    palette: MushafPalette,
    onSelectVerse: (QuranVerseEntity) -> Unit,
    onTogglePageRead: (Int) -> Unit = {}
) {
    val isArabic = strings.language == AppLanguage.ARABIC
    val juzMeta = remember(pageNumber) { QuranMetadata.juzForPage(pageNumber) }
    val hizbNumber = remember(pageNumber) { QuranScriptFormatter.hizbForPage(pageNumber) }
    val rubNumber = remember(pageNumber) { QuranScriptFormatter.rubInHizbForPage(pageNumber) }
    val primarySurah = remember(pageVerses, pageNumber) {
        val num = pageVerses.firstOrNull()?.surahNumber
            ?: QuranMetadata.surahsOnPage(pageNumber).firstOrNull()?.number
            ?: 1
        QuranMetadata.surahByNumber(num)
    }
    val runs = remember(pageVerses) { groupConsecutiveSurahs(pageVerses) }
    val isCeremonialOpeningPage = pageNumber == 1 || pageNumber == 2
    val isRightHandOddPage = pageNumber % 2 == 1

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = palette.sheetBg,
        border = BorderStroke(1.8.dp, palette.frameBorder),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mushaf_page_card_$pageNumber")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Realistic Mushaf book spine shadow on the binding side (left for odd right-hand page, right for even left-hand page)
                    val spineWidth = 18.dp.toPx()
                    if (isRightHandOddPage) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(palette.frameBorder.copy(alpha = 0.22f), Color.Transparent),
                                startX = 0f,
                                endX = spineWidth
                            ),
                            topLeft = Offset.Zero,
                            size = Size(spineWidth, size.height)
                        )
                    } else {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, palette.frameBorder.copy(alpha = 0.22f)),
                                startX = size.width - spineWidth,
                                endX = size.width
                            ),
                            topLeft = Offset(size.width - spineWidth, 0f),
                            size = Size(spineWidth, size.height)
                        )
                    }
                }
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.innerPageBg, RoundedCornerShape(14.dp))
                    .border(1.dp, palette.innerFrameBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = if (isCeremonialOpeningPage) 18.dp else 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Authentic Medina Mushaf Top Running Margin Header (Juz • Hizb/Rub' • Surah)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الجزء ${QuranScriptFormatter.toArabicIndic(juzMeta.number)}",
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldAccent
                    )
                    Text(
                        text = "۞ الحزب ${QuranScriptFormatter.toArabicIndic(hizbNumber)} • الربع ${QuranScriptFormatter.toArabicIndic(rubNumber)} ۞",
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.secondaryInk
                    )
                    Text(
                        text = "سُورَةُ ${primarySurah.nameAr}",
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldAccent
                    )
                }

                HorizontalDivider(color = palette.frameBorder.copy(alpha = 0.65f))

                // 2. Inner Illuminated Mushaf Frame for Quranic Lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isCeremonialOpeningPage) {
                                Modifier
                                    .border(1.5.dp, palette.goldAccent.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                        goldAccent = palette.goldAccent
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
                                    val verseMarker = if (hasSajdah) {
                                        " ۩ ﴿${QuranScriptFormatter.toArabicIndic(verse.ayahNumber)}﴾"
                                    } else {
                                        " ﴿${QuranScriptFormatter.toArabicIndic(verse.ayahNumber)}﴾"
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
                                            defaultInkColor = if (isCurrent) palette.goldAccent else palette.inkColor,
                                            palette = palette
                                        )
                                        withStyle(
                                            SpanStyle(
                                                color = if (isCurrent) palette.emeraldAccent else palette.goldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append(verseMarker)
                                        }
                                    }
                                    if (idx != run.verses.lastIndex) {
                                        append("  ")
                                    }
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = annotatedMushaf,
                                    fontFamily = AmiriFamily,
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.98f).sp,
                                    textAlign = if (isCeremonialOpeningPage) TextAlign.Center else TextAlign.Justify,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = palette.frameBorder.copy(alpha = 0.65f))

                // 3. Medina Mushaf Bottom Footer: Khatmah Read Toggle + Centered Ornamental Page Number Medallion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Khatmah Mark Page Read Pill (matching MihrabHQ/Mihrab khatmahProgress)
                    Surface(
                        shape = CircleShape,
                        color = if (isPageMarkedRead) {
                            palette.emeraldAccent.copy(alpha = 0.16f)
                        } else {
                            palette.highlightBg.copy(alpha = 0.5f)
                        },
                        border = BorderStroke(
                            width = 0.9.dp,
                            color = if (isPageMarkedRead) palette.emeraldAccent else palette.frameBorder
                        ),
                        modifier = Modifier
                            .clickable { onTogglePageRead(pageNumber) }
                            .testTag("mushaf_mark_page_read_$pageNumber")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isPageMarkedRead) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isPageMarkedRead) palette.emeraldAccent else palette.secondaryInk,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isPageMarkedRead) {
                                    if (isArabic) "تمت قراءتها" else "Page Read"
                                } else {
                                    if (isArabic) "إتمام بالختمة" else "Mark Read"
                                },
                                fontFamily = NotoSansArabicFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPageMarkedRead) palette.emeraldAccent else palette.secondaryInk
                            )
                        }
                    }

                    // Centered Ornamental Page Number Medallion
                    Surface(
                        shape = CircleShape,
                        color = palette.highlightBg.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, palette.frameBorder)
                    ) {
                        Text(
                            text = "﴿ ${QuranScriptFormatter.toArabicIndic(pageNumber)} • $pageNumber ﴾",
                            fontFamily = RobotoMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.goldAccent,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }

                    // Page Side Indicator (Right/Odd vs Left/Even Mushaf leaf)
                    Text(
                        text = if (isRightHandOddPage) {
                            if (isArabic) "صفحة يمنى" else "Right Leaf"
                        } else {
                            if (isArabic) "صفحة يسرى" else "Left Leaf"
                        },
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 11.sp,
                        color = palette.secondaryInk
                    )
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
    var tabIndex by remember { mutableIntStateOf(if (currentSurahNumber == null) 1 else 0) }
    var pageInput by remember { mutableStateOf(currentPageNumber.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tabIndex == 0) strings.surahsTab else strings.mushafPagesTab) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryTabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text(strings.surahsTab) }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("${strings.mushafPagesTab} (604)") }
                    )
                }

                if (tabIndex == 0) {
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
                                    text = "${s.number}. ${s.nameEn}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (s.number == currentSurahNumber) FontWeight.Bold else FontWeight.Normal,
                                    color = if (s.number == currentSurahNumber) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${s.nameAr} • ${strings.pageLabel} ${s.startPage}",
                                    fontFamily = NotoSansArabicFamily,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pageInput,
                            onValueChange = { pageInput = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("${strings.pageLabel} (1–604)") },
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
                            Text("Go")
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
                                    text = "${strings.juzTab} ${juz.number} • ${startSurah.nameEn}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${strings.pageLabel} ${juz.startPage}",
                                    fontFamily = RobotoMonoFamily,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
