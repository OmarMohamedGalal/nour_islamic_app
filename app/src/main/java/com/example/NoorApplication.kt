package com.example

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.data.db.NoorDatabase
import com.example.data.repository.NoorRepository
import com.example.receiver.NotificationHelper
import com.example.widget.WidgetUpdater

class NoorApplication : Application() {

    lateinit var database: NoorDatabase
        private set

    val repository: NoorRepository by lazy {
        NoorRepository(database.noorDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = NoorDatabase.getInstance(this)
        NotificationHelper.createNotificationChannels(this)
        WidgetUpdater.updateAllWidgets(this)
        WidgetUpdater.schedulePeriodicUpdates(this)

        // Listen to minute-by-minute system clock ticks, date flips, and screen unlocks
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                WidgetUpdater.updateAllWidgetsSync(context)
            }
        }, filter)
    }

    companion object {
        lateinit var instance: NoorApplication
            private set
    }
}
