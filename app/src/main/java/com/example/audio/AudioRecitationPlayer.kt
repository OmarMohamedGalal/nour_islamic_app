package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val surahNumber: Int = 0,
    val verseNumber: Int = 0,
    val isFullSurah: Boolean = true,
    val surahName: String = "",
    val surahNameEn: String = "",
    val reciterName: String = "",
    val playbackSpeed: Float = 1.0f,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val errorMessage: String? = null
)

class AudioRecitationPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPreparing: Boolean = false
    private var isPrepared: Boolean = false
    private var activeSessionId: Long = 0L

    private val playerScope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private fun safelyTeardownPlayer(player: MediaPlayer?) {
        if (player == null) return
        try {
            player.setOnPreparedListener(null)
            player.setOnCompletionListener(null)
            player.setOnErrorListener(null)
        } catch (e: Exception) {
            // ignore
        }
        try {
            if (player.isPlaying) {
                player.stop()
            }
        } catch (e: Exception) {
            // ignore native stop state warnings
        }
        try {
            player.reset()
        } catch (e: Exception) {
            // ignore
        }
        try {
            player.release()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = playerScope.launch {
            while (isActive) {
                if (isPrepared && mediaPlayer?.isPlaying == true) {
                    try {
                        val pos = mediaPlayer?.currentPosition ?: 0
                        val dur = mediaPlayer?.duration ?: 0
                        _playerState.value = _playerState.value.copy(
                            currentPositionMs = pos,
                            durationMs = if (dur > 0) dur else _playerState.value.durationMs
                        )
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                delay(500)
            }
        }
    }

    fun playFullSurah(
        surahNumber: Int,
        surahName: String,
        surahNameEn: String = "",
        audioUrl: String,
        reciterName: String = "",
        onCompleted: (() -> Unit)? = null
    ) {
        val currentSpeed = _playerState.value.playbackSpeed
        val sessionId = ++activeSessionId
        isPreparing = true
        isPrepared = false

        progressJob?.cancel()
        val oldPlayer = mediaPlayer
        mediaPlayer = null
        safelyTeardownPlayer(oldPlayer)

        _playerState.value = AudioPlayerState(
            isPlaying = false,
            isLoading = true,
            surahNumber = surahNumber,
            verseNumber = 0,
            isFullSurah = true,
            surahName = surahName,
            surahNameEn = surahNameEn,
            reciterName = reciterName,
            playbackSpeed = currentSpeed,
            currentPositionMs = 0,
            durationMs = 0
        )

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    if (sessionId != activeSessionId) {
                        safelyTeardownPlayer(mp)
                        return@setOnPreparedListener
                    }
                    isPreparing = false
                    isPrepared = true
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                mp.playbackParams = mp.playbackParams.setSpeed(_playerState.value.playbackSpeed)
                            } catch (e: Exception) {
                                Log.w("AudioPlayer", "Could not set playback speed", e)
                            }
                        }
                        mp.start()
                        _playerState.value = _playerState.value.copy(
                            isPlaying = true,
                            isLoading = false,
                            durationMs = mp.duration
                        )
                        startProgressUpdates()
                    } catch (e: Exception) {
                        Log.e("AudioPlayer", "Error starting playback in onPrepared", e)
                        _playerState.value = _playerState.value.copy(
                            isPlaying = false,
                            isLoading = false,
                            errorMessage = "تعذر تشغيل التلاوة"
                        )
                    }
                }
                setOnCompletionListener {
                    if (sessionId == activeSessionId) {
                        _playerState.value = _playerState.value.copy(isPlaying = false, currentPositionMs = 0)
                        onCompleted?.invoke()
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    Log.w("AudioPlayer", "MediaPlayer onError: what=$what extra=$extra")
                    if (sessionId == activeSessionId) {
                        isPreparing = false
                        isPrepared = false
                        _playerState.value = _playerState.value.copy(
                            isPlaying = false,
                            isLoading = false,
                            errorMessage = "تلاوة السورة غير متوفرة حالياً"
                        )
                    }
                    safelyTeardownPlayer(mp)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error initializing MediaPlayer", e)
            isPreparing = false
            isPrepared = false
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isLoading = false,
                errorMessage = e.localizedMessage ?: "Playback initialization failed"
            )
        }
    }

    fun playAyah(
        surahNumber: Int,
        verseNumber: Int,
        surahName: String,
        audioUrl: String,
        reciterName: String = "",
        onAyahCompleted: (() -> Unit)? = null
    ) {
        val currentSpeed = _playerState.value.playbackSpeed
        val sessionId = ++activeSessionId
        isPreparing = true
        isPrepared = false

        progressJob?.cancel()
        val oldPlayer = mediaPlayer
        mediaPlayer = null
        safelyTeardownPlayer(oldPlayer)

        _playerState.value = AudioPlayerState(
            isPlaying = false,
            isLoading = true,
            surahNumber = surahNumber,
            verseNumber = verseNumber,
            isFullSurah = false,
            surahName = surahName,
            reciterName = reciterName,
            playbackSpeed = currentSpeed
        )

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    if (sessionId != activeSessionId) {
                        safelyTeardownPlayer(mp)
                        return@setOnPreparedListener
                    }
                    isPreparing = false
                    isPrepared = true
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                mp.playbackParams = mp.playbackParams.setSpeed(_playerState.value.playbackSpeed)
                            } catch (e: Exception) {
                                Log.w("AudioPlayer", "Could not set playback speed", e)
                            }
                        }
                        mp.start()
                        _playerState.value = _playerState.value.copy(
                            isPlaying = true,
                            isLoading = false,
                            durationMs = mp.duration
                        )
                        startProgressUpdates()
                    } catch (e: Exception) {
                        Log.e("AudioPlayer", "Error starting playback in onPrepared", e)
                        _playerState.value = _playerState.value.copy(
                            isPlaying = false,
                            isLoading = false,
                            errorMessage = "Audio playback failed"
                        )
                    }
                }
                setOnCompletionListener {
                    if (sessionId == activeSessionId) {
                        _playerState.value = _playerState.value.copy(isPlaying = false)
                        onAyahCompleted?.invoke()
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    Log.w("AudioPlayer", "MediaPlayer onError: what=$what extra=$extra")
                    if (sessionId == activeSessionId) {
                        isPreparing = false
                        isPrepared = false
                        _playerState.value = _playerState.value.copy(
                            isPlaying = false,
                            isLoading = false,
                            errorMessage = "Recitation stream unavailable offline"
                        )
                    }
                    safelyTeardownPlayer(mp)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error initializing MediaPlayer", e)
            isPreparing = false
            isPrepared = false
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isLoading = false,
                errorMessage = e.localizedMessage ?: "Playback initialization failed"
            )
        }
    }

    fun pause() {
        if (isPreparing || !isPrepared) return
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    _playerState.value = _playerState.value.copy(isPlaying = false)
                }
            } catch (e: Exception) {
                Log.w("AudioPlayer", "Error pausing player", e)
            }
        }
    }

    fun resume() {
        if (isPreparing || !isPrepared) return
        mediaPlayer?.let { mp ->
            try {
                if (!mp.isPlaying) {
                    mp.start()
                    _playerState.value = _playerState.value.copy(isPlaying = true)
                    startProgressUpdates()
                }
            } catch (e: Exception) {
                Log.w("AudioPlayer", "Error resuming player", e)
            }
        }
    }

    fun seekTo(positionMs: Int) {
        if (!isPrepared) return
        try {
            mediaPlayer?.seekTo(positionMs)
            _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.w("AudioPlayer", "Error seeking", e)
        }
    }

    fun rewind(seconds: Int = 10) {
        if (!isPrepared) return
        try {
            val cur = mediaPlayer?.currentPosition ?: 0
            val target = (cur - seconds * 1000).coerceAtLeast(0)
            mediaPlayer?.seekTo(target)
            _playerState.value = _playerState.value.copy(currentPositionMs = target)
        } catch (e: Exception) {
            Log.w("AudioPlayer", "Error rewinding", e)
        }
    }

    fun forward(seconds: Int = 10) {
        if (!isPrepared) return
        try {
            val cur = mediaPlayer?.currentPosition ?: 0
            val duration = mediaPlayer?.duration ?: 0
            val target = (cur + seconds * 1000).coerceAtMost(duration)
            mediaPlayer?.seekTo(target)
            _playerState.value = _playerState.value.copy(currentPositionMs = target)
        } catch (e: Exception) {
            Log.w("AudioPlayer", "Error forwarding", e)
        }
    }

    fun stop() {
        activeSessionId++
        isPreparing = false
        isPrepared = false
        progressJob?.cancel()
        progressJob = null
        val oldPlayer = mediaPlayer
        mediaPlayer = null
        safelyTeardownPlayer(oldPlayer)
        _playerState.value = AudioPlayerState(playbackSpeed = _playerState.value.playbackSpeed)
    }

    fun setSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPrepared) {
            mediaPlayer?.let {
                try {
                    it.playbackParams = it.playbackParams.setSpeed(speed)
                } catch (e: Exception) {
                    Log.w("AudioPlayer", "Error changing speed", e)
                }
            }
        }
    }

    fun notifyReciterUnavailable(reciterName: String = "") {
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            isLoading = false,
            reciterName = reciterName,
            errorMessage = "القارئ غير متوفر فى تلك السورة"
        )
    }

    fun clearError() {
        _playerState.value = _playerState.value.copy(errorMessage = null)
    }
}
