package com.example.widget

import android.app.Activity
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.example.MainActivity

/**
 * High-speed direct calendar launcher.
 * Resolves the device's default Calendar app intent directly so tapping the 3 capsule calendar widgets
 * opens the calendar app instantaneously with zero delay (no 3-second wait).
 */
object CalendarLauncher {

    /**
     * Resolves the best direct Intent for opening the calendar application.
     */
    fun getDirectCalendarIntent(context: Context): Intent {
        val nowMillis = System.currentTimeMillis()

        // 1. Direct Content URI for current time in Calendar (Standard Android / Xiaomi HyperOS / MIUI)
        try {
            val timeUri = ContentUris.appendId(
                CalendarContract.CONTENT_URI.buildUpon().appendPath("time"),
                nowMillis
            ).build()
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                data = timeUri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            if (viewIntent.resolveActivity(context.packageManager) != null) {
                return viewIntent
            }
        } catch (e: Exception) {
            Log.w("CalendarLauncher", "Content URI check failed", e)
        }

        // 2. Standard Android CATEGORY_APP_CALENDAR Intent
        try {
            val appCalendarIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALENDAR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            if (appCalendarIntent.resolveActivity(context.packageManager) != null) {
                return appCalendarIntent
            }
        } catch (e: Exception) {
            Log.w("CalendarLauncher", "CATEGORY_APP_CALENDAR check failed", e)
        }

        // 3. Known vendor calendar packages (Xiaomi / MIUI / HyperOS, Google Calendar, Samsung Calendar)
        val knownPackages = listOf(
            "com.android.calendar",         // Xiaomi HyperOS/MIUI default & AOSP Calendar
            "com.xiaomi.calendar",          // Xiaomi Calendar explicit package
            "com.google.android.calendar",  // Google Calendar
            "com.samsung.android.calendar"  // Samsung Calendar
        )
        for (pkg in knownPackages) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    return launchIntent
                }
            } catch (e: Exception) {
                Log.w("CalendarLauncher", "Failed checking package: $pkg", e)
            }
        }

        // 4. Fallback generic calendar URI
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://com.android.calendar/time/$nowMillis")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    /**
     * Creates a high-speed direct PendingIntent so tapping the widget immediately opens
     * the calendar app at native speed.
     */
    fun getLaunchPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val directIntent = getDirectCalendarIntent(context)
        return PendingIntent.getActivity(
            context,
            requestCode,
            directIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun openCalendarApp(activity: Activity) {
        val intent = getDirectCalendarIntent(activity)
        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity.startActivity(fallback)
        }
    }
}
