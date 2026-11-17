package com.example.data.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Locale

data class QuranFlashDownloadProgress(
    val cachedPages: Int,
    val totalPages: Int = QuranMetadata.TOTAL_MUSHAF_PAGES,
    val currentPage: Int = 0,
    val isFinished: Boolean = false,
    val error: String? = null
)

class QuranFlashPageRepository(
    appContext: Context,
    private val okHttpClient: OkHttpClient
) {
    private val pagesDir = File(appContext.filesDir, "quranflash_medina1_v1").apply { mkdirs() }
    private val pageLocks = Array(QuranMetadata.TOTAL_MUSHAF_PAGES + 1) { Mutex() }

    fun localPageFile(pageNumber: Int): File {
        val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
        val padded = String.format(Locale.US, "%04d", clamped)
        return File(pagesDir, "medina1_$padded.img")
    }

    fun isPageCached(pageNumber: Int): Boolean {
        val f = localPageFile(pageNumber)
        return f.exists() && f.length() > 1024L
    }

    fun cachedPagesCount(): Int {
        return pagesDir.listFiles()?.count {
            it.isFile && it.name.startsWith("medina1_") && it.length() > 1024L
        } ?: 0
    }

    suspend fun getOrFetchPageImage(pageNumber: Int, forceRefresh: Boolean = false): Result<File> =
        withContext(Dispatchers.IO) {
            val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
            val target = localPageFile(clamped)

            pageLocks[clamped].withLock {
                if (!forceRefresh && target.exists() && target.length() > 1024L) {
                    return@withContext Result.success(target)
                }

                val candidates = medina1UrlCandidates(clamped)
                var lastError: Throwable? = null
                for (url in candidates) {
                    val result = downloadValidatedImage(url, target)
                    if (result.isSuccess) {
                        return@withContext result
                    }
                    lastError = result.exceptionOrNull()
                }
                Result.failure(lastError ?: IllegalStateException("Unable to load Mushaf page $clamped"))
            }
        }

    suspend fun prefetchSurroundingPages(centerPage: Int) = withContext(Dispatchers.IO) {
        val offsets = listOf(1, -1, 2)
        for (offset in offsets) {
            val targetPage = (centerPage + offset)
            if (targetPage in 1..QuranMetadata.TOTAL_MUSHAF_PAGES && !isPageCached(targetPage)) {
                runCatching { getOrFetchPageImage(targetPage) }
            }
        }
    }

    fun downloadAllMedina1Pages(): Flow<QuranFlashDownloadProgress> = flow {
        var cached = cachedPagesCount()
        for (page in 1..QuranMetadata.TOTAL_MUSHAF_PAGES) {
            if (isPageCached(page)) {
                continue
            }
            emit(
                QuranFlashDownloadProgress(
                    cachedPages = cached,
                    currentPage = page,
                    isFinished = false
                )
            )
            val res = getOrFetchPageImage(page)
            if (res.isFailure) {
                emit(
                    QuranFlashDownloadProgress(
                        cachedPages = cached,
                        currentPage = page,
                        isFinished = true,
                        error = res.exceptionOrNull()?.localizedMessage ?: "Network unavailable"
                    )
                )
                return@flow
            }
            cached++
            emit(
                QuranFlashDownloadProgress(
                    cachedPages = cached,
                    currentPage = page,
                    isFinished = false
                )
            )
        }
        emit(
            QuranFlashDownloadProgress(
                cachedPages = QuranMetadata.TOTAL_MUSHAF_PAGES,
                currentPage = QuranMetadata.TOTAL_MUSHAF_PAGES,
                isFinished = true
            )
        )
    }

    private fun downloadValidatedImage(url: String, destFile: File): Result<File> = runCatching {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0")
            .header("Referer", "https://app.quranflash.com/book/Medina1?ar")
            .header("Accept", "image/avif,image/webp,image/apng,image/gif,image/png,image/*,*/*;q=0.8")
            .build()

        okHttpClient.newCall(req).execute().use { response ->
            if (!response.isSuccessful) {
                error("HTTP ${response.code} for $url")
            }
            val body = response.body ?: error("Empty body for $url")
            val bytes = body.bytes()
            if (bytes.size < 1024 || !looksLikeValidImage(bytes)) {
                error("Invalid image payload (${bytes.size} bytes) from $url")
            }
            val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
            tempFile.writeBytes(bytes)
            if (destFile.exists()) destFile.delete()
            if (!tempFile.renameTo(destFile)) {
                tempFile.copyTo(destFile, overwrite = true)
                tempFile.delete()
            }
            destFile
        }
    }

    companion object {
        fun medina1UrlCandidates(pageNumber: Int): List<String> {
            val clamped = pageNumber.coerceIn(1, QuranMetadata.TOTAL_MUSHAF_PAGES)
            val padded4 = String.format(Locale.US, "%04d", clamped)
            return listOf(
                "https://app.quranflash.com/book/Medina1/epub/EPUB/imgs/$padded4.gif",
                "https://www.quranflash.com/books/Medina1/data/N/$padded4.gif",
                "https://quran.ksu.edu.sa/ayat/safahat2/$clamped.png"
            )
        }

        internal fun looksLikeValidImage(bytes: ByteArray): Boolean {
            if (bytes.size < 12) return false
            // GIF87a / GIF89a
            if (bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte()) {
                return true
            }
            // PNG
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) {
                return true
            }
            // JPEG
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
                return true
            }
            // WEBP (RIFF....WEBP)
            if (bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
                bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte()
            ) {
                return true
            }
            return false
        }
    }
}
