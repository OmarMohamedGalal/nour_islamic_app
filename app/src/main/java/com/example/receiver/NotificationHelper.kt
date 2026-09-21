package com.example.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.PrayerAudioAlertPlayer

object NotificationHelper {

    const val CHANNEL_ADHAN_CALM_ID = "noor_adhan_calm_v3"
    const val CHANNEL_ADHAN_DEFAULT_ID = "noor_adhan_default_v3"
    const val CHANNEL_REMINDERS_ID = "noor_reminders_channel"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 1. Calm Prayer Voice Alert Channel (Muhammad M. Gowaida)
            val calmChannel = NotificationChannel(
                CHANNEL_ADHAN_CALM_ID,
                "صوت محمد م. جويدة (اقتربت صلاة...)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تسجيل هادئ بصوت محمد م. جويدة يذكر باقتراب موعد الصلاة لجميع الصلوات وصلاة الجمعة."
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                // Channel sound is null because PrayerAudioAlertPlayer plays the authentic MP3 recording
                setSound(null, null)
            }

            // 2. Phone Default Sound Channel
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val defaultChannel = NotificationChannel(
                CHANNEL_ADHAN_DEFAULT_ID,
                "صوت الهاتف الافتراضي للصلوات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيه الصلاة باستخدام نغمة الإشعارات الافتراضية للجهاز."
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(defaultSoundUri, audioAttributes)
            }

            // 3. Reminders Channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Daily Reminders & Azkar • أذكار وأدعية",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكيرات الأذكار اليومية والصباح والمساء."
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(calmChannel)
            notificationManager.createNotificationChannel(defaultChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }

    fun showPrayerNotification(
        context: Context,
        prayerName: String,
        prayerNameAr: String,
        prayerTimeFormatted: String,
        cityName: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        // 1. Wake screen if phone is close/locked so notification appears prominently
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                "noor:prayer_alert_wakeup"
            )
            wakeLock?.acquire(10000L) // 10 seconds screen wake
        } catch (e: Exception) {
            Log.w("NotificationHelper", "Could not acquire wake lock", e)
        }

        // 2. Check Friday and prayer naming
        val isFriday = PrayerAudioAlertPlayer.isTodayFriday()
        val isDhuhrPrayer = prayerName.equals("Dhuhr", ignoreCase = true) ||
                prayerNameAr.contains("الظهر") ||
                prayerNameAr.contains("جمعة")

        val effectivePrayerName = if (isFriday && isDhuhrPrayer) "Jumu'ah" else prayerName
        val effectivePrayerNameAr = if (isFriday && isDhuhrPrayer) "الجمعة" else prayerNameAr

        // 3. Determine sound option
        val prefs = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
        val soundOptionId = prefs.getString("prayer_sound_option", "calm_prayer") ?: "calm_prayer"
        val isCalmVoice = soundOptionId == "calm_prayer" || soundOptionId == "athan"
        val channelId = if (isCalmVoice) CHANNEL_ADHAN_CALM_ID else CHANNEL_ADHAN_DEFAULT_ID

        // 4. Play Muhammad M. Gowaida prayer audio alert if voice option selected
        if (isCalmVoice) {
            PrayerAudioAlertPlayer.playCalmPrayerAlert(context, effectivePrayerName, isFriday)
        }

        // 5. Build high priority notification visible on lockscreen
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("noor://prayer")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            effectivePrayerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "اقتربت صلاة $effectivePrayerNameAr"
        val content = "حان موعد صلاة $effectivePrayerNameAr في $cityName ($prayerTimeFormatted)"
        val bigText = "اقتربت صلاة $effectivePrayerNameAr • $effectivePrayerName\n\n" +
                "حان وقت الصلاة والتقرب إلى الله في $cityName ($prayerTimeFormatted)\n" +
                "صوت التنبيه: محمد م. جويدة\n" +
                "حيّ على الصلاة، حيّ على الفلاح."

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText("تنبيه الصلاة")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Shows on lock screen when phone is locked
            .setFullScreenIntent(pendingIntent, true) // Heads-up when phone is locked/screen off
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF131313.toInt())
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID_BASE + prayerName.hashCode(), notification)
    }
}
