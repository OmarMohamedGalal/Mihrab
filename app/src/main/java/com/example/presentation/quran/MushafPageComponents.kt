package com.example.presentation.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
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
import com.example.core.i18n.AppStrings
import com.example.data.local.QuranVerseEntity
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranScriptFormatter
import com.example.data.quran.SurahMeta
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily

internal data class MushafPalette(
    val pageBg: Color,
    val sheetBg: Color,
    val frameBorder: Color,
    val inkColor: Color,
    val secondaryInk: Color,
    val goldAccent: Color,
    val highlightBg: Color
)

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

@Composable
internal fun OrnamentalSurahBanner(
    surahMeta: SurahMeta,
    strings: AppStrings,
    palette: MushafPalette,
    compact: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(if (compact) 16.dp else 22.dp),
        color = palette.highlightBg.copy(alpha = if (compact) 0.55f else 0.35f),
        border = BorderStroke(1.2.dp, palette.frameBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = if (compact) 10.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "سُورَةُ ${surahMeta.nameAr}",
                fontFamily = AmiriFamily,
                fontSize = if (compact) 23.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                color = palette.goldAccent,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${surahMeta.nameEn} • ${surahMeta.meaningEn}",
                style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall,
                color = palette.inkColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${if (surahMeta.isMakkan) strings.makkan else strings.madinan} • ${surahMeta.ayahCount} ${strings.versesLabel} • ${strings.pageLabel} ${surahMeta.startPage}",
                style = MaterialTheme.typography.labelSmall,
                color = palette.secondaryInk
            )
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
    selectedVerseKey: Pair<Int, Int>?,
    activeAudioGlobal: Int,
    isAudioActive: Boolean,
    strings: AppStrings,
    palette: MushafPalette,
    onSelectVerse: (QuranVerseEntity) -> Unit
) {
    val juzMeta = remember(pageNumber) { QuranMetadata.juzForPage(pageNumber) }
    val primarySurah = remember(pageVerses) {
        QuranMetadata.surahByNumber(pageVerses.firstOrNull()?.surahNumber ?: 1)
    }
    val runs = remember(pageVerses) { groupConsecutiveSurahs(pageVerses) }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = palette.sheetBg,
        border = BorderStroke(1.2.dp, palette.frameBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mushaf_page_card_$pageNumber")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Authentic Medina Mushaf top page margin header: Juz on one side, Surah name on the other
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الجزء ${QuranScriptFormatter.toArabicIndic(juzMeta.number)}",
                    fontFamily = NotoSansArabicFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.goldAccent
                )
                Text(
                    text = "سورة ${primarySurah.nameAr}",
                    fontFamily = NotoSansArabicFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.goldAccent
                )
            }

            HorizontalDivider(color = palette.frameBorder.copy(alpha = 0.55f))

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
                        val verseMarker = " ﴿${QuranScriptFormatter.toArabicIndic(verse.ayahNumber)}﴾"

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
                            withStyle(
                                SpanStyle(
                                    color = if (isCurrent) palette.goldAccent else palette.inkColor
                                )
                            ) {
                                append(formattedArabic)
                            }
                            withStyle(
                                SpanStyle(
                                    color = palette.goldAccent,
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
                        lineHeight = (fontSizeSp * 1.95f).sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(color = palette.frameBorder.copy(alpha = 0.55f))

            // Medina Mushaf bottom page number medallion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = palette.highlightBg.copy(alpha = 0.65f),
                    border = BorderStroke(0.8.dp, palette.frameBorder)
                ) {
                    Text(
                        text = "${QuranScriptFormatter.toArabicIndic(pageNumber)} • $pageNumber",
                        fontFamily = RobotoMonoFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.goldAccent,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp)
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
