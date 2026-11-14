package com.example.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import com.example.data.quran.QuranRecitersCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

enum class RepeatMode(val label: String, val maxPlays: Int) {
    ONCE("1x", 1),
    TWICE("2x", 2),
    THREE("3x", 3),
    FIVE("5x", 5),
    INFINITE("∞", Int.MAX_VALUE)
}

data class QuranAudioState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isFullSurahMode: Boolean = false,
    val isOfflinePlayback: Boolean = false,
    val reciterId: String = "ar.islamsobhi",
    val surahNumber: Int = 0,
    val ayahNumber: Int = 0,
    val globalAyahNumber: Int = 0,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.ONCE,
    val isShuffleEnabled: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val currentPlayCount: Int = 0,
    val downloadedKeys: Set<String> = emptySet(),
    val activeDownloads: Map<String, Float> = emptyMap(),
    val favoriteSurahs: Set<Int> = setOf(1, 2, 18, 36, 55, 56, 67),
    val errorMessage: String? = null
) {
    fun isSurahDownloaded(reciterId: String, surahNumber: Int): Boolean =
        downloadedKeys.contains("${reciterId}_${surahNumber}")

    fun downloadProgressFor(reciterId: String, surahNumber: Int): Float? =
        activeDownloads["${reciterId}_${surahNumber}"]
}

class QuranAudioManager(
    private val context: Context? = null,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(QuranAudioState())
    val state: StateFlow<QuranAudioState> = _state.asStateFlow()

    private var activePlaylist: List<Pair<Int, Int>> = emptyList() // (ayahNumber, globalNumber)
    private var activeReciterId: String = "ar.islamsobhi"

    init {
        refreshDownloadedFiles()
    }

    private fun audioRootDir(): File? {
        val ctx = context ?: return null
        return File(ctx.filesDir, "quran_audio").apply { mkdirs() }
    }

    fun offlineFileFor(reciterId: String, surahNumber: Int): File? {
        val root = audioRootDir() ?: return null
        val reciterDir = File(root, reciterId).apply { mkdirs() }
        return File(reciterDir, "surah_${surahNumber.coerceIn(1, 114)}.mp3")
    }

    fun refreshDownloadedFiles() {
        val root = audioRootDir() ?: return
        val keys = mutableSetOf<String>()
        root.listFiles()?.forEach { reciterDir ->
            if (reciterDir.isDirectory) {
                reciterDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.startsWith("surah_") && file.name.endsWith(".mp3") && file.length() > 1024L) {
                        val num = file.name.removePrefix("surah_").removeSuffix(".mp3").toIntOrNull()
                        if (num != null) {
                            keys.add("${reciterDir.name}_$num")
                        }
                    }
                }
            }
        }
        _state.update { it.copy(downloadedKeys = keys) }
    }

    fun toggleFavoriteSurah(surahNumber: Int) {
        _state.update { current ->
            val updated = current.favoriteSurahs.toMutableSet()
            if (!updated.add(surahNumber)) {
                updated.remove(surahNumber)
            }
            current.copy(favoriteSurahs = updated)
        }
    }

    fun toggleShuffle() {
        _state.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

    fun cycleRepeatMode() {
        val modes = RepeatMode.entries
        val next = modes[(modes.indexOf(_state.value.repeatMode) + 1) % modes.size]
        _state.update { it.copy(repeatMode = next) }
    }

    fun toggleMute() {
        val nextMuted = !_state.value.isMuted
        val vol = if (nextMuted) 0f else _state.value.volume
        runCatching { mediaPlayer?.setVolume(vol, vol) }
        _state.update { it.copy(isMuted = nextMuted) }
    }

    fun adjustVolume(delta: Float) {
        val nextVol = (_state.value.volume + delta).coerceIn(0.1f, 1.0f)
        runCatching { mediaPlayer?.setVolume(nextVol, nextVol) }
        _state.update { it.copy(volume = nextVol, isMuted = false) }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val duration = _state.value.durationMs.coerceAtLeast(1L)
        val clamped = positionMs.coerceIn(0L, duration)
        runCatching {
            player.seekTo(clamped.toInt())
            _state.update { it.copy(positionMs = clamped) }
        }
    }

    fun skipByMs(deltaMs: Long) {
        val target = (_state.value.positionMs + deltaMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun selectSurahForPlayer(surahNumber: Int, reciterId: String = activeReciterId) {
        val validSurah = surahNumber.coerceIn(1, 114)
        activeReciterId = reciterId
        _state.update {
            it.copy(
                surahNumber = validSurah,
                reciterId = reciterId,
                isFullSurahMode = true
            )
        }
    }

    fun playSurah(surahNumber: Int, reciterId: String = activeReciterId) {
        val validSurah = surahNumber.coerceIn(1, 114)
        activeReciterId = reciterId
        startFullSurahPlayback(validSurah, reciterId, playIteration = 1, allowFallback = true)
        notifyWidgetUpdate()
    }

    fun nextTrack() {
        val cur = _state.value
        if (cur.surahNumber <= 0) {
            playSurah(1, activeReciterId)
            return
        }
        val nextSurah = if (cur.isShuffleEnabled) {
            Random.nextInt(1, 115)
        } else {
            if (cur.surahNumber >= 114) 1 else cur.surahNumber + 1
        }
        playSurah(nextSurah, cur.reciterId.ifBlank { activeReciterId })
    }

    fun previousTrack() {
        val cur = _state.value
        if (cur.positionMs > 6000L) {
            seekTo(0L)
            return
        }
        val prevSurah = if (cur.surahNumber <= 1) 114 else cur.surahNumber - 1
        playSurah(prevSurah, cur.reciterId.ifBlank { activeReciterId })
    }

    fun playVerse(
        surahNumber: Int,
        ayahNumber: Int,
        globalNumber: Int,
        reciterId: String,
        surahVerses: List<Pair<Int, Int>>
    ) {
        activePlaylist = surahVerses
        activeReciterId = reciterId
        startVerseStream(surahNumber, ayahNumber, globalNumber, playIteration = 1)
    }

    fun togglePlayPause() {
        val cur = _state.value
        val player = mediaPlayer
        if (player == null) {
            if (cur.surahNumber > 0) {
                playSurah(cur.surahNumber, cur.reciterId.ifBlank { activeReciterId })
            } else {
                playSurah(1, activeReciterId)
            }
            return
        }
        runCatching {
            if (player.isPlaying) {
                player.pause()
                stopProgressPoller()
                _state.update { it.copy(isPlaying = false) }
            } else {
                player.start()
                startProgressPoller()
                _state.update { it.copy(isPlaying = true) }
            }
        }
        context?.let { QuranPlaybackService.sync(it) }
        notifyWidgetUpdate()
    }

    fun stop() {
        stopProgressPoller()
        runCatching {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        mediaPlayer = null
        _state.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                surahNumber = 0,
                ayahNumber = 0,
                globalAyahNumber = 0,
                positionMs = 0L,
                durationMs = 0L,
                currentPlayCount = 0,
                errorMessage = null
            )
        }
        context?.let { QuranPlaybackService.stop(it) }
        notifyWidgetUpdate()
    }

    private fun notifyWidgetUpdate() {
        val ctx = context ?: return
        scope.launch(Dispatchers.IO) {
            runCatching { com.example.widgets.WidgetUpdater.updateAll(ctx) }
        }
    }

    fun downloadSurahForOffline(surahNumber: Int, reciterId: String) {
        val key = "${reciterId}_${surahNumber}"
        if (_state.value.activeDownloads.containsKey(key)) return
        val destFile = offlineFileFor(reciterId, surahNumber) ?: return
        val reciter = QuranRecitersCatalog.reciterById(reciterId)

        _state.update {
            it.copy(
                activeDownloads = it.activeDownloads + (key to 0.02f),
                errorMessage = null
            )
        }

        scope.launch(Dispatchers.IO) {
            val tempFile = File(destFile.parentFile, "${destFile.name}.part")
            val primaryUrl = reciter.surahMp3Url(surahNumber)
            val fallbackUrl = reciter.fallbackSurahMp3Url(surahNumber)

            val success = downloadUrlToFile(primaryUrl, tempFile, key) ||
                (primaryUrl != fallbackUrl && downloadUrlToFile(fallbackUrl, tempFile, key))

            withContext(Dispatchers.Main) {
                if (success && tempFile.exists() && tempFile.length() > 1024L) {
                    if (destFile.exists()) destFile.delete()
                    tempFile.renameTo(destFile)
                    _state.update {
                        it.copy(
                            downloadedKeys = it.downloadedKeys + key,
                            activeDownloads = it.activeDownloads - key
                        )
                    }
                } else {
                    tempFile.delete()
                    _state.update {
                        it.copy(
                            activeDownloads = it.activeDownloads - key,
                            errorMessage = "Download failed for Surah $surahNumber"
                        )
                    }
                }
            }
        }
    }

    private fun downloadUrlToFile(url: String, target: File, progressKey: String): Boolean {
        return runCatching {
            val req = Request.Builder().url(url).build()
            okHttpClient.newCall(req).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body ?: return false
                val totalBytes = body.contentLength()
                body.byteStream().use { input ->
                    FileOutputStream(target).use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var downloaded = 0L
                        var read: Int
                        var lastEmit = 0L
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read
                            val now = System.currentTimeMillis()
                            if (totalBytes > 0 && now - lastEmit > 150L) {
                                lastEmit = now
                                val fraction = (downloaded.toFloat() / totalBytes).coerceIn(0.05f, 0.99f)
                                _state.update {
                                    it.copy(activeDownloads = it.activeDownloads + (progressKey to fraction))
                                }
                            }
                        }
                        output.flush()
                    }
                }
                true
            }
        }.getOrDefault(false)
    }

    fun deleteDownloadedSurah(surahNumber: Int, reciterId: String) {
        val file = offlineFileFor(reciterId, surahNumber)
        if (file?.exists() == true) {
            file.delete()
        }
        val key = "${reciterId}_${surahNumber}"
        _state.update { it.copy(downloadedKeys = it.downloadedKeys - key) }
    }

    private fun startFullSurahPlayback(
        surahNumber: Int,
        reciterId: String,
        playIteration: Int,
        allowFallback: Boolean
    ) {
        stopProgressPoller()
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null

        val reciter = QuranRecitersCatalog.reciterById(reciterId)
        val localFile = offlineFileFor(reciterId, surahNumber)
        val isLocalReady = localFile != null && localFile.exists() && localFile.length() > 1024L
        val dataSource = if (isLocalReady) {
            localFile!!.absolutePath
        } else if (allowFallback) {
            reciter.surahMp3Url(surahNumber)
        } else {
            reciter.fallbackSurahMp3Url(surahNumber)
        }

        _state.update {
            it.copy(
                isPlaying = false,
                isBuffering = true,
                isFullSurahMode = true,
                isOfflinePlayback = isLocalReady,
                reciterId = reciterId,
                surahNumber = surahNumber,
                ayahNumber = 0,
                globalAyahNumber = 0,
                positionMs = 0L,
                durationMs = 0L,
                currentPlayCount = playIteration,
                errorMessage = null
            )
        }
        context?.let { QuranPlaybackService.sync(it) }

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            // Keep CPU awake when screen is turned off during Quran recitation
            context?.let { ctx ->
                runCatching { setWakeMode(ctx, PowerManager.PARTIAL_WAKE_LOCK) }
            }
            val vol = if (_state.value.isMuted) 0f else _state.value.volume
            setVolume(vol, vol)

            setOnPreparedListener { mp ->
                val dur = runCatching { mp.duration.toLong().coerceAtLeast(0L) }.getOrDefault(0L)
                mp.start()
                _state.update {
                    it.copy(
                        isPlaying = true,
                        isBuffering = false,
                        durationMs = dur,
                        positionMs = 0L
                    )
                }
                startProgressPoller()
                context?.let { QuranPlaybackService.sync(it) }
            }

            setOnCompletionListener {
                val mode = _state.value.repeatMode
                if (playIteration < mode.maxPlays) {
                    startFullSurahPlayback(surahNumber, reciterId, playIteration + 1, allowFallback = true)
                } else {
                    nextTrack()
                }
            }

            setOnErrorListener { _, _, _ ->
                if (!isLocalReady && allowFallback) {
                    startFullSurahPlayback(surahNumber, reciterId, playIteration, allowFallback = false)
                } else {
                    stopProgressPoller()
                    _state.update {
                        it.copy(
                            isPlaying = false,
                            isBuffering = false,
                            errorMessage = "Audio unavailable offline — tap Download when online"
                        )
                    }
                    context?.let { QuranPlaybackService.sync(it) }
                }
                true
            }
        }

        mediaPlayer = player
        runCatching {
            player.setDataSource(dataSource)
            player.prepareAsync()
        }.onFailure { err ->
            _state.update {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = err.localizedMessage
                )
            }
        }
    }

    private fun startVerseStream(
        surahNumber: Int,
        ayahNumber: Int,
        globalNumber: Int,
        playIteration: Int
    ) {
        stopProgressPoller()
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null

        val reciter = QuranRecitersCatalog.reciterById(activeReciterId)
        val url = "https://cdn.islamic.network/quran/audio/128/${reciter.ayahEditionId}/$globalNumber.mp3"
        _state.update {
            it.copy(
                isPlaying = false,
                isBuffering = true,
                isFullSurahMode = false,
                isOfflinePlayback = false,
                reciterId = activeReciterId,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                globalAyahNumber = globalNumber,
                positionMs = 0L,
                durationMs = 0L,
                currentPlayCount = playIteration,
                errorMessage = null
            )
        }
        context?.let { QuranPlaybackService.sync(it) }

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            context?.let { ctx ->
                runCatching { setWakeMode(ctx, PowerManager.PARTIAL_WAKE_LOCK) }
            }
            val vol = if (_state.value.isMuted) 0f else _state.value.volume
            setVolume(vol, vol)

            setOnPreparedListener { mp ->
                val dur = runCatching { mp.duration.toLong().coerceAtLeast(0L) }.getOrDefault(0L)
                mp.start()
                _state.update {
                    it.copy(
                        isPlaying = true,
                        isBuffering = false,
                        durationMs = dur
                    )
                }
                startProgressPoller()
                context?.let { QuranPlaybackService.sync(it) }
            }
            setOnCompletionListener {
                val mode = _state.value.repeatMode
                if (playIteration < mode.maxPlays) {
                    startVerseStream(surahNumber, ayahNumber, globalNumber, playIteration + 1)
                } else {
                    val idx = activePlaylist.indexOfFirst { it.first == ayahNumber }
                    val nextPair = if (idx >= 0 && idx + 1 < activePlaylist.size) {
                        activePlaylist[idx + 1]
                    } else null

                    if (nextPair != null) {
                        startVerseStream(surahNumber, nextPair.first, nextPair.second, 1)
                    } else {
                        stop()
                    }
                }
            }
            setOnErrorListener { _, _, _ ->
                stopProgressPoller()
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "Audio unavailable offline"
                    )
                }
                true
            }
        }

        mediaPlayer = player
        runCatching {
            player.setDataSource(url)
            player.prepareAsync()
        }.onFailure { err ->
            _state.update {
                it.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = err.localizedMessage
                )
            }
        }
    }

    private fun startProgressPoller() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val mp = mediaPlayer
                if (mp != null && runCatching { mp.isPlaying }.getOrDefault(false)) {
                    val pos = runCatching { mp.currentPosition.toLong().coerceAtLeast(0L) }.getOrDefault(0L)
                    val dur = runCatching { mp.duration.toLong().coerceAtLeast(0L) }.getOrDefault(_state.value.durationMs)
                    _state.update { it.copy(positionMs = pos, durationMs = dur) }
                }
                delay(250L)
            }
        }
    }

    private fun stopProgressPoller() {
        progressJob?.cancel()
        progressJob = null
    }
}
