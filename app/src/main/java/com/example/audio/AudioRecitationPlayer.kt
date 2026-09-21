package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val surahNumber: Int = 0,
    val verseNumber: Int = 0,
    val surahName: String = "",
    val reciterName: String = "",
    val playbackSpeed: Float = 1.0f,
    val errorMessage: String? = null
)

class AudioRecitationPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPreparing: Boolean = false
    private var isPrepared: Boolean = false
    private var activeSessionId: Long = 0L

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

        // Cleanly release existing player
        val oldPlayer = mediaPlayer
        mediaPlayer = null
        safelyTeardownPlayer(oldPlayer)

        _playerState.value = AudioPlayerState(
            isPlaying = false,
            isLoading = true,
            surahNumber = surahNumber,
            verseNumber = verseNumber,
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
                            isLoading = false
                        )
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
                }
            } catch (e: Exception) {
                Log.w("AudioPlayer", "Error resuming player", e)
            }
        }
    }

    fun stop() {
        activeSessionId++
        isPreparing = false
        isPrepared = false
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
