package com.example.presentation.quran

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.design.ExpressiveCookie4Shape
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranScriptFormatter
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.EmeraldAligned
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily

@Composable
fun QuranScreen(
    uiState: QuranUiState,
    viewModel: QuranViewModel,
    openListenSurahNumber: Int = 0,
    openListenPlayerNonce: Int = 0,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    var quranMode by rememberSaveable { mutableIntStateOf(if (openListenPlayerNonce > 0) 1 else 0) } // 0 = Read, 1 = Listen
    var handledListenNonce by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(openListenPlayerNonce, openListenSurahNumber) {
        if (openListenPlayerNonce > handledListenNonce) {
            handledListenNonce = openListenPlayerNonce
            quranMode = 1
            if (openListenSurahNumber > 0) {
                viewModel.selectSurahForListenPlayer(openListenSurahNumber)
            }
        }
    }

    val isReaderOpen = uiState.selectedSurahNumber != null || uiState.selectedMushafPage != null

    Box(modifier = modifier.fillMaxSize()) {
        if (isReaderOpen) {
            TanzilSurahReaderView(
                surahNumber = uiState.selectedSurahNumber,
                mushafPage = uiState.selectedMushafPage,
                uiState = uiState,
                viewModel = viewModel,
                onBack = { viewModel.closeSurahReader() }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top 2-Option Read / Listen M3 Expressive Pill Selector
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .testTag("quran_mode_selector")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val readSelected = quranMode == 0
                        Surface(
                            shape = CircleShape,
                            color = if (readSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { quranMode = 0 }
                                .testTag("quran_mode_read")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = if (readSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "قراءة المصحف" else "Read Mushaf",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = NotoSansArabicFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (readSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val listenSelected = quranMode == 1
                        Surface(
                            shape = CircleShape,
                            color = if (listenSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { quranMode = 1 }
                                .testTag("quran_mode_listen")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Headphones,
                                    contentDescription = null,
                                    tint = if (listenSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "استماع (٥٧+ قارئ)" else "Listen (57+ Reciters)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = NotoSansArabicFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (listenSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (quranMode == 0) {
                        QuranBrowserView(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    } else {
                        QuranListenView(
                            uiState = uiState,
                            viewModel = viewModel,
                            openFullPlayerNonce = openListenPlayerNonce
                        )
                    }
                }
            }
        }

        // Persistent Audio Bar when in Read mode (Listen mode has its own Expressive Mini Player Pill & Full Now Playing screen)
        val audio = uiState.audioState
        if ((quranMode == 0 || isReaderOpen) && audio.surahNumber > 0 && (audio.isPlaying || audio.isBuffering)) {
            val surahMeta = QuranMetadata.surahByNumber(audio.surahNumber)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .testTag("quran_audio_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${surahMeta.nameEn} • ${surahMeta.nameAr}",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = NotoSansArabicFamily,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (audio.isBuffering) {
                                "Buffering..."
                            } else if (audio.isFullSurahMode) {
                                QuranMetadata.reciters.find { it.id == audio.reciterId }?.nameAr ?: ""
                            } else {
                                "Ayah ${audio.ayahNumber} (${audio.currentPlayCount}/${if (audio.repeatMode.maxPlays > 100) "∞" else audio.repeatMode.maxPlays})"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = RobotoMonoFamily,
                            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f)
                        )
                    }
                    if (!audio.isFullSurahMode) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.cycleRepeatMode() },
                            label = { Text(audio.repeatMode.label, fontFamily = RobotoMonoFamily) },
                            leadingIcon = { Icon(Icons.Filled.Repeat, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    IconButton(onClick = { viewModel.toggleAudioPlayPause() }) {
                        Icon(
                            imageVector = if (audio.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                    IconButton(onClick = { viewModel.stopAudio() }) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranBrowserView(
    uiState: QuranUiState,
    viewModel: QuranViewModel
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Surahs, 1=Pages, 2=Juz, 3=Bookmarks

    val filteredSurahs = remember(uiState.searchQuery) {
        val q = uiState.searchQuery.trim()
        if (q.isEmpty()) QuranMetadata.surahs
        else QuranMetadata.surahs.filter {
            it.nameEn.contains(q, ignoreCase = true) ||
                it.nameAr.contains(q) ||
                it.meaningEn.contains(q, ignoreCase = true) ||
                it.number.toString() == q
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(strings.searchQuranHint) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(0, 4),
                    modifier = Modifier.testTag("quran_tab_surahs")
                ) { Text(strings.surahsTab, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(1, 4),
                    modifier = Modifier.testTag("quran_tab_pages")
                ) { Text(strings.mushafPagesTab, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                SegmentedButton(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    shape = SegmentedButtonDefaults.itemShape(2, 4),
                    modifier = Modifier.testTag("quran_tab_juz")
                ) { Text(strings.juzTab, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                SegmentedButton(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    shape = SegmentedButtonDefaults.itemShape(3, 4),
                    modifier = Modifier.testTag("quran_tab_bookmarks")
                ) { Text(strings.bookmarksTab, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.searchQuery.trim().length >= 2 && uiState.verseSearchResults.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(uiState.verseSearchResults, key = { it.globalNumber }) { verse ->
                        val meta = QuranMetadata.surahByNumber(verse.surahNumber)
                        val cleanSnippet = QuranScriptFormatter.stripLeadingBismillah(
                            surahNumber = verse.surahNumber,
                            ayahNumber = verse.ayahNumber,
                            text = verse.arabicText
                        )
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openSurah(verse.surahNumber, verse.ayahNumber) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${meta.nameEn} (${verse.surahNumber}:${verse.ayahNumber}) • ${strings.pageLabel} ${verse.pageNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = cleanSnippet,
                                    fontFamily = AmiriFamily,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (verse.englishTranslation.isNotBlank()) {
                                    Text(
                                        text = verse.englishTranslation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            // Continue Reading Hero Card
                            item {
                                val lastMeta = QuranMetadata.surahByNumber(uiState.prefs.lastReadSurah)
                                Card(
                                    shape = RoundedCornerShape(26.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openSurah(lastMeta.number, uiState.prefs.lastReadAyah) }
                                        .testTag("continue_reading_card")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = ExpressiveCookie4Shape(),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = strings.continueReading,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = if (isArabic) "سورة ${lastMeta.nameAr}" else "${lastMeta.nameEn} • ${lastMeta.nameAr}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontFamily = NotoSansArabicFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Ayah ${uiState.prefs.lastReadAyah} • ${strings.pageLabel} ${lastMeta.startPage}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontFamily = RobotoMonoFamily
                                            )
                                        }
                                    }
                                }
                            }

                            // Offline Full Quran Download Card
                            item {
                                val dl = uiState.downloadProgress
                                val allCached = uiState.cachedSurahs.size >= 114
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        if (allCached) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldAligned)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${strings.offlineReady} (114 / 114)",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = EmeraldAligned
                                                )
                                            }
                                        } else if (dl != null && !dl.isFinished) {
                                            Text(
                                                text = "${strings.downloadingQuran} ${dl.currentSurahName} (${dl.completedSurahs}/114)",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { dl.completedSurahs / 114f },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            FilledTonalButton(
                                                onClick = { viewModel.downloadAllOffline() },
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("download_full_quran_button")
                                            ) {
                                                Icon(Icons.Filled.CloudDownload, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("${strings.downloadFullQuran} (${uiState.cachedSurahs.size}/114)")
                                            }
                                            if (dl?.error != null) {
                                                Text(
                                                    text = dl.error,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            items(filteredSurahs, key = { it.number }) { surah ->
                                val isCached = uiState.cachedSurahs.contains(surah.number)
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openSurah(surah.number) }
                                        .testTag("surah_item_${surah.number}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = ExpressiveCookie4Shape(),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${surah.number}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontFamily = RobotoMonoFamily,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = surah.nameEn,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${if (surah.isMakkan) strings.makkan else strings.madinan} • ${surah.ayahCount} ${strings.versesLabel} • ${strings.pageLabel} ${surah.startPage}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isCached) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = strings.offlineReady,
                                                tint = EmeraldAligned,
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = surah.nameAr,
                                            fontFamily = NotoSansArabicFamily,
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        MushafPagesBrowserGrid(
                            uiState = uiState,
                            viewModel = viewModel,
                            isArabic = isArabic
                        )
                    }

                    2 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(QuranMetadata.juzList, key = { it.number }) { juz ->
                                val surah = QuranMetadata.surahByNumber(juz.startSurah)
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openMushafPage(juz.startPage) }
                                        .testTag("juz_card_${juz.number}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "${strings.juzTab} ${juz.number} • ${strings.pageLabel} ${juz.startPage}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontFamily = RobotoMonoFamily,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = juz.nameAr,
                                            fontFamily = NotoSansArabicFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${surah.nameEn} : ${juz.startAyah}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        if (uiState.bookmarks.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.noBookmarksYet,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 84.dp)
                            ) {
                                items(uiState.bookmarks, key = { it.id }) { bm ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.openSurah(bm.surahNumber, bm.ayahNumber) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${bm.surahNameEn} • ${bm.surahNumber}:${bm.ayahNumber}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = bm.arabicSnippet,
                                                    fontFamily = AmiriFamily,
                                                    fontSize = 19.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(onClick = { viewModel.removeBookmark(bm) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Remove bookmark")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MushafPagesBrowserGrid(
    uiState: QuranUiState,
    viewModel: QuranViewModel,
    isArabic: Boolean
) {
    val strings = LocalStrings.current
    val khatmah = uiState.khatmahState
    var pageInput by remember { mutableStateOf("1") }
    val allPages = remember { (1..QuranMetadata.TOTAL_MUSHAF_PAGES).toList() }
    val starterPages = remember { listOf(1, 601, 602, 603, 604) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 84.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 84.dp),
        modifier = Modifier.testTag("mushaf_pages_grid")
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isArabic) "مصحف المدينة المنورة (٦٠٤ صفحة)" else "Medina Mushaf Layout (604 Pages)",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = NotoSansArabicFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mushaf_page_input")
                        )
                        Button(
                            onClick = {
                                val target = pageInput.toIntOrNull()?.coerceIn(1, 604) ?: 1
                                viewModel.openMushafPage(target)
                            },
                            shape = CircleShape,
                            modifier = Modifier.testTag("mushaf_page_go_button")
                        ) {
                            Text("Go")
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        starterPages.forEach { sp ->
                            AssistChip(
                                onClick = { viewModel.openMushafPage(sp) },
                                label = {
                                    Text("${strings.pageLabel} $sp", fontFamily = RobotoMonoFamily)
                                },
                                modifier = Modifier.testTag("mushaf_quick_page_$sp")
                            )
                        }
                    }
                }
            }
        }

        // Mihrab Khatmah Progress & Schedule Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("khatmah_progress_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) {
                                    "ختمة القرآن • ${khatmah.completedPagesCount} / 604 صفحة"
                                } else {
                                    "Khatmah Progress • ${khatmah.completedPagesCount} / 604 Pages"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = NotoSansArabicFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (isArabic) {
                                    "الورد اليومي: ${khatmah.dailyPagesGoal} صفحة (${khatmah.targetDays} يوماً)"
                                } else {
                                    "Daily goal: ${khatmah.dailyPagesGoal} pages/day (${khatmah.targetDays}-day plan)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = NotoSansArabicFamily,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        FilledTonalButton(
                            onClick = { viewModel.openMushafPage(khatmah.nextUnreadPage) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("khatmah_continue_button")
                        ) {
                            Text(
                                text = if (isArabic) "صفحة ${khatmah.nextUnreadPage}" else "Page ${khatmah.nextUnreadPage}",
                                fontFamily = RobotoMonoFamily,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { khatmah.progressFraction },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(30, 60, 90).forEach { days ->
                            FilterChip(
                                selected = khatmah.targetDays == days,
                                onClick = { viewModel.setKhatmahTargetDays(days) },
                                label = {
                                    Text(
                                        text = if (isArabic) "$days يوم" else "${days}d",
                                        fontFamily = RobotoMonoFamily
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        items(allPages, key = { it }) { pageNum ->
            val surahNames = remember(pageNum) {
                QuranMetadata.surahsOnPage(pageNum).joinToString(" • ") { it.nameAr }
            }
            val juzNum = remember(pageNum) { QuranMetadata.juzForPage(pageNum).number }
            val hizbNum = remember(pageNum) { QuranScriptFormatter.hizbForPage(pageNum) }
            val isPageRead = khatmah.readPages.contains(pageNum)
            val isStarterOffline = pageNum in starterPages
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isPageRead -> EmeraldAligned.copy(alpha = 0.18f)
                    isStarterOffline -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                    else -> MaterialTheme.colorScheme.surfaceContainer
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openMushafPage(pageNum) }
                    .testTag("mushaf_page_tile_$pageNum")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$pageNum",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = RobotoMonoFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isPageRead) EmeraldAligned else MaterialTheme.colorScheme.primary
                        )
                        if (isPageRead) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Read",
                                tint = EmeraldAligned,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = surahNames,
                        fontFamily = NotoSansArabicFamily,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "J$juzNum • H$hizbNum",
                        fontFamily = RobotoMonoFamily,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}
