package com.example.audio

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object AudioCacheManager {
    private const val TAG = "AudioCacheManager"
    private const val CACHE_SUBDIR = "quran_audio"
    private const val MAX_CACHE_SIZE_BYTES = 200 * 1024 * 1024L // 200 MB

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_SUBDIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getFileNameForUrl(url: String): String {
        val cleanUrl = url.trim()
        val hash = (cleanUrl.hashCode().toLong() and 0xFFFFFFFFL).toString(16)
        val end = cleanUrl.substringAfterLast("/").take(24).replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return "${hash}_$end"
    }

    fun getCachedFile(context: Context, url: String): File? {
        val dir = getCacheDir(context)
        val file = File(dir, getFileNameForUrl(url))
        return if (file.exists() && file.length() > 2048) file else null
    }

    suspend fun downloadOrGet(
        context: Context,
        url: String,
        maxSizeBytes: Long = 25 * 1024 * 1024L // max 25MB download limit
    ): File? = withContext(Dispatchers.IO) {
        val cached = getCachedFile(context, url)
        if (cached != null) {
            Log.d(TAG, "Cache hit for $url -> ${cached.absolutePath}")
            return@withContext cached
        }

        val dir = getCacheDir(context)
        val targetName = getFileNameForUrl(url)
        val targetFile = File(dir, targetName)
        val tempFile = File(dir, "$targetName.tmp")

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:124.0)")
                .header("Accept", "*/*")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Download failed HTTP ${response.code} for $url")
                    return@withContext null
                }

                val body = response.body ?: return@withContext null
                val contentLength = body.contentLength()
                if (contentLength > maxSizeBytes) {
                    Log.d(TAG, "File exceeds max pre-download size ($contentLength > $maxSizeBytes), will stream directly: $url")
                    return@withContext null
                }

                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            if (tempFile.exists() && tempFile.length() > 2048) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
                Log.d(TAG, "Successfully downloaded & cached: ${targetFile.length()} bytes")
                trimCacheIfNeeded(context)
                targetFile
            } else {
                tempFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception downloading $url: ${e.message}")
            try { tempFile.delete() } catch (_: Exception) {}
            null
        }
    }

    private fun trimCacheIfNeeded(context: Context) {
        try {
            val dir = getCacheDir(context)
            val files = dir.listFiles() ?: return
            val totalSize = files.sumOf { it.length() }
            if (totalSize > MAX_CACHE_SIZE_BYTES) {
                // Delete oldest files until under 80% of max
                files.sortedBy { it.lastModified() }
                    .take(files.size / 2)
                    .forEach { it.delete() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error trimming cache: ${e.message}")
        }
    }
}
