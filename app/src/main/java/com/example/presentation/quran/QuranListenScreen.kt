package com.example.presentation.quran

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay30
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranRecitersCatalog
import com.example.data.quran.QuranScriptFormatter
import com.example.data.quran.ReciterOption
import com.example.data.quran.SurahMeta
import com.example.services.QuranAudioState
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.EmeraldAligned
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranListenView(
    uiState: QuranUiState,
    viewModel: QuranViewModel,
    openFullPlayerNonce: Int = 0,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val audio = uiState.audioState
    val selectedReciter = remember(uiState.prefs.selectedReciterId) {
        QuranRecitersCatalog.reciterById(uiState.prefs.selectedReciterId)
    }

    // 0 = Surahs (Tracks), 1 = Reciters (57+), 2 = Favorites, 3 = Downloaded Offline
    var activeTab by rememberSaveable { mutableIntStateOf(0) }
    var listenSearchQuery by rememberSaveable { mutableStateOf("") }
    var showFullPlayer by rememberSaveable { mutableStateOf(openFullPlayerNonce > 0) }
    var handledFullPlayerNonce by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(openFullPlayerNonce) {
        if (openFullPlayerNonce > handledFullPlayerNonce) {
            handledFullPlayerNonce = openFullPlayerNonce
            showFullPlayer = true
        }
    }

    val filteredSurahs = remember(listenSearchQuery) {
        val q = listenSearchQuery.trim()
        if (q.isEmpty()) QuranMetadata.surahs
        else QuranMetadata.surahs.filter {
            it.nameEn.contains(q, ignoreCase = true) ||
                it.nameAr.contains(q) ||
                it.meaningEn.contains(q, ignoreCase = true) ||
                it.number.toString() == q
        }
    }

    val filteredReciters = remember(listenSearchQuery) {
        val q = listenSearchQuery.trim()
        if (q.isEmpty()) QuranRecitersCatalog.reciters
        else QuranRecitersCatalog.reciters.filter {
            it.nameEn.contains(q, ignoreCase = true) ||
                it.nameAr.contains(q) ||
                it.styleEn.contains(q, ignoreCase = true) ||
                it.styleAr.contains(q)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("quran_listen_root")
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
        ) {
            // Search input for Surahs or Reciters
            OutlinedTextField(
                value = listenSearchQuery,
                onValueChange = { listenSearchQuery = it },
                placeholder = {
                    Text(
                        if (activeTab == 1) {
                            if (isArabic) "ابحث في ٥٧ قارئاً (مثل: إسلام صبحي)..." else "Search 57+ reciters (e.g. Islam Sobhi)..."
                        } else {
                            if (isArabic) "ابحث عن سورة للاستماع أو التحميل..." else "Search Surah to listen or download..."
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (listenSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { listenSearchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = if (isArabic) "مسح" else "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_listen_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Tabs matching M3 Expressive header
            val tabs = if (isArabic) {
                listOf(
                    "السور (١١٤)",
                    "القراء (${QuranScriptFormatter.toArabicIndic(QuranRecitersCatalog.reciters.size)})",
                    "المفضلة",
                    "التحميلات"
                )
            } else {
                listOf(
                    "Surahs (114)",
                    "Reciters (${QuranRecitersCatalog.reciters.size})",
                    "Favorites",
                    "Downloaded"
                )
            }

            PrimaryScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("quran_listen_tab_$idx")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pill Sort / Active Reciter & Shuffle + Play All bar (matching Image 1 left screen)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_listen_toolbar_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable { activeTab = if (activeTab == 1) 0 else 1 }
                            .padding(vertical = 4.dp)
                            .testTag("active_reciter_selector_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isArabic) selectedReciter.nameAr else "${selectedReciter.nameEn} • ${selectedReciter.nameAr}",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = NotoSansArabicFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isArabic) "اضغط لتغيير القارئ (٥٧ قارئ) • يعمل والشاشة مغلقة" else "Tap to switch reciter (57+) • Screen-off playback",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleShuffleAudio() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quran_listen_shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = if (isArabic) "تبديل عشوائي" else "Shuffle",
                            tint = if (audio.isShuffleEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    IconButton(
                        onClick = {
                            val targetSurah = if (audio.surahNumber > 0) audio.surahNumber else 1
                            viewModel.playSurahAudio(targetSurah, selectedReciter.id)
                            showFullPlayer = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quran_listen_play_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = if (isArabic) "تشغيل" else "Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content List
            when (activeTab) {
                0 -> {
                    SurahTrackList(
                        surahs = filteredSurahs,
                        selectedReciter = selectedReciter,
                        audio = audio,
                        isArabic = isArabic,
                        onPlaySurah = { surah ->
                            viewModel.playSurahAudio(surah.number, selectedReciter.id)
                        },
                        onOpenFullPlayer = { surah ->
                            if (audio.surahNumber != surah.number || audio.reciterId != selectedReciter.id) {
                                viewModel.playSurahAudio(surah.number, selectedReciter.id)
                            }
                            showFullPlayer = true
                        },
                        onToggleFavorite = { viewModel.toggleFavoriteAudioSurah(it.number) },
                        onDownloadOrDelete = { surah, isDownloaded ->
                            if (isDownloaded) {
                                viewModel.deleteOfflineSurahAudio(surah.number, selectedReciter.id)
                            } else {
                                viewModel.downloadSurahAudioOffline(surah.number, selectedReciter.id)
                            }
                        }
                    )
                }

                1 -> {
                    RecitersCatalogList(
                        reciters = filteredReciters,
                        selectedReciterId = selectedReciter.id,
                        isArabic = isArabic,
                        onSelectReciter = { reciter ->
                            viewModel.selectReciter(reciter.id)
                            activeTab = 0
                        }
                    )
                }

                2 -> {
                    val favSurahs = remember(filteredSurahs, audio.favoriteSurahs) {
                        filteredSurahs.filter { audio.favoriteSurahs.contains(it.number) }
                    }
                    if (favSurahs.isEmpty()) {
                        EmptyListenState(
                            message = if (isArabic) {
                                "لا توجد سور في المفضلة بعد. اضغط على أيقونة القلب لإضافة سورك المفضلة."
                            } else {
                                "No favorite Surahs yet. Tap the heart icon on any Surah to save it here."
                            }
                        )
                    } else {
                        SurahTrackList(
                            surahs = favSurahs,
                            selectedReciter = selectedReciter,
                            audio = audio,
                            isArabic = isArabic,
                            onPlaySurah = { viewModel.playSurahAudio(it.number, selectedReciter.id) },
                            onOpenFullPlayer = { surah ->
                                if (audio.surahNumber != surah.number) {
                                    viewModel.playSurahAudio(surah.number, selectedReciter.id)
                                }
                                showFullPlayer = true
                            },
                            onToggleFavorite = { viewModel.toggleFavoriteAudioSurah(it.number) },
                            onDownloadOrDelete = { surah, isDownloaded ->
                                if (isDownloaded) {
                                    viewModel.deleteOfflineSurahAudio(surah.number, selectedReciter.id)
                                } else {
                                    viewModel.downloadSurahAudioOffline(surah.number, selectedReciter.id)
                                }
                            }
                        )
                    }
                }

                else -> {
                    val downloadedSurahs = remember(filteredSurahs, audio.downloadedKeys, selectedReciter.id) {
                        filteredSurahs.filter { audio.isSurahDownloaded(selectedReciter.id, it.number) }
                    }
                    if (downloadedSurahs.isEmpty()) {
                        EmptyListenState(
                            message = if (isArabic) {
                                "لم تقم بتحميل سور للقارئ ${selectedReciter.nameAr} بعد.\nاضغط على زر التحميل بجانب أي سورة للاستماع إليها دون إنترنت والشاشة مغلقة."
                            } else {
                                "No offline Surahs downloaded for ${selectedReciter.nameEn} yet.\nTap the download icon on any Surah to listen offline anytime."
                            }
                        )
                    } else {
                        SurahTrackList(
                            surahs = downloadedSurahs,
                            selectedReciter = selectedReciter,
                            audio = audio,
                            isArabic = isArabic,
                            onPlaySurah = { viewModel.playSurahAudio(it.number, selectedReciter.id) },
                            onOpenFullPlayer = { surah ->
                                if (audio.surahNumber != surah.number) {
                                    viewModel.playSurahAudio(surah.number, selectedReciter.id)
                                }
                                showFullPlayer = true
                            },
                            onToggleFavorite = { viewModel.toggleFavoriteAudioSurah(it.number) },
                            onDownloadOrDelete = { surah, _ ->
                                viewModel.deleteOfflineSurahAudio(surah.number, selectedReciter.id)
                            }
                        )
                    }
                }
            }
        }

        // Floating Bottom Mini-Player Pill (matching Image 1 bottom pill)
        val activeSurahNumber = if (audio.surahNumber > 0) audio.surahNumber else uiState.prefs.lastReadSurah.coerceIn(1, 114)
        val activeSurahMeta = QuranMetadata.surahByNumber(activeSurahNumber)
        val activeReciterMeta = QuranRecitersCatalog.reciterById(
            if (audio.surahNumber > 0) audio.reciterId else selectedReciter.id
        )

        ExpressiveMiniPlayerPill(
            surah = activeSurahMeta,
            reciter = activeReciterMeta,
            isPlaying = audio.isPlaying,
            isBuffering = audio.isBuffering,
            isOffline = audio.isOfflinePlayback || audio.isSurahDownloaded(activeReciterMeta.id, activeSurahMeta.number),
            isArabic = isArabic,
            onExpand = { showFullPlayer = true },
            onPrevious = { viewModel.previousSurahTrack() },
            onPlayPause = {
                if (audio.surahNumber <= 0) {
                    viewModel.playSurahAudio(activeSurahMeta.number, activeReciterMeta.id)
                } else {
                    viewModel.toggleAudioPlayPause()
                }
            },
            onNext = { viewModel.nextSurahTrack() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 640.dp)
                .fillMaxWidth()
        )

        // Full-Screen Now Playing Sheet / Overlay (matching Image 1 right screen & Image 2)
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            BackHandler { showFullPlayer = false }
            ExpressiveFullNowPlayingScreen(
                surah = activeSurahMeta,
                reciter = activeReciterMeta,
                audio = audio,
                isArabic = isArabic,
                onCollapse = { showFullPlayer = false },
                onOpenReciterPicker = {
                    showFullPlayer = false
                    activeTab = 1
                },
                onPlayPause = {
                    if (audio.surahNumber <= 0) {
                        viewModel.playSurahAudio(activeSurahMeta.number, activeReciterMeta.id)
                    } else {
                        viewModel.toggleAudioPlayPause()
                    }
                },
                onPrevious = { viewModel.previousSurahTrack() },
                onNext = { viewModel.nextSurahTrack() },
                onSeekTo = { viewModel.seekAudioTo(it) },
                onSkipByMs = { viewModel.skipAudioByMs(it) },
                onToggleShuffle = { viewModel.toggleShuffleAudio() },
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onToggleFavorite = { viewModel.toggleFavoriteAudioSurah(activeSurahMeta.number) },
                onDownloadOrDelete = {
                    val downloaded = audio.isSurahDownloaded(activeReciterMeta.id, activeSurahMeta.number)
                    if (downloaded) {
                        viewModel.deleteOfflineSurahAudio(activeSurahMeta.number, activeReciterMeta.id)
                    } else {
                        viewModel.downloadSurahAudioOffline(activeSurahMeta.number, activeReciterMeta.id)
                    }
                },
                onToggleMute = { viewModel.toggleMuteAudio() },
                onAdjustVolume = { viewModel.adjustAudioVolume(it) }
            )
        }
    }
}

@Composable
private fun SurahTrackList(
    surahs: List<SurahMeta>,
    selectedReciter: ReciterOption,
    audio: QuranAudioState,
    isArabic: Boolean,
    onPlaySurah: (SurahMeta) -> Unit,
    onOpenFullPlayer: (SurahMeta) -> Unit,
    onToggleFavorite: (SurahMeta) -> Unit,
    onDownloadOrDelete: (SurahMeta, Boolean) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 104.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_listen_tracks_list")
    ) {
        items(surahs, key = { it.number }) { surah ->
            val isCurrentPlaying = audio.surahNumber == surah.number &&
                (audio.reciterId == selectedReciter.id || audio.isPlaying || audio.isBuffering)
            val isDownloaded = audio.isSurahDownloaded(selectedReciter.id, surah.number)
            val downloadProgress = audio.downloadProgressFor(selectedReciter.id, surah.number)
            val isFavorite = audio.favoriteSurahs.contains(surah.number)

            // Active row morphs into full CircleShape pill with deep primary fill (matching Image 1 left screen)
            val rowShape = if (isCurrentPlaying) CircleShape else RoundedCornerShape(20.dp)
            val rowContainerColor = if (isCurrentPlaying) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
            val titleColor = if (isCurrentPlaying) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val subtitleColor = if (isCurrentPlaying) {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = rowShape,
                color = rowContainerColor,
                tonalElevation = if (isCurrentPlaying) 6.dp else 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFullPlayer(surah) }
                    .testTag("listen_surah_item_${surah.number}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left circular note / surah emblem badge
                    Surface(
                        shape = CircleShape,
                        color = if (isCurrentPlaying) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onPlaySurah(surah) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCurrentPlaying && audio.isPlaying) {
                                Icon(
                                    imageVector = Icons.Filled.Equalizer,
                                    contentDescription = if (isArabic) "جاري التشغيل" else "Playing",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = if (isCurrentPlaying) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) {
                                "${QuranScriptFormatter.toArabicIndic(surah.number)}. سورة ${surah.nameAr}"
                            } else {
                                "${surah.number}. ${surah.nameEn} • سورة ${surah.nameAr}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = NotoSansArabicFamily,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = buildString {
                                append(if (isArabic) selectedReciter.nameAr else selectedReciter.nameEn)
                                append(" • ")
                                append(if (isArabic) "${QuranScriptFormatter.toArabicIndic(surah.ayahCount)} آية" else "${surah.ayahCount} Ayahs")
                                if (isDownloaded) {
                                    append(if (isArabic) " • متاح دون إنترنت" else " • Offline")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = NotoSansArabicFamily,
                            color = subtitleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Favorite button
                    IconButton(
                        onClick = { onToggleFavorite(surah) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("listen_fav_surah_${surah.number}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isCurrentPlaying) {
                                MaterialTheme.colorScheme.onPrimary
                            } else if (isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Offline Download / Progress / Downloaded Check button
                    IconButton(
                        onClick = { onDownloadOrDelete(surah, isDownloaded) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("listen_download_surah_${surah.number}")
                    ) {
                        when {
                            downloadProgress != null -> {
                                CircularProgressIndicator(
                                    progress = { downloadProgress },
                                    strokeWidth = 2.5.dp,
                                    color = if (isCurrentPlaying) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            isDownloaded -> {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Downloaded offline",
                                    tint = if (isCurrentPlaying) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        EmeraldAligned
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Filled.CloudDownload,
                                    contentDescription = "Download offline",
                                    tint = if (isCurrentPlaying) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecitersCatalogList(
    reciters: List<ReciterOption>,
    selectedReciterId: String,
    isArabic: Boolean,
    onSelectReciter: (ReciterOption) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 104.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_reciters_list")
    ) {
        items(reciters, key = { it.id }) { reciter ->
            val isSelected = reciter.id == selectedReciterId
            val shape = if (isSelected) CircleShape else RoundedCornerShape(20.dp)
            val bg = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
            val titleColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val subtitleColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = shape,
                color = bg,
                tonalElevation = if (isSelected) 6.dp else 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectReciter(reciter) }
                    .testTag("reciter_item_${reciter.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.GraphicEq else Icons.Filled.Mic,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) reciter.nameAr else "${reciter.nameEn} • ${reciter.nameAr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = NotoSansArabicFamily,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isArabic) reciter.styleAr else "${reciter.styleEn} • ${reciter.styleAr}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = NotoSansArabicFamily,
                            color = subtitleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveMiniPlayerPill(
    surah: SurahMeta,
    reciter: ReciterOption,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isOffline: Boolean,
    isArabic: Boolean,
    onExpand: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 10.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .clickable { onExpand() }
            .testTag("quran_listen_mini_player")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded-square album/emblem badge on the left (matching Image 1 bottom bar)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) "سورة ${surah.nameAr}" else "${surah.nameEn} • ${surah.nameAr}",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = NotoSansArabicFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(if (isArabic) reciter.nameAr else reciter.nameEn)
                        if (isOffline) append(if (isArabic) " • دون إنترنت" else " • Offline")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = NotoSansArabicFamily,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("mini_player_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            IconButton(
                onClick = onExpand,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("mini_player_expand_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = "Open Now Playing",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ExpressiveFullNowPlayingScreen(
    surah: SurahMeta,
    reciter: ReciterOption,
    audio: QuranAudioState,
    isArabic: Boolean,
    onCollapse: () -> Unit,
    onOpenReciterPicker: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipByMs: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownloadOrDelete: () -> Unit,
    onToggleMute: () -> Unit,
    onAdjustVolume: (Float) -> Unit
) {
    val isDownloaded = audio.isSurahDownloaded(reciter.id, surah.number)
    val downloadProgress = audio.downloadProgressFor(reciter.id, surah.number)
    val isFavorite = audio.favoriteSurahs.contains(surah.number)

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_full_now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with pill collapse button (matching Image 1 right screen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .size(width = 52.dp, height = 38.dp)
                        .clickable { onCollapse() }
                        .testTag("now_playing_collapse_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Screen-off background playback indicator chip
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.clickable { onOpenReciterPicker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LockClock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "يعمل والشاشة مغلقة" else "Screen-Off Ready",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large Circular Artwork Emblem (matching Image 1 right screen centerpiece)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                tonalElevation = 4.dp,
                modifier = Modifier.size(230.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "سورة ${surah.nameAr}",
                            fontFamily = AmiriFamily,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isDownloaded) {
                                if (isArabic) "محفوظة دون إنترنت" else "Downloaded Offline"
                            } else {
                                if (isArabic) {
                                    "${QuranScriptFormatter.toArabicIndic(surah.ayahCount)} آية"
                                } else {
                                    "${surah.ayahCount} Ayahs"
                                }
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = NotoSansArabicFamily,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Centered Surah Title & Reciter Subtitle
            Text(
                text = if (isArabic) "سورة ${surah.nameAr}" else "${surah.nameEn} • سورة ${surah.nameAr}",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = NotoSansArabicFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isArabic) reciter.nameAr else "${reciter.nameEn} • ${reciter.nameAr}",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = NotoSansArabicFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onOpenReciterPicker() }
            )

            if (!audio.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic) "تعذّر تشغيل التلاوة، يرجى التحقق من الاتصال" else audio.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Action Row: Queue / Favorite / Offline Download (matching Image 1 right screen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = "Surah Queue",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("now_playing_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDownloadOrDelete,
                    modifier = Modifier.testTag("now_playing_download_button")
                ) {
                    when {
                        downloadProgress != null -> {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        isDownloaded -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Downloaded Offline",
                                tint = EmeraldAligned
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Filled.CloudDownload,
                                contentDescription = "Download for Offline",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Material 3 Expressive Wavy Scrubber Bar (matching Image 1 & Image 2)
            ExpressiveWavyScrubber(
                positionMs = audio.positionMs,
                durationMs = audio.durationMs,
                isPlaying = audio.isPlaying,
                onSeekTo = onSeekTo,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expressive_wavy_scrubber")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Expressive Transport Controls (Squircle Play/Pause + Tall Pill Skip buttons from Image 1 & Image 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (audio.isShuffleEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Left Tall Rounded Pill / Squircle (Previous Surah)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .size(width = 62.dp, height = 74.dp)
                        .clickable { onPrevious() }
                        .testTag("now_playing_prev_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous Surah",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Center Oversized Rounded-Squircle Play/Pause Button
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(width = 104.dp, height = 80.dp)
                        .clickable { onPlayPause() }
                        .testTag("now_playing_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (audio.isBuffering) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (audio.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                // Right Tall Rounded Pill / Squircle (Next Surah)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .size(width = 62.dp, height = 74.dp)
                        .clickable { onNext() }
                        .testTag("now_playing_next_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next Surah",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                IconButton(onClick = onCycleRepeat) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = if (isArabic) "تكرار" else "Repeat",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isArabic) audio.repeatMode.labelAr else audio.repeatMode.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expressive 30s Skip & Volume Pill Row (matching Image 2 bottom controls)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (audio.isMuted) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { onToggleMute() }
                        .testTag("now_playing_mute_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (audio.isMuted) {
                                Icons.AutoMirrored.Filled.VolumeOff
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = "Mute",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { onSkipByMs(-30_000L) }
                        .testTag("now_playing_rewind_30_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay30,
                            contentDescription = "Rewind 30s",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                            contentDescription = "Volume Down",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onAdjustVolume(-0.15f) }
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { onSkipByMs(30_000L) }
                        .testTag("now_playing_forward_30_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forward30,
                            contentDescription = "Forward 30s",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Volume Up",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onAdjustVolume(0.15f) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Reciter & Queue / Offline Pills (matching Image 2 bottom row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { onOpenReciterPicker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) reciter.nameAr else reciter.nameEn,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = NotoSansArabicFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable { onDownloadOrDelete() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Filled.DeleteOutline else Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = if (isDownloaded) EmeraldAligned else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                downloadProgress != null -> {
                                    val pct = (downloadProgress * 100).toInt()
                                    if (isArabic) "${QuranScriptFormatter.toArabicIndic(pct)}٪" else "$pct%"
                                }
                                isDownloaded -> if (isArabic) "محمل دون إنترنت" else "Offline Ready"
                                else -> if (isArabic) "تحميل للاستماع دون نت" else "Download Offline"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = NotoSansArabicFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Wavy Scrubber matching the attached concept art:
 * - Animated sine wave on the active (left) side of the vertical pill thumb
 * - Vertical rounded pill thumb bar
 * - Flat track on the remaining (right) side
 */
@Composable
fun ExpressiveWavyScrubber(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    val fraction = if (durationMs > 0L) {
        (positionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    } else {
        0.18f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wave_phase")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "wave_phase_anim"
    )

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.primaryContainer

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(safeDuration) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val targetRatio = (offset.x / w).coerceIn(0f, 1f)
                        if (durationMs > 0L) {
                            onSeekTo((targetRatio * durationMs).toLong())
                        }
                    }
                }
                .pointerInput(safeDuration) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        val targetRatio = (change.position.x / w).coerceIn(0f, 1f)
                        if (durationMs > 0L) {
                            onSeekTo((targetRatio * durationMs).toLong())
                        }
                    }
                }
        ) {
            val w = size.width
            val cy = size.height / 2f
            val thumbX = (w * fraction).coerceIn(8.dp.toPx(), (w - 8.dp.toPx()).coerceAtLeast(8.dp.toPx()))
            val amplitude = if (isPlaying) 5.5.dp.toPx() else 3.5.dp.toPx()
            val wavelength = 26.dp.toPx().coerceAtLeast(12f)
            val phase = if (isPlaying) wavePhase else 0f

            // 1. Wavy active path from 0 to thumbX
            val wavePath = Path()
            val endWaveX = (thumbX - 6.dp.toPx()).coerceAtLeast(0f)
            var x = 0f
            val step = 3f
            while (x <= endWaveX) {
                val y = cy + amplitude * sin((2f * PI.toFloat() * x / wavelength) - phase)
                if (x == 0f) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                x += step
            }
            drawPath(
                path = wavePath,
                color = activeColor,
                style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Inactive straight line from thumbX to width
            val startLineX = (thumbX + 6.dp.toPx()).coerceAtMost(w)
            if (startLineX < w) {
                drawLine(
                    color = inactiveColor,
                    start = Offset(startLineX, cy),
                    end = Offset(w, cy),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Vertical pill thumb bar matching the attached M3 Expressive scrubber
            val thumbW = 5.dp.toPx()
            val thumbH = 28.dp.toPx()
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(thumbX - thumbW / 2f, cy - thumbH / 2f),
                size = Size(thumbW, thumbH),
                cornerRadius = CornerRadius(thumbW / 2f, thumbW / 2f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMillisToMinutesSeconds(positionMs),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = RobotoMonoFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (durationMs > 0L) formatMillisToMinutesSeconds(durationMs) else "--:--",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = RobotoMonoFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatMillisToMinutesSeconds(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }
}

@Composable
private fun EmptyListenState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = NotoSansArabicFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
