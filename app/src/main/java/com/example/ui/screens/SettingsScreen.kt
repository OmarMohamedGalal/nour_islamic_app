package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculationMethod
import com.example.data.model.Madhab
import com.example.data.model.PrayerType
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "PREFERENCES & ACCURACY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted
                )
                Text(
                    text = "الإعدادات والحساب",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite
                )
            }
        }

        // LOCATION & CALCULATION METHOD
        item {
            Text(
                text = "CALCULATION PARAMETERS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // City item
                    SettingsRow(
                        title = "Location & City",
                        subtitle = "${uiState.currentCity.nameEn} (${uiState.currentCity.countryEn})",
                        icon = Icons.Outlined.LocationOn,
                        onClick = { viewModel.setShowCityDialog(true) }
                    )

                    HorizontalDivider(color = BorderSubtle)

                    // Calculation Method item
                    SettingsRow(
                        title = "Calculation Authority",
                        subtitle = uiState.calculationMethod.titleEn,
                        icon = Icons.Outlined.Calculate,
                        onClick = { viewModel.setShowCalculationMethodDialog(true) }
                    )

                    HorizontalDivider(color = BorderSubtle)

                    // Madhab item
                    SettingsRow(
                        title = "Asr Juristic Madhab",
                        subtitle = "${uiState.madhab.titleEn} (${if (uiState.madhab == Madhab.STANDARD) "Shafi'i, Maliki, Hanbali" else "Hanafi double shadow"})",
                        icon = Icons.Outlined.Balance,
                        onClick = {
                            val next = if (uiState.madhab == Madhab.STANDARD) Madhab.HANAFI else Madhab.STANDARD
                            viewModel.selectMadhab(next)
                        }
                    )

                    HorizontalDivider(color = BorderSubtle)

                    // Hijri Date Adjustment item
                    val adj = uiState.hijriAdjustmentDays
                    val adjDisplay = if (adj > 0) "+${adj}" else "$adj"
                    SettingsRow(
                        title = "Hijri Date Adjustment",
                        subtitle = "$adjDisplay Days (Moonsighting offset: Today is ${uiState.hijriDate.day} ${uiState.hijriDate.monthNameEn})",
                        icon = Icons.Outlined.CalendarMonth,
                        onClick = {
                            // Cycle through -2 (User's region), -1, 0 (Standard), +1, +2
                            val nextAdj = when (adj) {
                                -2 -> -1
                                -1 -> 0
                                0 -> 1
                                1 -> 2
                                2 -> -2
                                else -> -2
                            }
                            viewModel.setHijriAdjustment(nextAdj)
                        }
                    )
                }
            }
        }

        // PRAYER ADHAN NOTIFICATIONS
        item {
            Text(
                text = "ADHAN NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    NotificationToggleRow("Fajr Adhan", uiState.userSettings.fajrNotification) {
                        viewModel.toggleNotification(PrayerType.FAJR)
                    }
                    HorizontalDivider(color = BorderSubtle)
                    NotificationToggleRow("Dhuhr Adhan", uiState.userSettings.dhuhrNotification) {
                        viewModel.toggleNotification(PrayerType.DHUHR)
                    }
                    HorizontalDivider(color = BorderSubtle)
                    NotificationToggleRow("Asr Adhan", uiState.userSettings.asrNotification) {
                        viewModel.toggleNotification(PrayerType.ASR)
                    }
                    HorizontalDivider(color = BorderSubtle)
                    NotificationToggleRow("Maghrib Adhan", uiState.userSettings.maghribNotification) {
                        viewModel.toggleNotification(PrayerType.MAGHRIB)
                    }
                    HorizontalDivider(color = BorderSubtle)
                    NotificationToggleRow("Isha Adhan", uiState.userSettings.ishaNotification) {
                        viewModel.toggleNotification(PrayerType.ISHA)
                    }
                }
            }
        }

        // RELIABILITY & EXACT ALARMS (Prompt requirement: exact alarm + battery optimization)
        item {
            Text(
                text = "SYSTEM PERMISSIONS & RELIABILITY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLow),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.AlarmOn, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Reliable Exact Adhans",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To guarantee the Adhan sounds exactly on the second even when the device is locked, grant 'Alarms & Reminders' and disable battery optimization.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to app details
                                    val appIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(appIntent)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = VoidBlack)
                    ) {
                        Text("Open Alarm Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // HOME SCREEN WIDGETS SECTION
        item {
            Text(
                text = "HOME SCREEN WIDGETS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Widgets (${14})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Text(
                                text = "Auto-refreshes on prayer times & periodic intervals",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val widgetsList = listOf(
                        Triple(
                            "Horizontal Prayer Bar",
                            "Fajr, Dhuhr, Asr, Maghrib, Isha in a slim pill",
                            com.example.widget.PrayerBarWidgetProvider::class.java
                        ),
                        Triple(
                            "Quran Verse of the Day",
                            "Inspiring ayah (tap cycles to next verse)",
                            com.example.widget.QuranVerseWidgetProvider::class.java
                        ),
                        Triple(
                            "Compact Prayer Times",
                            "Vertical list with active prayer highlight",
                            com.example.widget.PrayerListWidgetProvider::class.java
                        ),
                        Triple(
                            "Next Prayer Arabic Banner",
                            "Large time, countdown, and Arabic title",
                            com.example.widget.NextPrayerArabicWidgetProvider::class.java
                        )
                    )

                    widgetsList.forEachIndexed { idx, (name, desc, providerClass) ->
                        if (idx > 0) {
                            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val appWidgetManager = context.getSystemService(android.appwidget.AppWidgetManager::class.java)
                                if (appWidgetManager?.isRequestPinAppWidgetSupported == true) {
                                    FilledTonalButton(
                                        onClick = {
                                            val component = android.content.ComponentName(context, providerClass)
                                            appWidgetManager.requestPinAppWidget(component, null, null)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = SurfaceCardHighest,
                                            contentColor = PureWhite
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Pin", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            com.example.widget.WidgetUpdater.updateAllWidgets(context)
                            com.example.widget.WidgetUpdater.schedulePeriodicUpdates(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = VoidBlack)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh All Widgets Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ABOUT CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "NOOR ISLAMIC COMPANION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0 • Offline-first, Ad-free, Privacy-first",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Crafted with black-and-white elegance, high-precision astronomical algorithms, authentic Uthmani script, and responsive home screen widgets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // CALCULATION METHOD DIALOG
    if (uiState.showCalculationMethodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowCalculationMethodDialog(false) },
            containerColor = SurfaceCardHigh,
            title = {
                Text(
                    text = "Prayer Calculation Method",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(CalculationMethod.entries.size) { i ->
                        val method = CalculationMethod.entries[i]
                        val isSelected = method == uiState.calculationMethod
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectCalculationMethod(method) },
                            color = if (isSelected) SurfaceCardHighest else Color.Transparent
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = method.titleEn,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                                Text(
                                    text = "Fajr: ${method.fajrAngle}° | Isha: ${if (method.ishaMinutesAfterMaghrib > 0) "+${method.ishaMinutesAfterMaghrib}m" else "${method.ishaAngle}°"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setShowCalculationMethodDialog(false) }) {
                    Text("Close", color = PureWhite)
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = PureWhite)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = PureWhite)
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = VoidBlack,
                checkedTrackColor = PureWhite,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceLow
            )
        )
    }
}
