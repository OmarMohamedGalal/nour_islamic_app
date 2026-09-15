package com.example

import android.app.Application
import com.example.data.db.NoorDatabase
import com.example.data.repository.NoorRepository
import com.example.receiver.NotificationHelper

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
        com.example.widget.WidgetUpdater.updateAllWidgets(this)
        com.example.widget.WidgetUpdater.schedulePeriodicUpdates(this)
    }

    companion object {
        lateinit var instance: NoorApplication
            private set
    }
}
