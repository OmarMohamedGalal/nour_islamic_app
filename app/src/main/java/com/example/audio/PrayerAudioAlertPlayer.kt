package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.R
import java.util.Calendar

/**
 * Handles playing the authentic prayer voice alerts by Muhammad M. Gowaida (محمد م. جويدة)
 * "اقتربت صلاة..." for all prayers:
 * - Fajr: اقتربت صلاة الفجر
 * - Dhuhr: اقتربت صلاة الظهر
 * - Asr: اقتربت صلاة العصر
 * - Maghrib: اقتربت صلاة المغرب
 * - Isha: اقتربت صلاة العشاء
 * - Jumu'ah (Friday): اقتربت صلاة الجمعة
 */
object PrayerAudioAlertPlayer {

    private var activePlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun playCalmPrayerAlert(
        context: Context,
        prayerName: String,
        isFriday: Boolean = isTodayFriday(),
        onFinished: (() -> Unit)? = null
    ) {
        val soundResId = getRawAlertRes(prayerName, isFriday)
        playRawSound(context, soundResId, onFinished)
    }

    fun previewAlert(
        context: Context,
        prayerName: String = "Asr",
        isFriday: Boolean = false,
        onFinished: (() -> Unit)? = null
    ) {
        val soundResId = getRawAlertRes(prayerName, isFriday)
        playRawSound(context, soundResId, onFinished)
    }

    fun getRawAlertRes(prayerName: String, isFriday: Boolean = isTodayFriday()): Int {
        val norm = prayerName.trim().lowercase()
        return when {
            isFriday && (norm.contains("dhuhr") || norm.contains("ظهر") || norm.contains("جمعة") || norm.contains("jumu")) -> {
                R.raw.alert_jumuah
            }
            norm.contains("fajr") || norm.contains("فجر") -> R.raw.alert_fajr
            norm.contains("dhuhr") || norm.contains("zuhr") || norm.contains("ظهر") -> R.raw.alert_dhuhr
            norm.contains("asr") || norm.contains("عصر") -> R.raw.alert_asr
            norm.contains("maghrib") || norm.contains("مغرب") -> R.raw.alert_maghrib
            norm.contains("isha") || norm.contains("عشاء") -> R.raw.alert_isha
            norm.contains("jumu") || norm.contains("جمعة") -> R.raw.alert_jumuah
            else -> R.raw.alert_asr
        }
    }

    private fun playRawSound(context: Context, resId: Int, onFinished: (() -> Unit)? = null) {
        mainHandler.post {
            try {
                stop()

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val player = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    val afd = context.resources.openRawResourceFd(resId) ?: run {
                        Log.w("PrayerAudioAlert", "ResourceFd is null for $resId")
                        onFinished?.invoke()
                        return@apply
                    }
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    setVolume(1.0f, 1.0f)
                    prepare()
                }

                activePlayer = player

                player.setOnCompletionListener { mp ->
                    try {
                        mp.reset()
                        mp.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                    if (activePlayer == mp) {
                        activePlayer = null
                    }
                    onFinished?.invoke()
                }

                player.setOnErrorListener { mp, what, extra ->
                    Log.w("PrayerAudioAlert", "MediaPlayer error what=$what extra=$extra")
                    try {
                        mp.reset()
                        mp.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                    if (activePlayer == mp) {
                        activePlayer = null
                    }
                    onFinished?.invoke()
                    true
                }

                player.start()
            } catch (e: Exception) {
                Log.e("PrayerAudioAlert", "Failed to play prayer alert audio", e)
                onFinished?.invoke()
            }
        }
    }

    fun isTodayFriday(): Boolean {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    fun stop() {
        try {
            activePlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.w("PrayerAudioAlert", "Error stopping active player", e)
        } finally {
            activePlayer = null
        }
    }
}
