package com.example.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.AppContainer
import com.example.data.local.BookmarkEntity
import com.example.data.local.QuranVerseEntity
import com.example.data.preferences.UserPreferences
import com.example.data.quran.DownloadProgress
import com.example.data.quran.QuranMetadata
import com.example.services.QuranAudioState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuranUiState(
    val prefs: UserPreferences = UserPreferences(),
    val selectedSurahNumber: Int? = null,
    val selectedMushafPage: Int? = null,
    val currentSurahVerses: List<QuranVerseEntity> = emptyList(),
    val cachedSurahs: Set<Int> = emptySet(),
    val totalCachedVerses: Int = 0,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val searchQuery: String = "",
    val verseSearchResults: List<QuranVerseEntity> = emptyList(),
    val isLoadingSurah: Boolean = false,
    val surahLoadError: String? = null,
    val downloadProgress: DownloadProgress? = null,
    val audioState: QuranAudioState = QuranAudioState()
)

@OptIn(ExperimentalCoroutinesApi::class)
class QuranViewModel(private val container: AppContainer) : ViewModel() {

    private val selectedSurah = MutableStateFlow<Int?>(null)
    private val selectedMushafPage = MutableStateFlow<Int?>(null)
    private val searchQuery = MutableStateFlow("")
    private val verseSearchResults = MutableStateFlow<List<QuranVerseEntity>>(emptyList())
    private val isLoadingSurah = MutableStateFlow(false)
    private val surahLoadError = MutableStateFlow<String?>(null)
    private val downloadProgress = MutableStateFlow<DownloadProgress?>(null)

    private val currentVersesFlow = combine(selectedSurah, selectedMushafPage) { surah, page ->
        surah to page
    }.flatMapLatest { (surah, page) ->
        when {
            page != null -> container.quranRepository.observePageVerses(page)
            surah != null -> container.quranRepository.observeSurahVerses(surah)
            else -> flowOf(emptyList())
        }
    }

    val uiState: StateFlow<QuranUiState> = combine(
        container.preferencesDataStore.preferencesFlow,
        selectedSurah,
        selectedMushafPage,
        currentVersesFlow,
        container.quranRepository.observeCachedSurahNumbers(),
        container.quranRepository.observeTotalVerseCount(),
        container.quranRepository.observeBookmarks(),
        searchQuery,
        verseSearchResults,
        isLoadingSurah,
        surahLoadError,
        downloadProgress,
        container.quranAudioManager.state
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        QuranUiState(
            prefs = values[0] as UserPreferences,
            selectedSurahNumber = values[1] as Int?,
            selectedMushafPage = values[2] as Int?,
            currentSurahVerses = values[3] as List<QuranVerseEntity>,
            cachedSurahs = (values[4] as List<Int>).toSet(),
            totalCachedVerses = values[5] as Int,
            bookmarks = values[6] as List<BookmarkEntity>,
            searchQuery = values[7] as String,
            verseSearchResults = values[8] as List<QuranVerseEntity>,
            isLoadingSurah = values[9] as Boolean,
            surahLoadError = values[10] as String?,
            downloadProgress = values[11] as DownloadProgress?,
            audioState = values[12] as QuranAudioState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = QuranUiState()
    )

    fun openSurah(surahNumber: Int, ayahNumber: Int = 1) {
        selectedMushafPage.value = null
        selectedSurah.value = surahNumber
        surahLoadError.value = null
        viewModelScope.launch {
            container.preferencesDataStore.updateLastRead(surahNumber, ayahNumber)
            isLoadingSurah.value = true
            val res = container.quranRepository.fetchAndCacheSurahIfMissing(surahNumber)
            res.onFailure { err ->
                surahLoadError.value = err.localizedMessage ?: "Unable to fetch Surah offline"
            }
            isLoadingSurah.value = false
        }
    }

    fun openMushafPage(pageNumber: Int) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        selectedSurah.value = null
        selectedMushafPage.value = clamped
        surahLoadError.value = null
        viewModelScope.launch {
            isLoadingSurah.value = true
            val res = container.quranRepository.fetchAndCachePageIfMissing(clamped)
            res.onFailure { err ->
                surahLoadError.value = err.localizedMessage ?: "Unable to fetch Mushaf Page offline"
            }
            val firstVerse = res.getOrNull()?.firstOrNull()
            if (firstVerse != null) {
                container.preferencesDataStore.updateLastRead(firstVerse.surahNumber, firstVerse.ayahNumber)
            }
            isLoadingSurah.value = false
        }
    }

    fun closeSurahReader() {
        selectedSurah.value = null
        selectedMushafPage.value = null
        surahLoadError.value = null
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
        viewModelScope.launch {
            verseSearchResults.value = if (query.trim().length >= 2) {
                container.quranRepository.searchVerses(query)
            } else {
                emptyList()
            }
        }
    }

    fun toggleBookmark(verse: QuranVerseEntity) {
        viewModelScope.launch {
            val exists = uiState.value.bookmarks.any {
                it.surahNumber == verse.surahNumber && it.ayahNumber == verse.ayahNumber
            }
            container.quranRepository.setBookmark(verse, !exists)
            container.preferencesDataStore.updateLastRead(verse.surahNumber, verse.ayahNumber)
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            container.quranRepository.setBookmark(
                QuranVerseEntity(
                    globalNumber = 0,
                    surahNumber = bookmark.surahNumber,
                    ayahNumber = bookmark.ayahNumber,
                    arabicText = bookmark.arabicSnippet,
                    englishTranslation = "",
                    juzNumber = 1
                ),
                bookmarked = false
            )
        }
    }

    fun downloadAllOffline() {
        if (downloadProgress.value != null && downloadProgress.value?.isFinished == false) return
        viewModelScope.launch {
            container.quranRepository.downloadAllSurahsOffline().collect { progress ->
                downloadProgress.value = progress
            }
        }
    }

    fun setFontSize(sizeSp: Float) = viewModelScope.launch {
        container.preferencesDataStore.updateQuranFontSize(sizeSp)
    }

    fun toggleNightMode() = viewModelScope.launch {
        container.preferencesDataStore.updateQuranNightMode(!uiState.value.prefs.quranNightMode)
    }

    fun toggleTranslation() = viewModelScope.launch {
        container.preferencesDataStore.updateShowTranslation(!uiState.value.prefs.showTranslation)
    }

    fun toggleMushafMode() = viewModelScope.launch {
        container.preferencesDataStore.updateQuranMushafMode(!uiState.value.prefs.quranMushafMode)
    }

    fun toggleSimpleScript() = viewModelScope.launch {
        container.preferencesDataStore.updateQuranSimpleScript(!uiState.value.prefs.quranSimpleScript)
    }

    fun selectReciter(reciterId: String) {
        viewModelScope.launch {
            container.preferencesDataStore.updateReciter(reciterId)
            val audio = uiState.value.audioState
            if (audio.isFullSurahMode && audio.surahNumber > 0 && (audio.isPlaying || audio.isBuffering)) {
                container.quranAudioManager.playSurah(audio.surahNumber, reciterId)
            }
        }
    }

    fun selectSurahForListenPlayer(
        surahNumber: Int,
        reciterId: String = uiState.value.prefs.selectedReciterId
    ) {
        container.quranAudioManager.selectSurahForPlayer(surahNumber, reciterId)
    }

    fun playSurahAudio(
        surahNumber: Int,
        reciterId: String = uiState.value.prefs.selectedReciterId
    ) {
        viewModelScope.launch {
            if (reciterId != uiState.value.prefs.selectedReciterId) {
                container.preferencesDataStore.updateReciter(reciterId)
            }
        }
        container.quranAudioManager.playSurah(surahNumber, reciterId)
    }

    fun playVerse(verse: QuranVerseEntity) {
        val verses = uiState.value.currentSurahVerses.map { it.ayahNumber to it.globalNumber }
        container.quranAudioManager.playVerse(
            surahNumber = verse.surahNumber,
            ayahNumber = verse.ayahNumber,
            globalNumber = verse.globalNumber,
            reciterId = uiState.value.prefs.selectedReciterId,
            surahVerses = verses
        )
        viewModelScope.launch {
            container.preferencesDataStore.updateLastRead(verse.surahNumber, verse.ayahNumber)
        }
    }

    fun toggleAudioPlayPause() = container.quranAudioManager.togglePlayPause()
    fun stopAudio() = container.quranAudioManager.stop()
    fun cycleRepeatMode() = container.quranAudioManager.cycleRepeatMode()
    fun toggleShuffleAudio() = container.quranAudioManager.toggleShuffle()
    fun nextSurahTrack() = container.quranAudioManager.nextTrack()
    fun previousSurahTrack() = container.quranAudioManager.previousTrack()
    fun seekAudioTo(positionMs: Long) = container.quranAudioManager.seekTo(positionMs)
    fun skipAudioByMs(deltaMs: Long) = container.quranAudioManager.skipByMs(deltaMs)
    fun toggleMuteAudio() = container.quranAudioManager.toggleMute()
    fun adjustAudioVolume(delta: Float) = container.quranAudioManager.adjustVolume(delta)
    fun toggleFavoriteAudioSurah(surahNumber: Int) = container.quranAudioManager.toggleFavoriteSurah(surahNumber)

    fun downloadSurahAudioOffline(
        surahNumber: Int,
        reciterId: String = uiState.value.prefs.selectedReciterId
    ) = container.quranAudioManager.downloadSurahForOffline(surahNumber, reciterId)

    fun deleteOfflineSurahAudio(
        surahNumber: Int,
        reciterId: String = uiState.value.prefs.selectedReciterId
    ) = container.quranAudioManager.deleteDownloadedSurah(surahNumber, reciterId)

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuranViewModel(container) as T
        }
    }
}
