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
    val playbackSpeed: Float = 1.0f,
    val errorMessage: String? = null
)

class AudioRecitationPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    fun playAyah(
        surahNumber: Int,
        verseNumber: Int,
        surahName: String,
        audioUrl: String,
        onAyahCompleted: (() -> Unit)? = null
    ) {
        stop()
        _playerState.value = AudioPlayerState(
            isPlaying = false,
            isLoading = true,
            surahNumber = surahNumber,
            verseNumber = verseNumber,
            surahName = surahName,
            playbackSpeed = _playerState.value.playbackSpeed
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            mp.playbackParams = mp.playbackParams.setSpeed(_playerState.value.playbackSpeed)
                        } catch (e: Exception) {
                            Log.e("AudioPlayer", "Could not set playback speed", e)
                        }
                    }
                    mp.start()
                    _playerState.value = _playerState.value.copy(
                        isPlaying = true,
                        isLoading = false
                    )
                }
                setOnCompletionListener {
                    _playerState.value = _playerState.value.copy(isPlaying = false)
                    onAyahCompleted?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "MediaPlayer error: what=$what extra=$extra")
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        errorMessage = "Recitation stream unavailable offline"
                    )
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error initializing MediaPlayer", e)
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isLoading = false,
                errorMessage = e.localizedMessage
            )
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playerState.value = _playerState.value.copy(isPlaying = false)
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping player", e)
        }
        mediaPlayer = null
        _playerState.value = AudioPlayerState(playbackSpeed = _playerState.value.playbackSpeed)
    }

    fun setSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let {
                try {
                    it.playbackParams = it.playbackParams.setSpeed(speed)
                } catch (e: Exception) {
                    Log.e("AudioPlayer", "Error changing speed", e)
                }
            }
        }
    }
}
