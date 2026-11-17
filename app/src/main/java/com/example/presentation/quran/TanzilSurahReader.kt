package com.example.presentation.quran

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var isFullscreenMode by remember { mutableStateOf(false) }

    // Load QuranFlash Medina1 page image & persist last-read Mushaf page whenever current pager page changes
    LaunchedEffect(pagerState.currentPage) {
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
            .background(palette.pageBg)
    ) {
        // 1. Mushaf Pages Top Bar (collapsible in fullscreen mode)
        AnimatedVisibility(visible = !isFullscreenMode) {
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
                    }
                )

                // 2. Mushaf Pages Only Strip + App Theme & Paper Swatches + Audio Qaree Chips
                Surface(
                    color = palette.sheetBg,
                    border = BorderStroke(0.6.dp, palette.innerFrameBorder.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = palette.chromeBg,
                                shadowElevation = 1.dp,
                                modifier = Modifier.testTag("tanzil_mode_mushaf")
                            ) {
                                Text(
                                    text = if (isArabic) {
                                        "صفحات المصحف • ${readingTheme.labelAr}"
                                    } else {
                                        "Mushaf Pages • ${readingTheme.labelEn}"
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

                        // Listen & Reciter Chips Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val chipColors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.emeraldAccent.copy(alpha = 0.14f),
                                selectedLabelColor = palette.emeraldAccent,
                                selectedLeadingIconColor = palette.emeraldAccent,
                                labelColor = palette.secondaryInk
                            )

                            val isCurrentSurahAudio = uiState.audioState.surahNumber == displayedSurahMeta.number &&
                                uiState.audioState.surahNumber > 0 &&
                                (uiState.audioState.isPlaying || uiState.audioState.isBuffering || uiState.audioState.isPaused)
                            val isPlayingSurah = isCurrentSurahAudio && uiState.audioState.isPlaying
                            val isBufferingSurah = isCurrentSurahAudio && uiState.audioState.isBuffering
                            val isPausedSurah = isCurrentSurahAudio && uiState.audioState.isPaused
                            FilterChip(
                                selected = isCurrentSurahAudio,
                                onClick = {
                                    if (isCurrentSurahAudio) {
                                        viewModel.toggleAudioPlayPause()
                                    } else {
                                        viewModel.playSurahAudio(displayedSurahMeta.number)
                                    }
                                },
                                label = {
                                    val labelText = when {
                                        isPlayingSurah -> if (isArabic) "إيقاف مؤقت (${activeReciter.nameAr})" else "Pause (${activeReciter.nameEn})"
                                        isBufferingSurah -> if (isArabic) "جاري التحميل..." else "Loading..."
                                        isPausedSurah -> if (isArabic) "متابعة (${activeReciter.nameAr})" else "Continue (${activeReciter.nameEn})"
                                        else -> if (isArabic) "استماع (${activeReciter.nameAr})" else "Listen (${activeReciter.nameEn})"
                                    }
                                    Text(
                                        text = labelText,
                                        fontFamily = NotoSansArabicFamily,
                                        fontSize = 11.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isPlayingSurah) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = chipColors,
                                modifier = Modifier.testTag("medina_play_surah_chip")
                            )

                            Box {
                                FilterChip(
                                    selected = false,
                                    onClick = { showReciterMenu = true },
                                    label = {
                                        Text(
                                            text = if (isArabic) activeReciter.nameAr else activeReciter.nameEn,
                                            fontFamily = NotoSansArabicFamily,
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    colors = chipColors,
                                    modifier = Modifier.testTag("ksu_qaree_selector_chip")
                                )
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
        }

        // 3. Authentic 604-Page RTL Medina1 Mushaf Pager (صفحات المصحف فقط)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                    }
                }

                // Bottom Page Navigation & Scrubber Bar
                AnimatedVisibility(visible = !isFullscreenMode) {
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

            // Exit Fullscreen Floating Pill
            if (isFullscreenMode) {
                Surface(
                    shape = CircleShape,
                    color = palette.chromeBg.copy(alpha = 0.92f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clickable { isFullscreenMode = false }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FullscreenExit,
                            contentDescription = if (isArabic) "إظهار الأدوات" else "Show Toolbars",
                            tint = palette.chromeInk,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isArabic) "إظهار الأدوات" else "Controls",
                            fontFamily = NotoSansArabicFamily,
                            fontSize = 11.sp,
                            color = palette.chromeInk
                        )
                    }
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
