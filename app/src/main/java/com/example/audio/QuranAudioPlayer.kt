package com.example.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.model.Muezzin
import com.example.data.model.MuezzinData
import com.example.data.model.RadioStation
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.data.network.QuranApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurah: Surah? = null,
    val currentAyahNumber: Int? = null,
    val currentRadio: RadioStation? = null,
    val currentMuezzin: Muezzin? = null,
    val currentAudioTitle: String? = null,
    val currentReciter: Reciter = QuranApiService.availableReciters.first(),
    val selectedMuezzin: Muezzin = MuezzinData.availableMuezzins.first(),
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutesRemaining: Int = 0,
    val autoAdvanceAyah: Boolean = true,
    val isLooping: Boolean = false,
    val currentLoopCount: Int = 0,
    val targetLoopCount: Int = 1,
    val errorMessage: String? = null
)

class QuranAudioPlayer(private val context: Context) {

    companion object {
        private const val TAG = "QuranAudioPlayer"
    }

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private var currentOnComplete: (() -> Unit)? = null
    private var candidateUrls: List<String> = emptyList()
    private var currentCandidateIndex: Int = 0

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val okHttpDataSourceFactory by lazy {
        OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> {
                    _playbackState.value = _playbackState.value.copy(isLoading = true)
                }
                Player.STATE_READY -> {
                    val duration = if (player.duration > 0) player.duration.toInt() else 0
                    _playbackState.value = _playbackState.value.copy(
                        isLoading = false,
                        isPlaying = player.isPlaying,
                        durationMs = duration,
                        errorMessage = null
                    )
                    startProgressTracking()
                }
                Player.STATE_ENDED -> {
                    stopProgressTracking()
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        currentPositionMs = 0
                    )
                    currentOnComplete?.invoke()
                }
                Player.STATE_IDLE -> {
                    // Idle state
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = _playbackState.value.copy(
                isPlaying = isPlaying,
                isLoading = if (isPlaying) false else _playbackState.value.isLoading
            )
            if (isPlaying) {
                startProgressTracking()
            } else {
                stopProgressTracking()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "ExoPlayer error: ${error.errorCodeName} (code=${error.errorCode}) - ${error.message}")
            stopProgressTracking()
            tryPlayNextCandidate()
        }
    }

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context.applicationContext)
                    .setDataSourceFactory(okHttpDataSourceFactory)
            )
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(playerListener)
            }
    }

    fun setSelectedMuezzin(muezzin: Muezzin) {
        _playbackState.value = _playbackState.value.copy(selectedMuezzin = muezzin)
        try {
            val prefs = context.getSharedPreferences("islamic_prayer_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("selected_muezzin_id", muezzin.id).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving selected muezzin: ${e.message}")
        }
    }

    fun playSurah(surah: Surah, reciter: Reciter = _playbackState.value.currentReciter) {
        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentSurah = surah,
            currentAyahNumber = null,
            currentRadio = null,
            currentMuezzin = null,
            currentAudioTitle = "سورة ${surah.nameArabic} - ${reciter.nameArabic}",
            currentReciter = reciter,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        val candidates = QuranApiService.getSurahAudioUrlsWithFallbacks(reciter, surah.id)
        startPlayback(
            urls = candidates,
            onComplete = {
                stopProgressTracking()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPositionMs = 0
                )
            }
        )
    }

    fun playAyah(
        surah: Surah,
        ayahNumber: Int,
        reciter: Reciter = _playbackState.value.currentReciter,
        autoAdvance: Boolean = true
    ) {
        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentSurah = surah,
            currentAyahNumber = ayahNumber,
            autoAdvanceAyah = autoAdvance,
            currentRadio = null,
            currentMuezzin = null,
            currentAudioTitle = "سورة ${surah.nameArabic} (الآية $ayahNumber) - ${reciter.nameArabic}",
            currentReciter = reciter,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0
        )

        val candidates = QuranApiService.getAyahAudioUrlsWithFallbacks(reciter, surah.id, ayahNumber)
        startPlayback(
            urls = candidates,
            onComplete = {
                if (_playbackState.value.autoAdvanceAyah && ayahNumber < surah.versesCount) {
                    playAyah(surah, ayahNumber + 1, reciter, autoAdvance = true)
                } else {
                    stopProgressTracking()
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPositionMs = 0
                    )
                }
            }
        )

        // Background prefetch next Ayah so transition is instant
        if (ayahNumber < surah.versesCount) {
            scope.launch(Dispatchers.IO) {
                val nextUrls = QuranApiService.getAyahAudioUrlsWithFallbacks(reciter, surah.id, ayahNumber + 1)
                for (nextUrl in nextUrls) {
                    try {
                        AudioCacheManager.downloadOrGet(context, nextUrl)
                        break
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun startPlayback(urls: List<String>, onComplete: () -> Unit) {
        currentOnComplete = onComplete
        candidateUrls = urls
        currentCandidateIndex = 0
        playCurrentCandidate()
    }

    private fun playCurrentCandidate() {
        if (currentCandidateIndex >= candidateUrls.size) {
            Log.w(TAG, "All audio candidates failed.")
            _playbackState.value = _playbackState.value.copy(
                isLoading = false,
                isPlaying = false,
                errorMessage = "تعذر تشغيل الصوت. يرجى التحقق من اتصال الإنترنت أو اختيار قارئ آخر."
            )
            return
        }

        val url = candidateUrls[currentCandidateIndex]
        Log.d(TAG, "Playing candidate $currentCandidateIndex/${candidateUrls.size}: $url")

        scope.launch {
            try {
                val cachedFile = AudioCacheManager.getCachedFile(context, url)
                val mediaItem = if (cachedFile != null && cachedFile.exists() && cachedFile.length() > 2048) {
                    Log.d(TAG, "Playing from local cache: ${cachedFile.absolutePath}")
                    MediaItem.fromUri(Uri.fromFile(cachedFile))
                } else {
                    Log.d(TAG, "Streaming via ExoPlayer: $url")
                    MediaItem.fromUri(Uri.parse(url))
                }

                player.setMediaItem(mediaItem)
                player.playbackParameters = PlaybackParameters(_playbackState.value.playbackSpeed)
                player.prepare()
                player.play()
            } catch (e: Exception) {
                Log.e(TAG, "Failed starting candidate $url: ${e.message}")
                tryPlayNextCandidate()
            }
        }
    }

    private fun tryPlayNextCandidate() {
        currentCandidateIndex++
        playCurrentCandidate()
    }

    fun nextAyah() {
        val currentSurah = _playbackState.value.currentSurah ?: return
        val currentAyah = _playbackState.value.currentAyahNumber ?: return
        if (currentAyah < currentSurah.versesCount) {
            playAyah(currentSurah, currentAyah + 1, _playbackState.value.currentReciter)
        }
    }

    fun previousAyah() {
        val currentSurah = _playbackState.value.currentSurah ?: return
        val currentAyah = _playbackState.value.currentAyahNumber ?: return
        if (currentAyah > 1) {
            playAyah(currentSurah, currentAyah - 1, _playbackState.value.currentReciter)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        try {
            player.playbackParameters = PlaybackParameters(speed)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting speed: ${e.message}")
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = 0)
            return
        }
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = minutes)
        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60_000)
                remaining--
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = remaining)
            }
            stop()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = 0)
    }

    fun playRadio(radio: RadioStation) {
        if (_playbackState.value.currentRadio?.id == radio.id && _playbackState.value.isPlaying) {
            togglePlayPause()
            return
        }

        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentRadio = radio,
            currentSurah = null,
            currentMuezzin = null,
            currentAudioTitle = radio.name,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        val candidateUrls = listOf(radio.url)
        startPlayback(
            urls = candidateUrls,
            onComplete = {
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        )
    }

    fun playAdhan(muezzin: Muezzin = _playbackState.value.selectedMuezzin) {
        if (_playbackState.value.currentMuezzin?.id == muezzin.id && _playbackState.value.isPlaying) {
            stop()
            return
        }

        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentMuezzin = muezzin,
            selectedMuezzin = muezzin,
            currentSurah = null,
            currentRadio = null,
            currentAudioTitle = "أذان ${muezzin.name} (${muezzin.mosque})",
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        currentOnComplete = {
            stopProgressTracking()
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
        }

        if (muezzin.rawResId != null) {
            try {
                val rawUri = Uri.parse("android.resource://${context.packageName}/${muezzin.rawResId}")
                Log.d(TAG, "Playing Adhan from local raw resource: $rawUri")
                val mediaItem = MediaItem.fromUri(rawUri)
                player.setMediaItem(mediaItem)
                player.playbackParameters = PlaybackParameters(_playbackState.value.playbackSpeed)
                player.prepare()
                player.play()
                return
            } catch (e: Exception) {
                Log.e(TAG, "Failed playing raw resource adhan, falling back to remote url: ${e.message}")
            }
        }

        val candidateUrls = listOf(muezzin.audioUrl)
        startPlayback(
            urls = candidateUrls,
            onComplete = {
                stopProgressTracking()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        )
    }

    fun playAthkarPlaylist(title: String, audioUrl: String) {
        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentSurah = null,
            currentRadio = null,
            currentMuezzin = null,
            currentAudioTitle = title,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0,
            isLooping = false
        )

        val candidateUrls = listOf(audioUrl)
        startPlayback(
            urls = candidateUrls,
            onComplete = {
                stopProgressTracking()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        )
    }

    fun togglePlayPause() {
        if (player.currentMediaItem == null) {
            val state = _playbackState.value
            val surah = state.currentSurah
            if (surah != null) {
                if (state.currentAyahNumber != null) {
                    playAyah(surah, state.currentAyahNumber, state.currentReciter)
                } else {
                    playSurah(surah, state.currentReciter)
                }
                return
            }
            val radio = state.currentRadio
            if (radio != null) {
                playRadio(radio)
                return
            }
            val muezzin = state.currentMuezzin
            if (muezzin != null) {
                playAdhan(muezzin)
                return
            }
            return
        }

        try {
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_ENDED) {
                    player.seekTo(0)
                }
                player.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling play/pause: ${e.message}")
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            player.seekTo(positionMs.toLong())
            _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking: ${e.message}")
        }
    }

    fun setReciter(reciter: Reciter) {
        _playbackState.value = _playbackState.value.copy(currentReciter = reciter)
        val currentSurah = _playbackState.value.currentSurah
        if (currentSurah != null && _playbackState.value.isPlaying) {
            playSurah(currentSurah, reciter)
        }
    }

    fun stop() {
        stopProgressTracking()
        try {
            player.stop()
            player.clearMediaItems()
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isLoading = false,
            currentPositionMs = 0,
            currentAudioTitle = null
        )
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                try {
                    if (player.isPlaying) {
                        val pos = player.currentPosition.toInt().coerceAtLeast(0)
                        val dur = player.duration.toInt().coerceAtLeast(0)
                        _playbackState.value = _playbackState.value.copy(
                            currentPositionMs = pos,
                            durationMs = dur
                        )
                    }
                } catch (_: Exception) {}
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
        try {
            player.removeListener(playerListener)
            player.release()
        } catch (_: Exception) {}
    }
}
