package com.example.receiver

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
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ADHAN_ID = "noor_adhan_channel_v2"
    const val CHANNEL_REMINDERS_ID = "noor_reminders_channel"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Adhan Channel
            val adhanSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val adhanChannel = NotificationChannel(
                CHANNEL_ADHAN_ID,
                "Adhan & Prayer Times",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when it is time for each obligatory prayer."
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setSound(adhanSoundUri, audioAttributes)
            }

            // Reminders Channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Daily Reminders & Azkar",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for Morning, Evening Azkar and Duaa."
            }

            notificationManager.createNotificationChannel(adhanChannel)
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

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("noor://prayer")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Hayya 'ala as-Salah • حان وقت الصلاة"
        val content = "It is time for $prayerName ($prayerNameAr) in $cityName ($prayerTimeFormatted)"

        val notification = NotificationCompat.Builder(context, CHANNEL_ADHAN_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\n\nMay Allah accept your prayers and remembrance."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF131313.toInt())
            .build()

        notificationManager.notify(NOTIFICATION_ID_BASE + prayerName.hashCode(), notification)
    }
}
