package com.example.presentation.quran

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranRecitersCatalog
import com.example.data.quran.QuranScriptFormatter
import com.example.ui.theme.NotoSansArabicFamily
import kotlinx.coroutines.launch

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
    val readingTheme = uiState.readingTheme
    val fontStyle = uiState.fontStyle
    val scope = rememberCoroutineScope()

    val palette = rememberMushafPalette(readingTheme)
    val verses = uiState.currentSurahVerses
    val initialBookPage = (
        mushafPage
            ?: verses.firstOrNull()?.pageNumber
            ?: QuranMetadata.surahByNumber(surahNumber ?: 1).startPage
        ).coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)

    val pagerState = rememberPagerState(
        initialPage = initialBookPage - 1,
        pageCount = { QuranMetadata.TOTAL_MUSHAF_PAGES }
    )

    // Sync HorizontalPager target when caller opens a Surah or Mushaf Page
    LaunchedEffect(mushafPage, surahNumber) {
        val target = (
            mushafPage
                ?: QuranMetadata.surahByNumber(surahNumber ?: 1).startPage
            ).coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES) - 1
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    val currentBookPage = (pagerState.currentPage + 1).coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)

    val displayedSurahMeta = remember(currentBookPage, surahNumber) {
        QuranMetadata.surahsOnPage(currentBookPage).firstOrNull()
            ?: QuranMetadata.surahByNumber(surahNumber ?: 1)
    }
    val displayedJuz = remember(currentBookPage) { QuranMetadata.juzForPage(currentBookPage) }
    val displayedHizb = remember(currentBookPage) { QuranScriptFormatter.hizbForPage(currentBookPage) }

    val activeReciter = remember(prefs.selectedReciterId) {
        QuranRecitersCatalog.reciterById(prefs.selectedReciterId)
    }

    var showReciterMenu by remember { mutableStateOf(false) }
    var showSurahPicker by remember { mutableStateOf(false) }
    var showReaderSettings by remember { mutableStateOf(false) }
    // Reading-first: the mushaf page fills the screen by default; only quiet page metadata
    // shows (surah, juz, page number). Tap the metadata to bring the toolbars back.
    var isFullscreenMode by remember { mutableStateOf(true) }

    // Ayah selection in the digital mushaf reader: tapping an ayah highlights it and
    // offers copy / share / tafsir. Clears when the page turns. The tapped ayah comes
    // from the bundled KFGQPC text, so copy & share work fully offline.
    var selectedAyah by remember { mutableStateOf<io.github.sherifshabans.mushaf.Ayah?>(null) }
    var tafsirAyahKey by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Load QuranFlash Medina1 page image & persist last-read Mushaf page whenever current pager page changes
    LaunchedEffect(pagerState.currentPage) {
        selectedAyah = null
        val page = pagerState.currentPage + 1
        viewModel.onMushafPageViewed(page)
    }

    val navigateToBookPage: (Int) -> Unit = { targetPage ->
        val clamped = targetPage.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        viewModel.ensurePageLinesLoaded(clamped)
        scope.launch {
            pagerState.animateScrollToPage(clamped - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.innerPageBg)
    ) {
        // 1. Top bar: hidden in reading mode. Plain swap, no expand animation, so the
        // page relayouts once per toggle instead of every animation frame.
        if (!isFullscreenMode) {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = palette.chromeBg,
                        titleContentColor = palette.chromeInk,
                        navigationIconContentColor = palette.chromeInk,
                        actionIconContentColor = palette.chromeInk
                    ),
                    title = {
                        Surface(
                            shape = CircleShape,
                            color = palette.chromeInk.copy(alpha = 0.14f),
                            border = BorderStroke(0.8.dp, palette.innerFrameBorder.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .clickable { showSurahPicker = true }
                                .testTag("tanzil_surah_picker")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) {
                                        "سورة ${displayedSurahMeta.nameAr}"
                                    } else {
                                        displayedSurahMeta.nameEn
                                    },
                                    fontFamily = NotoSansArabicFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.chromeInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) {
                                        "• ص ${QuranScriptFormatter.toArabicIndic(currentBookPage)} • ج${QuranScriptFormatter.toArabicIndic(displayedJuz.number)}"
                                    } else {
                                        "• p.$currentBookPage • J${displayedJuz.number}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = NotoSansArabicFamily,
                                    color = palette.chromeInk.copy(alpha = 0.88f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = if (isArabic) "اختيار السورة أو الصفحة" else "Select Surah or Page",
                                    tint = palette.chromeInk
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("surah_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isArabic) "رجوع" else "Back"
                            )
                        }
                    },
                    actions = {
                        val isPageBookmarked = uiState.bookmarks.any {
                            it.surahNumber == 0 && it.ayahNumber == currentBookPage
                        }
                        IconButton(
                            onClick = { viewModel.togglePageBookmark(currentBookPage) },
                            modifier = Modifier.testTag("reader_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isPageBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isArabic) "حفظ علامة الصفحة" else "Bookmark page",
                                tint = if (isPageBookmarked) palette.emeraldAccent else palette.chromeInk.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(
                            onClick = { isFullscreenMode = true },
                            modifier = Modifier.testTag("quran_fullscreen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Fullscreen,
                                contentDescription = if (isArabic) "ملء الشاشة" else "Fullscreen",
                                tint = palette.chromeInk.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(onClick = { viewModel.toggleNightMode() }) {
                            Icon(
                                imageVector = Icons.Filled.DarkMode,
                                contentDescription = strings.nightReadingMode,
                                tint = if (readingTheme == QuranReadingTheme.DARK) palette.emeraldAccent else palette.chromeInk.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(
                            onClick = { showReaderSettings = true },
                            modifier = Modifier.testTag("reader_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = if (isArabic) "إعدادات القراءة" else "Reading Settings",
                                tint = palette.chromeInk.copy(alpha = 0.9f)
                            )
                        }
                    }
                )

            }
        }

        // 3. Authentic 604-Page RTL Medina1 Mushaf Pager (صفحات المصحف فقط)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (isFullscreenMode) 30.dp else 0.dp,
                        bottom = if (isFullscreenMode) 24.dp else 0.dp
                    )
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HorizontalPager(
                        state = pagerState,
                        key = { it + 1 },
                        beyondViewportPageCount = 1,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("medina_book_pager")
                    ) { pageIdx ->
                        val pageNum = pageIdx + 1
                        if (uiState.ksuScanImageMode) {
                            LaunchedEffect(pageNum) {
                                viewModel.ensurePageLinesLoaded(pageNum)
                            }
                            val fallbackVerses = remember(verses, pageNum) {
                                verses.filter { it.pageNumber == pageNum }
                            }
                            QuranFlashMedinaBookPageSheet(
                                pageNumber = pageNum,
                                imageState = uiState.medinaPageImages[pageNum],
                                pageVerses = fallbackVerses,
                                pageLineLayout = uiState.pageLineLayouts[pageNum],
                                readingTheme = readingTheme,
                                fontSizeSp = prefs.quranFontSizeSp,
                                fontStyle = fontStyle,
                                tajweedEnabled = false,
                                isPageMarkedRead = uiState.khatmahState.readPages.contains(pageNum),
                                selectedVerseKey = null,
                                activeAudioGlobal = uiState.audioState.globalAyahNumber,
                                isAudioActive = uiState.audioState.isPlaying || uiState.audioState.isBuffering,
                                strings = strings,
                                palette = palette,
                                onRetryImage = { viewModel.ensureMedinaPageImageLoaded(it, forceRefresh = true) },
                                onToggleFullscreen = { isFullscreenMode = !isFullscreenMode },
                                onSelectVerse = {},
                                onSelectSurahAyah = { _, _ -> },
                                onTogglePageRead = { viewModel.togglePageRead(it) }
                            )
                        } else {
                            // Digital Madinah text mode: offline KFGQPC Hafs text laid out like the
                            // printed mushaf, scaled to the screen. No image or layout fetch needed.
                            // Tapping an ayah selects it (tap again to unselect); toolbars come back
                            // via the top metadata strip or the top-bar button, not page taps.
                            DigitalMadinahPage(
                                pageNumber = pageNum,
                                palette = palette,
                                selectedAyahId = selectedAyah?.takeIf { it.page == pageNum }?.id,
                                onAyahClick = { ayah ->
                                    selectedAyah = if (selectedAyah?.id == ayah.id) null else ayah
                                }
                            )
                        }
                    }
                }

                // Bottom controls (tools visible): reciter pill over the page scrubber, then khatmah row.
                if (!isFullscreenMode) {
                    Surface(
                        color = palette.sheetBg,
                        border = BorderStroke(0.6.dp, palette.innerFrameBorder.copy(alpha = 0.5f)),
                        tonalElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val isCurrentSurahAudio = uiState.audioState.surahNumber == displayedSurahMeta.number &&
                                uiState.audioState.surahNumber > 0 &&
                                (uiState.audioState.isPlaying || uiState.audioState.isBuffering || uiState.audioState.isPaused)
                            val isPlayingSurah = isCurrentSurahAudio && uiState.audioState.isPlaying
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box {
                                    Surface(
                                        shape = CircleShape,
                                        color = palette.chromeBg,
                                        shadowElevation = 2.dp
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable { showReciterMenu = true }
                                                    .testTag("ksu_qaree_selector_chip")
                                                    .padding(start = 14.dp, top = 7.dp, bottom = 7.dp, end = 2.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.RecordVoiceOver,
                                                    contentDescription = null,
                                                    tint = palette.chromeInk,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isArabic) activeReciter.nameAr else activeReciter.nameEn,
                                                    fontFamily = NotoSansArabicFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = palette.chromeInk
                                                )
                                                Icon(
                                                    Icons.Filled.ArrowDropDown,
                                                    contentDescription = if (isArabic) "اختيار القارئ" else "Choose reciter",
                                                    tint = palette.chromeInk,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (isCurrentSurahAudio) {
                                                        viewModel.toggleAudioPlayPause()
                                                    } else {
                                                        viewModel.playSurahAudio(displayedSurahMeta.number)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .testTag("medina_play_surah_chip")
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlayingSurah) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                    contentDescription = if (isArabic) "تشغيل أو إيقاف التلاوة" else "Play or pause recitation",
                                                    tint = palette.emeraldAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showReciterMenu,
                                        onDismissRequest = { showReciterMenu = false }
                                    ) {
                                        QuranRecitersCatalog.reciters.forEach { rec ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = if (isArabic) rec.nameAr else "${rec.nameAr} • ${rec.nameEn}",
                                                        fontFamily = NotoSansArabicFamily,
                                                        fontWeight = if (rec.id == activeReciter.id) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (rec.id == activeReciter.id) palette.emeraldAccent else MaterialTheme.colorScheme.onSurface
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

                            var sliderPagePos by remember(currentBookPage) {
                                mutableFloatStateOf(currentBookPage.toFloat())
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { navigateToBookPage(currentBookPage - 1) },
                                    enabled = currentBookPage > 1,
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("prev_mushaf_page_button")
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = strings.prevPage,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isArabic) "السابقة" else "Prev",
                                        fontSize = 11.sp
                                    )
                                }

                                Slider(
                                    value = sliderPagePos,
                                    onValueChange = { sliderPagePos = it },
                                    onValueChangeFinished = {
                                        navigateToBookPage(sliderPagePos.toInt().coerceIn(1, 604))
                                    },
                                    valueRange = 1f..604f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = palette.emeraldAccent,
                                        activeTrackColor = palette.emeraldAccent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                FilledTonalButton(
                                    onClick = { navigateToBookPage(currentBookPage + 1) },
                                    enabled = currentBookPage < QuranMetadata.TOTAL_MUSHAF_PAGES,
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("next_mushaf_page_button")
                                ) {
                                    Text(
                                        text = if (isArabic) "التالية" else "Next",
                                        fontSize = 11.sp
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = strings.nextPage,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isCurrentMarked = uiState.khatmahState.readPages.contains(currentBookPage)
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCurrentMarked) palette.emeraldAccent.copy(alpha = 0.14f) else Color.Transparent,
                                    border = BorderStroke(
                                        0.9.dp,
                                        if (isCurrentMarked) palette.emeraldAccent else palette.innerFrameBorder.copy(alpha = 0.65f)
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.togglePageRead(currentBookPage) }
                                        .testTag("mushaf_mark_page_read_$currentBookPage")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrentMarked) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isCurrentMarked) palette.emeraldAccent else palette.secondaryInk,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isCurrentMarked) {
                                                if (isArabic) "مقروءة" else "Read"
                                            } else {
                                                if (isArabic) "تسجيل ختمة" else "Mark"
                                            },
                                            fontFamily = NotoSansArabicFamily,
                                            fontSize = 11.sp,
                                            color = if (isCurrentMarked) palette.emeraldAccent else palette.secondaryInk
                                        )
                                    }
                                }

                                Text(
                                    text = if (isArabic) {
                                        "صفحة ${QuranScriptFormatter.toArabicIndic(currentBookPage)} من ٦٠٤ • الحزب ${QuranScriptFormatter.toArabicIndic(displayedHizb)}"
                                    } else {
                                        "Page $currentBookPage / 604 • Hizb $displayedHizb"
                                    },
                                    fontFamily = NotoSansArabicFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.inkColor
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = palette.chromeBg,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.togglePageRead(currentBookPage)
                                            if (!isCurrentMarked && currentBookPage < 604) {
                                                navigateToBookPage(currentBookPage + 1)
                                            }
                                        }
                                        .testTag("khatmah_complete_and_next_button")
                                ) {
                                    Text(
                                        text = if (isArabic) "إتمام والتالية" else "Done & Next",
                                        fontFamily = NotoSansArabicFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.chromeInk,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ayah selection sheet (digital mode): reference + tafsir / copy / share for the tapped
            // ayah, presented as a bottom sheet like Omar's reference instead of a floating pill.
            val tappedAyah = selectedAyah
            if (tappedAyah != null && !uiState.ksuScanImageMode) {
                val tappedSurahName = QuranMetadata.surahByNumber(tappedAyah.surah).nameAr
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.22f))
                        .clickable { selectedAyah = null }
                        .testTag("ayah_sheet_scrim")
                )
                AyahActionSheet(
                    reference = if (isArabic) {
                        "سورة $tappedSurahName • ${tappedAyah.surah}:${tappedAyah.number}"
                    } else {
                        "${QuranMetadata.surahByNumber(tappedAyah.surah).nameEn} • ${tappedAyah.surah}:${tappedAyah.number}"
                    },
                    palette = palette,
                    isArabic = isArabic,
                    showToolsOption = isFullscreenMode,
                    onCopy = { clipboardManager.setText(AnnotatedString(tappedAyah.text)) },
                    onShare = {
                        val shareText = tappedAyah.text +
                            "\n\n" +
                            (if (isArabic) "سورة " else "Surah ") +
                            tappedSurahName +
                            " ${tappedAyah.surah}:${tappedAyah.number}"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    },
                    onTafsir = {
                        tafsirAyahKey = tappedAyah.surah to tappedAyah.number
                        viewModel.loadTafsirForAyah(tappedAyah.surah, tappedAyah.number)
                    },
                    onShowTools = { isFullscreenMode = false },
                    onDismiss = { selectedAyah = null },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Tools-hidden state: only quiet page metadata over the page, no buttons.
            if (isFullscreenMode && selectedAyah == null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .clickable { isFullscreenMode = false }
                        .testTag("reader_metadata_overlay")
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) displayedSurahMeta.nameAr else displayedSurahMeta.nameEn,
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 12.sp,
                        color = palette.secondaryInk
                    )
                    Text(
                        text = if (isArabic) {
                            "\u0627\u0644\u062c\u0632\u0621 ${QuranScriptFormatter.toArabicIndic(displayedJuz.number)}"
                        } else {
                            "Part ${displayedJuz.number}"
                        },
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 12.sp,
                        color = palette.secondaryInk
                    )
                }
                Text(
                    text = QuranScriptFormatter.toArabicIndic(currentBookPage),
                    fontFamily = NotoSansArabicFamily,
                    fontSize = 11.sp,
                    color = palette.secondaryInk,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable { isFullscreenMode = false }
                        .testTag("reader_page_number_overlay")
                        .padding(bottom = 8.dp)
                )
            }

        }
    }

    // Tafsir dialog for the ayah tapped in the digital mushaf reader
    val openTafsirKey = tafsirAyahKey
    if (openTafsirKey != null) {
        val tafsirState = uiState.tafsirState
        val tafsirSurahName = QuranMetadata.surahByNumber(openTafsirKey.first).nameAr
        Dialog(
            onDismissRequest = {
                tafsirAyahKey = null
                viewModel.clearTafsir()
            }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.sheetBg,
                border = BorderStroke(0.8.dp, palette.innerFrameBorder.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("ayah_tafsir_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = (if (isArabic) "تفسير " else "Tafsir ") +
                            "$tafsirSurahName ${openTafsirKey.first}:${openTafsirKey.second}",
                        fontFamily = NotoSansArabicFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = palette.inkColor
                    )
                    Text(
                        text = if (isArabic) tafsirState.edition.labelAr else tafsirState.edition.labelEn,
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 11.sp,
                        color = palette.secondaryInk
                    )
                    when {
                        tafsirState.isLoading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = palette.emeraldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isArabic) "جارٍ تحميل التفسير..." else "Loading tafsir...",
                                    fontFamily = NotoSansArabicFamily,
                                    fontSize = 12.sp,
                                    color = palette.secondaryInk
                                )
                            }
                        }
                        tafsirState.error != null -> {
                            Text(
                                text = if (isArabic) {
                                    "تعذر تحميل التفسير الآن. يحتاج اتصالًا بالإنترنت في المرة الأولى ثم يُحفظ على الجهاز."
                                } else {
                                    "Tafsir could not be loaded now. It needs internet the first time, then it is saved on-device."
                                },
                                fontFamily = NotoSansArabicFamily,
                                fontSize = 12.sp,
                                color = palette.inkColor
                            )
                        }
                        else -> {
                            Text(
                                text = tafsirState.text,
                                fontFamily = NotoSansArabicFamily,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = palette.inkColor,
                                modifier = Modifier
                                    .heightIn(max = 320.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }
        }
    }

    // Reading settings sheet: page mode, paper theme and audio repeat live here instead of a
    // permanent second strip under the toolbar, keeping the visible chrome to one toolbar row.
    if (showReaderSettings) {
        Dialog(onDismissRequest = { showReaderSettings = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.sheetBg,
                border = BorderStroke(0.8.dp, palette.innerFrameBorder.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("reader_settings_sheet")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isArabic) "إعدادات القراءة" else "Reading Settings",
                        fontFamily = NotoSansArabicFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = palette.inkColor
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tap toggles digital Madinah text (default) vs scanned mushaf plates.
                        Surface(
                            shape = CircleShape,
                            color = palette.chromeBg,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .testTag("tanzil_mode_mushaf")
                                .clickable { viewModel.toggleKsuScanImageMode() }
                        ) {
                            Text(
                                text = if (isArabic) {
                                    val modeLabel = if (uiState.ksuScanImageMode) "صور المصحف" else "نص رقمي"
                                    "صفحات المصحف • $modeLabel"
                                } else {
                                    val modeLabel = if (uiState.ksuScanImageMode) "Scans" else "Digital text"
                                    "Mushaf Pages • $modeLabel"
                                },
                                fontFamily = NotoSansArabicFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.chromeInk,
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 12.dp)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ThemeSwatchDot(
                                fill = MaterialTheme.colorScheme.primaryContainer,
                                selected = readingTheme == QuranReadingTheme.KSU_MUSHAF,
                                accent = MaterialTheme.colorScheme.primary,
                                tag = "theme_ksu_dot",
                                onClick = { viewModel.setReadingTheme(QuranReadingTheme.KSU_MUSHAF) }
                            )
                            ThemeSwatchDot(
                                fill = Color(0xFFFFFFFF),
                                selected = readingTheme == QuranReadingTheme.LIGHT,
                                accent = MaterialTheme.colorScheme.primary,
                                tag = "theme_light_dot",
                                onClick = { viewModel.setReadingTheme(QuranReadingTheme.LIGHT) }
                            )
                            ThemeSwatchDot(
                                fill = Color(0xFFF6EBD4),
                                selected = readingTheme == QuranReadingTheme.SEPIA,
                                accent = MaterialTheme.colorScheme.primary,
                                tag = "theme_sepia_dot",
                                onClick = { viewModel.setReadingTheme(QuranReadingTheme.SEPIA) }
                            )
                            ThemeSwatchDot(
                                fill = Color(0xFF181A1E),
                                selected = readingTheme == QuranReadingTheme.DARK,
                                accent = MaterialTheme.colorScheme.primary,
                                tag = "theme_dark_dot",
                                onClick = { viewModel.setReadingTheme(QuranReadingTheme.DARK) }
                            )
                        }
                    }
                    val chipColors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.emeraldAccent.copy(alpha = 0.14f),
                        selectedLabelColor = palette.emeraldAccent,
                        selectedLeadingIconColor = palette.emeraldAccent,
                        labelColor = palette.secondaryInk
                    )
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.cycleRepeatMode() },
                        label = {
                            Text(
                                text = if (isArabic) {
                                    "تكرار: ${uiState.audioState.repeatMode.labelAr}"
                                } else {
                                    "Repeat ${uiState.audioState.repeatMode.label}"
                                },
                                fontFamily = NotoSansArabicFamily,
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = chipColors
                    )
                }
            }
        }
    }

    if (showSurahPicker) {

        SurahOrPagePickerDialog(
            currentSurahNumber = displayedSurahMeta.number,
            currentPageNumber = currentBookPage,
            strings = strings,
            onSelectSurah = { s ->
                navigateToBookPage(QuranMetadata.surahByNumber(s).startPage)
            },
            onSelectPage = { p ->
                navigateToBookPage(p)
            },
            onDismiss = { showSurahPicker = false }
        )
    }
}

@Composable
private fun ThemeSwatchDot(
    fill: Color,
    selected: Boolean,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(fill)
            .border(
                width = if (selected) 2.4.dp else 1.dp,
                color = if (selected) accent else Color(0xFF9E9E9E),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(tag)
    )
}

/**
 * Bottom sheet shown for the ayah tapped in the digital mushaf reader: its reference plus
 * tafsir, copy and share rows (and a show-tools row while chrome is hidden), styled after
 * the selection sheet in Omar's reference screenshots, in the reader's palette.
 */
@Composable
internal fun AyahActionSheet(
    reference: String,
    palette: MushafPalette,
    isArabic: Boolean,
    showToolsOption: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onTafsir: () -> Unit,
    onShowTools: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = palette.sheetBg,
        border = BorderStroke(0.8.dp, palette.innerFrameBorder.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("ayah_action_strip")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reference,
                    fontFamily = NotoSansArabicFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.inkColor
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("ayah_sheet_close")
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = if (isArabic) "إغلاق" else "Close",
                        tint = palette.secondaryInk,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            AyahActionSheetRow(
                icon = Icons.Filled.MenuBook,
                label = if (isArabic) "التفسير" else "Tafsir",
                palette = palette,
                tag = "ayah_tafsir_button",
                onClick = onTafsir
            )
            AyahActionSheetRow(
                icon = Icons.Filled.ContentCopy,
                label = if (isArabic) "نسخ الآية" else "Copy Ayah",
                palette = palette,
                tag = "ayah_copy_button",
                onClick = onCopy
            )
            AyahActionSheetRow(
                icon = Icons.Filled.Share,
                label = if (isArabic) "مشاركة الآية" else "Share Ayah",
                palette = palette,
                tag = "ayah_share_button",
                onClick = onShare
            )
            if (showToolsOption) {
                AyahActionSheetRow(
                    icon = Icons.Filled.FullscreenExit,
                    label = if (isArabic) "إظهار الأدوات" else "Show Controls",
                    palette = palette,
                    tag = "ayah_show_tools_button",
                    onClick = {
                        onShowTools()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun AyahActionSheetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    palette: MushafPalette,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = palette.emeraldAccent,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontFamily = NotoSansArabicFamily,
            fontSize = 13.sp,
            color = palette.inkColor
        )
    }
}
