package com.example.presentation.quran

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.local.QuranVerseEntity
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranScriptFormatter
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TanzilSurahReaderView(
    surahNumber: Int?,
    mushafPage: Int?,
    uiState: QuranUiState,
    viewModel: QuranViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val prefs = uiState.prefs
    val nightMode = prefs.quranNightMode
    val isMushafMode = prefs.quranMushafMode
    val isSimpleScript = prefs.quranSimpleScript
    val tajweedEnabled = uiState.tajweedEnabled
    val isPageMode = mushafPage != null

    val palette = remember(nightMode) { rememberMushafPalette(nightMode) }

    val verses = uiState.currentSurahVerses
    val activePage = mushafPage
        ?: verses.firstOrNull()?.pageNumber
        ?: QuranMetadata.surahByNumber(surahNumber ?: 1).startPage
    val primarySurahNumber = surahNumber
        ?: verses.firstOrNull()?.surahNumber
        ?: QuranMetadata.surahsOnPage(activePage).firstOrNull()?.number
        ?: 1
    val surahMeta = QuranMetadata.surahByNumber(primarySurahNumber)

    var showReciterMenu by remember { mutableStateOf(false) }
    var showSurahPicker by remember { mutableStateOf(false) }
    var selectedVerseKey by remember(surahNumber, mushafPage) {
        mutableStateOf(
            if (surahNumber != null) {
                val initAyah = if (prefs.lastReadSurah == surahNumber && prefs.lastReadAyah >= 1) prefs.lastReadAyah else 1
                surahNumber to initAyah
            } else {
                verses.firstOrNull()?.let { it.surahNumber to it.ayahNumber }
            }
        )
    }

    LaunchedEffect(verses, mushafPage) {
        if (selectedVerseKey == null && verses.isNotEmpty()) {
            selectedVerseKey = verses.first().let { it.surahNumber to it.ayahNumber }
        }
    }

    LaunchedEffect(uiState.audioState.globalAyahNumber, uiState.audioState.surahNumber) {
        if (uiState.audioState.surahNumber > 0 && uiState.audioState.ayahNumber > 0) {
            val match = verses.firstOrNull { it.globalNumber == uiState.audioState.globalAyahNumber }
            if (match != null) {
                selectedVerseKey = match.surahNumber to match.ayahNumber
            }
        }
    }

    val selectedIndex = verses.indexOfFirst {
        selectedVerseKey != null && it.surahNumber == selectedVerseKey!!.first && it.ayahNumber == selectedVerseKey!!.second
    }.let { if (it >= 0) it else 0 }
    val selectedVerse = verses.getOrNull(selectedIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.pageBg)
    ) {
        // 1. Top Navigator Bar
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = palette.sheetBg,
                titleContentColor = palette.inkColor,
                navigationIconContentColor = palette.inkColor,
                actionIconContentColor = palette.inkColor
            ),
            title = {
                Surface(
                    shape = CircleShape,
                    color = palette.highlightBg.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, palette.frameBorder),
                    modifier = Modifier
                        .clickable { showSurahPicker = true }
                        .testTag("tanzil_surah_picker")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPageMode) "${strings.pageLabel} $activePage" else "${surahMeta.number}. ${surahMeta.nameEn}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = surahMeta.nameAr,
                            fontFamily = NotoSansArabicFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.goldAccent
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select Surah or Page",
                            tint = palette.goldAccent
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("surah_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.toggleTajweed() },
                    modifier = Modifier.testTag("tanzil_tajweed_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ColorLens,
                        contentDescription = "Tajweed Colors",
                        tint = if (tajweedEnabled) palette.emeraldAccent else palette.secondaryInk
                    )
                }
                IconButton(onClick = { viewModel.toggleTranslation() }) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = strings.showTranslation,
                        tint = if (prefs.showTranslation) palette.goldAccent else palette.secondaryInk
                    )
                }
                IconButton(onClick = { viewModel.toggleNightMode() }) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = strings.nightReadingMode,
                        tint = if (nightMode) palette.goldAccent else palette.secondaryInk
                    )
                }
            }
        )

        // 2. Mihrab Mushaf & Tajweed Controls Strip
        Surface(
            color = palette.sheetBg,
            border = BorderStroke(0.5.dp, palette.frameBorder.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isMushafMode,
                        onClick = { if (!isMushafMode) viewModel.toggleMushafMode() },
                        label = { Text(strings.mushafViewLabel) },
                        modifier = Modifier.testTag("tanzil_mode_mushaf")
                    )
                    FilterChip(
                        selected = !isMushafMode,
                        onClick = { if (isMushafMode) viewModel.toggleMushafMode() },
                        label = { Text(strings.verseByVerseLabel) },
                        modifier = Modifier.testTag("tanzil_mode_verses")
                    )
                    FilterChip(
                        selected = tajweedEnabled,
                        onClick = { viewModel.toggleTajweed() },
                        label = { Text(if (isArabic) "تجويد ملون" else "Tajwīd") },
                        modifier = Modifier.testTag("tanzil_tajweed_toggle")
                    )
                    FilterChip(
                        selected = !isSimpleScript,
                        onClick = { if (isSimpleScript) viewModel.toggleSimpleScript() },
                        label = { Text(strings.uthmaniScriptLabel) },
                        modifier = Modifier.testTag("tanzil_script_uthmani")
                    )
                    FilterChip(
                        selected = isSimpleScript,
                        onClick = { if (!isSimpleScript) viewModel.toggleSimpleScript() },
                        label = { Text(strings.simpleScriptLabel) },
                        modifier = Modifier.testTag("tanzil_script_simple")
                    )
                    if (!isPageMode) {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.openMushafPage(surahMeta.startPage) },
                            label = { Text("${strings.pageLabel} ${surahMeta.startPage}") },
                            modifier = Modifier.testTag("open_mushaf_page_chip")
                        )
                    }

                    Box {
                        val activeReciter = QuranMetadata.reciters.firstOrNull { it.id == prefs.selectedReciterId }
                            ?: QuranMetadata.reciters.first()
                        FilledTonalButton(
                            onClick = { showReciterMenu = true },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activeReciter.nameEn.substringBefore(" "),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        DropdownMenu(
                            expanded = showReciterMenu,
                            onDismissRequest = { showReciterMenu = false }
                        ) {
                            QuranMetadata.reciters.forEach { rec ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${rec.nameEn} (${rec.nameAr})",
                                            fontFamily = NotoSansArabicFamily
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectReciter(rec.id)
                                        showReciterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (tajweedEnabled) {
                    TajweedLegendBar(
                        palette = palette,
                        isArabic = isArabic
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.fontSizeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryInk
                    )
                    Slider(
                        value = prefs.quranFontSizeSp,
                        onValueChange = { viewModel.setFontSize(it) },
                        valueRange = 22f..42f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "${prefs.quranFontSizeSp.toInt()}sp",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = RobotoMonoFamily,
                        color = palette.goldAccent
                    )
                }
            }
        }

        // 3. Body
        if (uiState.isLoadingSurah && verses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = palette.goldAccent)
            }
        } else if (uiState.surahLoadError != null && verses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.surahLoadError,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (mushafPage != null) viewModel.openMushafPage(mushafPage)
                            else if (surahNumber != null) viewModel.openSurah(surahNumber)
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        } else {
            val bottomContentPad = if (uiState.audioState.isPlaying || uiState.audioState.isBuffering) 110.dp else 28.dp
            val pagesMap = remember(verses) { verses.groupBy { it.pageNumber }.toSortedMap() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tanzil_reader_list"),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = bottomContentPad),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isMushafMode) {
                    // Authentic Mihrab Madinah Mushaf 604-page cards (multi-surah pages, double frames, Tajweed coloring, Khatmah toggle)
                    items(pagesMap.entries.toList(), key = { it.key }) { (pageNum, pageVerses) ->
                        MedinaMushafPageCard(
                            pageNumber = pageNum,
                            pageVerses = pageVerses,
                            fontSizeSp = prefs.quranFontSizeSp,
                            isSimpleScript = isSimpleScript,
                            tajweedEnabled = tajweedEnabled,
                            isPageMarkedRead = uiState.khatmahState.readPages.contains(pageNum),
                            selectedVerseKey = selectedVerseKey,
                            activeAudioGlobal = uiState.audioState.globalAyahNumber,
                            isAudioActive = uiState.audioState.isPlaying || uiState.audioState.isBuffering,
                            strings = strings,
                            palette = palette,
                            onSelectVerse = { v -> selectedVerseKey = v.surahNumber to v.ayahNumber },
                            onTogglePageRead = { p -> viewModel.togglePageRead(p) }
                        )
                    }

                    if (selectedVerse != null) {
                        item(key = "selected_verse_inspector") {
                            SelectedAyahInspectorCard(
                                verse = selectedVerse,
                                canGoPrev = selectedIndex > 0,
                                canGoNext = selectedIndex < verses.lastIndex,
                                isBookmarked = uiState.bookmarks.any {
                                    it.surahNumber == selectedVerse.surahNumber &&
                                        it.ayahNumber == selectedVerse.ayahNumber
                                },
                                isPlayingThis = uiState.audioState.globalAyahNumber == selectedVerse.globalNumber &&
                                    (uiState.audioState.isPlaying || uiState.audioState.isBuffering),
                                showTranslation = prefs.showTranslation,
                                tapHint = strings.tapVerseHint,
                                palette = palette,
                                onPrevAyah = {
                                    verses.getOrNull(selectedIndex - 1)?.let {
                                        selectedVerseKey = it.surahNumber to it.ayahNumber
                                    }
                                },
                                onNextAyah = {
                                    verses.getOrNull(selectedIndex + 1)?.let {
                                        selectedVerseKey = it.surahNumber to it.ayahNumber
                                    }
                                },
                                onPlay = { viewModel.playVerse(selectedVerse) },
                                onBookmark = { viewModel.toggleBookmark(selectedVerse) }
                            )
                        }
                    }
                } else {
                    // Verse-by-Verse Mode with Surah Banner + Standalone Bismillah + Tajweed Coloring
                    items(verses, key = { it.globalNumber }) { verse ->
                        if (verse.ayahNumber == 1) {
                            val vSurahMeta = QuranMetadata.surahByNumber(verse.surahNumber)
                            OrnamentalSurahBanner(
                                surahMeta = vSurahMeta,
                                strings = strings,
                                palette = palette
                            )
                            if (verse.surahNumber != 1 && verse.surahNumber != 9) {
                                Spacer(modifier = Modifier.height(6.dp))
                                StandaloneBismillahLine(
                                    fontSizeSp = prefs.quranFontSizeSp,
                                    goldAccent = palette.goldAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        val isBookmarked = uiState.bookmarks.any {
                            it.surahNumber == verse.surahNumber && it.ayahNumber == verse.ayahNumber
                        }
                        val isPlayingThis = uiState.audioState.globalAyahNumber == verse.globalNumber &&
                            (uiState.audioState.isPlaying || uiState.audioState.isBuffering)
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

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isPlayingThis) palette.highlightBg else palette.sheetBg,
                            border = BorderStroke(
                                width = if (isPlayingThis) 1.5.dp else 0.8.dp,
                                color = if (isPlayingThis) palette.goldAccent else palette.frameBorder.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(shape = CircleShape, color = palette.highlightBg) {
                                            Text(
                                                text = "${verse.surahNumber}:${verse.ayahNumber}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontFamily = RobotoMonoFamily,
                                                color = palette.goldAccent,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        if (hasSajdah) {
                                            Surface(
                                                shape = CircleShape,
                                                color = palette.goldAccent.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "۩ Sajdah",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.goldAccent,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { viewModel.playVerse(verse) },
                                            modifier = Modifier.testTag("play_ayah_${verse.ayahNumber}")
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingThis) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                contentDescription = "Play verse",
                                                tint = palette.goldAccent
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleBookmark(verse) },
                                            modifier = Modifier.testTag("bookmark_ayah_${verse.ayahNumber}")
                                        ) {
                                            Icon(
                                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                contentDescription = "Bookmark verse",
                                                tint = if (isBookmarked) palette.goldAccent else palette.secondaryInk
                                            )
                                        }
                                    }
                                }

                                val annotatedAyah = buildAnnotatedString {
                                    appendFormattedVerseWithTajweed(
                                        formattedArabic = formattedArabic,
                                        tajweedEnabled = tajweedEnabled,
                                        defaultInkColor = palette.inkColor,
                                        palette = palette
                                    )
                                    withStyle(
                                        SpanStyle(
                                            color = palette.goldAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(" ﴿${QuranScriptFormatter.toArabicIndic(verse.ayahNumber)}﴾")
                                    }
                                }

                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    Text(
                                        text = annotatedAyah,
                                        fontFamily = AmiriFamily,
                                        fontSize = prefs.quranFontSizeSp.sp,
                                        lineHeight = (prefs.quranFontSizeSp * 1.85f).sp,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (prefs.showTranslation && verse.englishTranslation.isNotBlank()) {
                                    HorizontalDivider(color = palette.frameBorder.copy(alpha = 0.45f))
                                    Text(
                                        text = verse.englishTranslation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.secondaryInk
                                    )
                                }
                            }
                        }
                    }
                }

                // Medina Mushaf Page Turner & Khatmah Advance Footer when browsing by Page
                if (mushafPage != null) {
                    item(key = "mushaf_page_turner") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = { viewModel.openMushafPage(mushafPage - 1) },
                                    enabled = mushafPage > 1,
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("prev_mushaf_page_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.prevPage)
                                }

                                Text(
                                    text = "$mushafPage / ${QuranMetadata.TOTAL_MUSHAF_PAGES}",
                                    fontFamily = RobotoMonoFamily,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.goldAccent
                                )

                                FilledTonalButton(
                                    onClick = { viewModel.openMushafPage(mushafPage + 1) },
                                    enabled = mushafPage < QuranMetadata.TOTAL_MUSHAF_PAGES,
                                    shape = CircleShape,
                                    modifier = Modifier.testTag("next_mushaf_page_button")
                                ) {
                                    Text(strings.nextPage)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                                }
                            }

                            // Complete Page in Khatmah & Advance Button
                            FilledTonalButton(
                                onClick = { viewModel.markPageReadAndAdvance(mushafPage) },
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("khatmah_complete_and_next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) {
                                        "إتمام الصفحة $mushafPage في الختمة والانتقال للتالية"
                                    } else {
                                        "Complete Page $mushafPage in Khatmah & Next Page"
                                    },
                                    fontFamily = NotoSansArabicFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSurahPicker) {
        SurahOrPagePickerDialog(
            currentSurahNumber = surahNumber,
            currentPageNumber = activePage,
            strings = strings,
            onSelectSurah = { viewModel.openSurah(it) },
            onSelectPage = { viewModel.openMushafPage(it) },
            onDismiss = { showSurahPicker = false }
        )
    }
}

@Composable
private fun SelectedAyahInspectorCard(
    verse: QuranVerseEntity,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    isBookmarked: Boolean,
    isPlayingThis: Boolean,
    showTranslation: Boolean,
    tapHint: String,
    palette: MushafPalette,
    onPrevAyah: () -> Unit,
    onNextAyah: () -> Unit,
    onPlay: () -> Unit,
    onBookmark: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = palette.highlightBg.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, palette.frameBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevAyah, enabled = canGoPrev) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Ayah",
                            tint = palette.inkColor
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = palette.goldAccent.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "${verse.surahNumber}:${verse.ayahNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = RobotoMonoFamily,
                            color = palette.goldAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = onNextAyah, enabled = canGoNext) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Ayah",
                            tint = palette.inkColor
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.testTag("play_ayah_${verse.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isPlayingThis) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play verse",
                            tint = palette.goldAccent
                        )
                    }
                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier.testTag("bookmark_ayah_${verse.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Bookmark verse",
                            tint = if (isBookmarked) palette.goldAccent else palette.secondaryInk
                        )
                    }
                }
            }

            if (showTranslation && verse.englishTranslation.isNotBlank()) {
                Text(
                    text = verse.englishTranslation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.inkColor
                )
            } else {
                Text(
                    text = tapHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.secondaryInk
                )
            }
        }
    }
}
