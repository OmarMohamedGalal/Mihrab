package com.example.presentation.quran

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.AppContainer
import com.example.data.local.BookmarkEntity
import com.example.data.local.QuranVerseEntity
import com.example.data.preferences.UserPreferences
import com.example.data.quran.DownloadProgress
import com.example.data.quran.KsuTafsirEdition
import com.example.data.quran.MushafPageLineLayout
import com.example.data.quran.QuranMetadata
import com.example.services.QuranAudioState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.ceil

enum class QuranReadingTheme(val labelAr: String, val labelEn: String) {
    KSU_MUSHAF("سمة التطبيق", "App Theme"),
    LIGHT("أبيض", "Light"),
    SEPIA("بني", "Sepia"),
    DARK("داكن", "Dark")
}

enum class QuranFontStyle(val labelAr: String, val labelEn: String) {
    UTHMANI_HAFS("خط حفص (المدينة)", "Uthmani Hafs"),
    NASKH_SIL("نسخ المدينة", "Madinah Naskh"),
    AMIRI_CLASSIC("الأميري", "Amiri")
}

data class KsuTafsirState(
    val verseKey: String = "",
    val edition: KsuTafsirEdition = KsuTafsirEdition.AR_MUYASSAR,
    val text: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface MedinaPageImageState {
    data object Loading : MedinaPageImageState
    data class Ready(val file: File) : MedinaPageImageState
    data class Error(val message: String) : MedinaPageImageState
}

data class KhatmahProgressState(
    val readPages: Set<Int> = emptySet(),
    val targetDays: Int = 30
) {
    val completedPagesCount: Int
        get() = readPages.size

    val progressFraction: Float
        get() = (readPages.size.toFloat() / QuranMetadata.TOTAL_MUSHAF_PAGES).coerceIn(0f, 1f)

    val dailyPagesGoal: Int
        get() = ceil(QuranMetadata.TOTAL_MUSHAF_PAGES.toDouble() / targetDays.coerceAtLeast(1)).toInt()

    val currentReachPage: Int
        get() = readPages.maxOrNull() ?: 1

    val nextUnreadPage: Int
        get() = (1..QuranMetadata.TOTAL_MUSHAF_PAGES).firstOrNull { !readPages.contains(it) } ?: 1
}

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
    val audioState: QuranAudioState = QuranAudioState(),
    val tajweedEnabled: Boolean = false,
    val khatmahState: KhatmahProgressState = KhatmahProgressState(),
    val readingTheme: QuranReadingTheme = QuranReadingTheme.KSU_MUSHAF,
    val fontStyle: QuranFontStyle = QuranFontStyle.UTHMANI_HAFS,
    val pageLineLayouts: Map<Int, MushafPageLineLayout> = emptyMap(),
    val ksuScanImageMode: Boolean = true,
    val medinaPageImages: Map<Int, MedinaPageImageState> = emptyMap(),
    val selectedTafsirEdition: KsuTafsirEdition = KsuTafsirEdition.AR_MUYASSAR,
    val tafsirState: KsuTafsirState = KsuTafsirState(),
    val requestedJumpAyah: Pair<Int, Int>? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class QuranViewModel(private val container: AppContainer) : ViewModel() {

    private val khatmahPrefs = container.appContext.getSharedPreferences("mihrab.quran.v1", Context.MODE_PRIVATE)

    private val selectedSurah = MutableStateFlow<Int?>(null)
    private val selectedMushafPage = MutableStateFlow<Int?>(null)
    private val searchQuery = MutableStateFlow("")
    private val verseSearchResults = MutableStateFlow<List<QuranVerseEntity>>(emptyList())
    private val isLoadingSurah = MutableStateFlow(false)
    private val surahLoadError = MutableStateFlow<String?>(null)
    private val downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    private val tajweedEnabled = MutableStateFlow(khatmahPrefs.getBoolean("tajweed_enabled", false))
    private val khatmahState = MutableStateFlow(loadKhatmahState())
    private val readingTheme = MutableStateFlow(loadReadingTheme())
    private val fontStyle = MutableStateFlow(loadFontStyle())
    private val pageLineLayouts = MutableStateFlow<Map<Int, MushafPageLineLayout>>(emptyMap())
    // Digital Madinah text mode is the default (offline, screen-sized real text);
    // scan-plate image mode remains available via the reader toggle. New pref key migrates existing installs.
    private val ksuScanImageMode = MutableStateFlow(khatmahPrefs.getBoolean("quranflash_medina1_pages_v3", false))
    private val medinaPageImages = MutableStateFlow<Map<Int, MedinaPageImageState>>(emptyMap())
    private val selectedTafsirEdition = MutableStateFlow(loadTafsirEdition())
    private val tafsirState = MutableStateFlow(KsuTafsirState())
    private val requestedJumpAyah = MutableStateFlow<Pair<Int, Int>?>(null)
    private val optimisticPrefOverrides = MutableStateFlow(OptimisticPrefOverrides())

    private var tafsirFetchJob: Job? = null

    private fun loadReadingTheme(): QuranReadingTheme {
        val raw = khatmahPrefs.getString("reading_theme", QuranReadingTheme.KSU_MUSHAF.name)
        return QuranReadingTheme.entries.firstOrNull { it.name == raw } ?: QuranReadingTheme.KSU_MUSHAF
    }

    private fun loadFontStyle(): QuranFontStyle {
        val raw = khatmahPrefs.getString("quran_font_style", QuranFontStyle.UTHMANI_HAFS.name)
        return QuranFontStyle.entries.firstOrNull { it.name == raw } ?: QuranFontStyle.UTHMANI_HAFS
    }

    private fun loadTafsirEdition(): KsuTafsirEdition {
        val raw = khatmahPrefs.getString("ksu_tafsir_edition", KsuTafsirEdition.AR_MUYASSAR.name)
        return KsuTafsirEdition.entries.firstOrNull { it.name == raw } ?: KsuTafsirEdition.AR_MUYASSAR
    }

    private fun loadKhatmahState(): KhatmahProgressState {
        val rawPages = khatmahPrefs.getStringSet("read_pages", emptySet()).orEmpty()
        val parsedPages = rawPages.mapNotNull { it.toIntOrNull() }
            .filter { it in 1..QuranMetadata.TOTAL_MUSHAF_PAGES }
            .toSet()
        val targetDays = khatmahPrefs.getInt("target_days", 30).coerceIn(7, 365)
        return KhatmahProgressState(readPages = parsedPages, targetDays = targetDays)
    }

    private fun persistKhatmahState(state: KhatmahProgressState) {
        khatmahState.value = state
        khatmahPrefs.edit()
            .putStringSet("read_pages", state.readPages.map { it.toString() }.toSet())
            .putInt("target_days", state.targetDays)
            .apply()
    }

    private val currentVersesFlow = combine(selectedSurah, selectedMushafPage) { surah, page ->
        surah to page
    }.flatMapLatest { (surah, page) ->
        when {
            page != null -> container.quranRepository.observePageVerses(page)
            surah != null -> container.quranRepository.observeSurahVerses(surah)
            else -> flowOf(emptyList())
        }
    }

    private data class OptimisticPrefOverrides(
        val quranMushafMode: Boolean? = null,
        val quranSimpleScript: Boolean? = null,
        val showTranslation: Boolean? = null
    )

    private data class ReaderAppearanceState(
        val tajweedEnabled: Boolean,
        val khatmahState: KhatmahProgressState,
        val readingTheme: QuranReadingTheme,
        val fontStyle: QuranFontStyle,
        val pageLineLayouts: Map<Int, MushafPageLineLayout>,
        val ksuScanImageMode: Boolean,
        val medinaPageImages: Map<Int, MedinaPageImageState>,
        val selectedTafsirEdition: KsuTafsirEdition,
        val tafsirState: KsuTafsirState,
        val requestedJumpAyah: Pair<Int, Int>?,
        val prefOverrides: OptimisticPrefOverrides
    )

    private val ksuStudyFlow = combine(
        ksuScanImageMode,
        medinaPageImages,
        selectedTafsirEdition,
        tafsirState,
        requestedJumpAyah
    ) { scanMode, pageImgs, edition, tafsir, jump ->
        listOf(scanMode, pageImgs, edition, tafsir, jump)
    }

    private val appearanceFlow = combine(
        tajweedEnabled,
        khatmahState,
        readingTheme,
        fontStyle,
        pageLineLayouts,
        ksuStudyFlow,
        optimisticPrefOverrides
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val study = values[5] as List<Any?>
        @Suppress("UNCHECKED_CAST")
        ReaderAppearanceState(
            tajweedEnabled = values[0] as Boolean,
            khatmahState = values[1] as KhatmahProgressState,
            readingTheme = values[2] as QuranReadingTheme,
            fontStyle = values[3] as QuranFontStyle,
            pageLineLayouts = values[4] as Map<Int, MushafPageLineLayout>,
            ksuScanImageMode = study[0] as Boolean,
            medinaPageImages = study[1] as Map<Int, MedinaPageImageState>,
            selectedTafsirEdition = study[2] as KsuTafsirEdition,
            tafsirState = study[3] as KsuTafsirState,
            requestedJumpAyah = study[4] as Pair<Int, Int>?,
            prefOverrides = values[6] as OptimisticPrefOverrides
        )
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
        container.quranAudioManager.state,
        appearanceFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val appearance = values[13] as ReaderAppearanceState
        val rawPrefs = values[0] as UserPreferences
        val prefs = rawPrefs.copy(
            quranMushafMode = true,
            quranSimpleScript = false,
            showTranslation = false
        )
        val effectiveTheme = appearance.readingTheme
        QuranUiState(
            prefs = prefs,
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
            audioState = values[12] as QuranAudioState,
            tajweedEnabled = appearance.tajweedEnabled,
            khatmahState = appearance.khatmahState,
            readingTheme = effectiveTheme,
            fontStyle = appearance.fontStyle,
            pageLineLayouts = appearance.pageLineLayouts,
            ksuScanImageMode = appearance.ksuScanImageMode,
            medinaPageImages = appearance.medinaPageImages,
            selectedTafsirEdition = appearance.selectedTafsirEdition,
            tafsirState = appearance.tafsirState,
            requestedJumpAyah = appearance.requestedJumpAyah
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = QuranUiState(
            tajweedEnabled = tajweedEnabled.value,
            khatmahState = khatmahState.value,
            readingTheme = readingTheme.value,
            fontStyle = fontStyle.value,
            ksuScanImageMode = ksuScanImageMode.value,
            selectedTafsirEdition = selectedTafsirEdition.value
        )
    )

    fun ensurePageLinesLoaded(pageNumber: Int) {
        // Digital text mode renders fully offline; page line layouts and scan images are
        // only fetched while scan-plate mode is active.
        if (!ksuScanImageMode.value) return
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        if (!pageLineLayouts.value.containsKey(clamped)) {
            viewModelScope.launch {
                val layout = container.quranComLineRepository.getOrFetchPageLines(clamped)
                if (layout != null) {
                    pageLineLayouts.update { it + (clamped to layout) }
                }
            }
        }
        ensureMedinaPageImageLoaded(clamped, forceRefresh = false)
    }

    fun ensureMedinaPageImageLoaded(pageNumber: Int, forceRefresh: Boolean = false) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        val current = medinaPageImages.value[clamped]
        if (!forceRefresh && (current is MedinaPageImageState.Ready || current is MedinaPageImageState.Loading)) {
            return
        }
        val localFile = container.quranFlashPageRepository.localPageFile(clamped)
        if (!forceRefresh && localFile.exists() && localFile.length() > 1024L) {
            medinaPageImages.update { it + (clamped to MedinaPageImageState.Ready(localFile)) }
            viewModelScope.launch {
                container.quranFlashPageRepository.prefetchSurroundingPages(clamped)
            }
            return
        }

        medinaPageImages.update { it + (clamped to MedinaPageImageState.Loading) }
        viewModelScope.launch {
            val result = container.quranFlashPageRepository.getOrFetchPageImage(clamped, forceRefresh)
            result.onSuccess { file ->
                medinaPageImages.update { it + (clamped to MedinaPageImageState.Ready(file)) }
                container.quranFlashPageRepository.prefetchSurroundingPages(clamped)
            }.onFailure { err ->
                medinaPageImages.update {
                    it + (clamped to MedinaPageImageState.Error(err.localizedMessage ?: "Offline"))
                }
            }
        }
    }

    fun openSurah(surahNumber: Int, ayahNumber: Int = 1) {
        val startPage = QuranMetadata.surahByNumber(surahNumber).startPage
        selectedMushafPage.value = null
        selectedSurah.value = surahNumber
        surahLoadError.value = null
        requestedJumpAyah.value = if (ayahNumber > 1) (surahNumber to ayahNumber) else null
        ensurePageLinesLoaded(startPage)
        viewModelScope.launch {
            container.preferencesDataStore.updateLastReadPage(startPage, surahNumber, ayahNumber)
            isLoadingSurah.value = true
            val res = container.quranRepository.fetchAndCacheSurahIfMissing(surahNumber)
            res.onSuccess { list ->
                val targetVerse = list.firstOrNull { it.ayahNumber == ayahNumber }
                val targetPage = targetVerse?.pageNumber ?: startPage
                ensurePageLinesLoaded(targetPage)
                container.preferencesDataStore.updateLastReadPage(targetPage, surahNumber, ayahNumber)
                if (targetVerse != null && ayahNumber > 1) {
                    loadTafsirForVerse(targetVerse)
                }
            }.onFailure { err ->
                surahLoadError.value = err.localizedMessage ?: "Unable to fetch Surah offline"
            }
            isLoadingSurah.value = false
        }
    }

    fun openMushafPage(pageNumber: Int, focusAyah: Pair<Int, Int>? = null) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        val primarySurah = QuranMetadata.surahsOnPage(clamped).firstOrNull()?.number ?: 1
        selectedSurah.value = null
        selectedMushafPage.value = clamped
        surahLoadError.value = null
        requestedJumpAyah.value = focusAyah
        ensurePageLinesLoaded(clamped)
        viewModelScope.launch {
            container.preferencesDataStore.updateLastReadPage(clamped, primarySurah, focusAyah?.second ?: 1)
            isLoadingSurah.value = true
            val res = container.quranRepository.fetchAndCachePageIfMissing(clamped)
            res.onFailure { err ->
                surahLoadError.value = err.localizedMessage ?: "Unable to fetch Mushaf Page offline"
            }
            val list = res.getOrNull().orEmpty()
            val targetVerse = focusAyah?.let { (s, a) ->
                list.firstOrNull { it.surahNumber == s && it.ayahNumber == a }
            } ?: list.firstOrNull()
            if (targetVerse != null) {
                container.preferencesDataStore.updateLastReadPage(clamped, targetVerse.surahNumber, targetVerse.ayahNumber)
                if (focusAyah != null) {
                    loadTafsirForVerse(targetVerse)
                }
            }
            isLoadingSurah.value = false
        }
    }

    fun onMushafPageViewed(pageNumber: Int) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        ensurePageLinesLoaded(clamped)
        val primarySurah = QuranMetadata.surahsOnPage(clamped).firstOrNull()?.number ?: 1
        viewModelScope.launch {
            container.preferencesDataStore.updateLastReadPage(clamped, primarySurah, 1)
        }
    }

    fun syncPagerPage(pageNumber: Int) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        onMushafPageViewed(clamped)
        if (selectedMushafPage.value != null && selectedMushafPage.value != clamped) {
            openMushafPage(clamped)
        }
    }

    fun jumpToSurahAyah(surahNumber: Int, ayahNumber: Int) {
        val meta = QuranMetadata.surahByNumber(surahNumber)
        val clampedAyah = ayahNumber.coerceIn(1, meta.ayahCount)
        openSurah(meta.number, clampedAyah)
    }

    fun clearJumpAyah() {
        requestedJumpAyah.value = null
    }

    fun closeSurahReader() {
        selectedSurah.value = null
        selectedMushafPage.value = null
        surahLoadError.value = null
        requestedJumpAyah.value = null
    }

    fun toggleKsuScanImageMode() {
        val next = !ksuScanImageMode.value
        ksuScanImageMode.value = next
        khatmahPrefs.edit().putBoolean("quranflash_medina1_pages_v3", next).apply()
    }

    fun setTafsirEdition(edition: KsuTafsirEdition, activeVerse: QuranVerseEntity?) {
        selectedTafsirEdition.value = edition
        khatmahPrefs.edit().putString("ksu_tafsir_edition", edition.name).apply()
        if (activeVerse != null) {
            loadTafsirForVerse(activeVerse, edition)
        }
    }

    fun loadTafsirForVerse(
        verse: QuranVerseEntity,
        edition: KsuTafsirEdition = selectedTafsirEdition.value
    ) {
        val key = "${verse.surahNumber}:${verse.ayahNumber}"
        tafsirFetchJob?.cancel()
        tafsirState.value = KsuTafsirState(
            verseKey = key,
            edition = edition,
            text = "",
            isLoading = true,
            error = null
        )
        tafsirFetchJob = viewModelScope.launch {
            val result = container.quranTafsirRepository.getTafsir(
                surahNumber = verse.surahNumber,
                ayahNumber = verse.ayahNumber,
                edition = edition,
                englishFallback = verse.englishTranslation
            )
            result.onSuccess { content ->
                tafsirState.value = KsuTafsirState(
                    verseKey = key,
                    edition = edition,
                    text = content,
                    isLoading = false,
                    error = null
                )
            }.onFailure { err ->
                tafsirState.value = KsuTafsirState(
                    verseKey = key,
                    edition = edition,
                    text = "",
                    isLoading = false,
                    error = err.localizedMessage ?: "Tafsir unavailable offline"
                )
            }
        }
    }

    /**
     * Tafsir entry point for the digital mushaf reader, where the tapped ayah
     * comes from the bundled KFGQPC text and there may be no cached
     * [QuranVerseEntity] for it. Fetches directly by surah/ayah coordinates.
     */
    fun loadTafsirForAyah(
        surahNumber: Int,
        ayahNumber: Int,
        edition: KsuTafsirEdition = selectedTafsirEdition.value
    ) {
        val key = "$surahNumber:$ayahNumber"
        tafsirFetchJob?.cancel()
        tafsirState.value = KsuTafsirState(
            verseKey = key,
            edition = edition,
            text = "",
            isLoading = true,
            error = null
        )
        tafsirFetchJob = viewModelScope.launch {
            val result = container.quranTafsirRepository.getTafsir(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                edition = edition
            )
            result.onSuccess { content ->
                tafsirState.value = KsuTafsirState(
                    verseKey = key,
                    edition = edition,
                    text = content,
                    isLoading = false,
                    error = null
                )
            }.onFailure { err ->
                tafsirState.value = KsuTafsirState(
                    verseKey = key,
                    edition = edition,
                    text = "",
                    isLoading = false,
                    error = err.localizedMessage ?: "Tafsir unavailable offline"
                )
            }
        }
    }

    fun clearTafsir() {
        tafsirFetchJob?.cancel()
        tafsirState.value = KsuTafsirState()
    }

    fun toggleTajweed() {
        val next = !tajweedEnabled.value
        tajweedEnabled.value = next
        khatmahPrefs.edit().putBoolean("tajweed_enabled", next).apply()
    }

    fun setReadingTheme(theme: QuranReadingTheme) {
        readingTheme.value = theme
        khatmahPrefs.edit().putString("reading_theme", theme.name).apply()
        viewModelScope.launch {
            container.preferencesDataStore.updateQuranNightMode(theme == QuranReadingTheme.DARK)
        }
    }

    fun setQuranFontStyle(style: QuranFontStyle) {
        fontStyle.value = style
        khatmahPrefs.edit().putString("quran_font_style", style.name).apply()
    }

    fun togglePageRead(pageNumber: Int) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        val current = khatmahState.value
        val updatedPages = if (current.readPages.contains(clamped)) {
            current.readPages - clamped
        } else {
            current.readPages + clamped
        }
        persistKhatmahState(current.copy(readPages = updatedPages))
    }

    fun markPageReadAndAdvance(pageNumber: Int) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        val current = khatmahState.value
        persistKhatmahState(current.copy(readPages = current.readPages + clamped))
        if (clamped < QuranMetadata.TOTAL_MUSHAF_PAGES) {
            openMushafPage(clamped + 1)
        }
    }

    fun setKhatmahTargetDays(days: Int) {
        val current = khatmahState.value
        persistKhatmahState(current.copy(targetDays = days.coerceIn(7, 365)))
    }

    fun resetKhatmahProgress() {
        val current = khatmahState.value
        persistKhatmahState(current.copy(readPages = emptySet()))
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

    fun togglePageBookmark(pageNumber: Int) {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        viewModelScope.launch {
            val exists = uiState.value.bookmarks.any {
                it.surahNumber == 0 && it.ayahNumber == clamped
            }
            container.quranRepository.setPageBookmark(clamped, !exists)
            if (!exists) {
                val primarySurah = QuranMetadata.surahsOnPage(clamped).firstOrNull()?.number ?: 1
                container.preferencesDataStore.updateLastReadPage(clamped, primarySurah, 1)
            }
        }
    }

    fun toggleBookmark(verse: QuranVerseEntity) {
        viewModelScope.launch {
            val exists = uiState.value.bookmarks.any {
                it.surahNumber == verse.surahNumber && it.ayahNumber == verse.ayahNumber
            }
            container.quranRepository.setBookmark(verse, !exists)
            container.preferencesDataStore.updateLastReadPage(verse.pageNumber, verse.surahNumber, verse.ayahNumber)
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            container.quranRepository.removeBookmarkEntry(bookmark)
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
        val nextDark = !uiState.value.prefs.quranNightMode
        val nextTheme = if (nextDark) QuranReadingTheme.DARK else QuranReadingTheme.KSU_MUSHAF
        readingTheme.value = nextTheme
        khatmahPrefs.edit().putString("reading_theme", nextTheme.name).apply()
        container.preferencesDataStore.updateQuranNightMode(nextDark)
    }

    fun toggleTranslation() {
        val next = !uiState.value.prefs.showTranslation
        optimisticPrefOverrides.update { it.copy(showTranslation = next) }
        viewModelScope.launch {
            container.preferencesDataStore.updateShowTranslation(next)
        }
    }

    fun toggleMushafMode() {
        val next = !uiState.value.prefs.quranMushafMode
        optimisticPrefOverrides.update { it.copy(quranMushafMode = next) }
        viewModelScope.launch {
            container.preferencesDataStore.updateQuranMushafMode(next)
        }
    }

    fun toggleSimpleScript() {
        val next = !uiState.value.prefs.quranSimpleScript
        optimisticPrefOverrides.update { it.copy(quranSimpleScript = next) }
        viewModelScope.launch {
            container.preferencesDataStore.updateQuranSimpleScript(next)
        }
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
