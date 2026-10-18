package com.example.widget

import android.app.Activity
import android.os.Bundle

/**
 * Transparent trampoline activity with zero-delay animation
 * for fallback calendar launching.
 */
class CalendarTrampolineActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        CalendarLauncher.openCalendarApp(this)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        finish()
    }
}
