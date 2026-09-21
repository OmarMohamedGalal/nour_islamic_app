package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CalculationMethod
import com.example.data.model.Madhab
import com.example.data.model.NotificationSoundOption
import com.example.data.model.PrayerType
import com.example.data.model.QuranReciter
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.components.ReciterSelectionDialog
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
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkBase)
                    .background(GlassGradientCard)
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // City item
                    SettingsRow(
                        title = "Location & City",
                        subtitle = "${uiState.currentCity.nameEn} (${uiState.currentCity.countryEn})",
                        icon = Icons.Outlined.LocationOn,
                        onClick = { viewModel.setShowCityDialog(true) }
                    )

                    HorizontalDivider(color = GlassBorderColorSubtle)

                    // Calculation Method item
                    SettingsRow(
                        title = "Calculation Authority",
                        subtitle = uiState.calculationMethod.titleEn,
                        icon = Icons.Outlined.Calculate,
                        onClick = { viewModel.setShowCalculationMethodDialog(true) }
                    )

                    HorizontalDivider(color = GlassBorderColorSubtle)

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

                    HorizontalDivider(color = GlassBorderColorSubtle)

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

        // QURAN RECITATION & RECITER SELECTION
        item {
            Text(
                text = "QURAN RECITATION • تلاوة القرآن",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkBase)
                    .background(GlassGradientCard)
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsRow(
                        title = "Quran Reciter • القارئ المفضل",
                        subtitle = "${uiState.selectedReciter.nameAr} (${uiState.selectedReciter.nameEn})",
                        icon = Icons.Filled.RecordVoiceOver,
                        onClick = { viewModel.setShowReciterDialog(true) }
                    )
                    HorizontalDivider(color = GlassBorderColorSubtle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Headphones,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "القراء المتاحون: المنشاوي، ماهر المعيقلي، عبد الرحمن مسعد، إسلام صبحي، العفاسي",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // PRAYER ADHAN NOTIFICATIONS & SOUND
        item {
            Text(
                text = "PRAYER NOTIFICATION SOUND • صوت التنبيه",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkBase)
                    .background(GlassGradientCard)
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Option 1: Muhammad M. Gowaida voice alert
                    SoundOptionRow(
                        title = NotificationSoundOption.CALM_PRAYER.titleAr,
                        subtitle = NotificationSoundOption.CALM_PRAYER.descriptionAr,
                        isSelected = uiState.selectedPrayerSoundOption == NotificationSoundOption.CALM_PRAYER,
                        onSelect = {
                            viewModel.setPrayerNotificationSound(NotificationSoundOption.CALM_PRAYER)
                        },
                        onPreview = {
                            viewModel.previewPrayerSound(NotificationSoundOption.CALM_PRAYER, "Asr")
                        }
                    )

                    // If Muhammad Gowaida option is selected, show instant preview chips for all 6 prayers
                    if (uiState.selectedPrayerSoundOption == NotificationSoundOption.CALM_PRAYER) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0DFFFFFF))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "استمع لتسجيلات محمد م. جويدة للصلوات:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val prayers = listOf(
                                    Triple("الفجر", "Fajr", false),
                                    Triple("الظهر", "Dhuhr", false),
                                    Triple("العصر", "Asr", false),
                                    Triple("المغرب", "Maghrib", false),
                                    Triple("العشاء", "Isha", false),
                                    Triple("الجمعة", "Dhuhr", true)
                                )
                                prayers.forEach { (label, name, isFriday) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x22FFFFFF))
                                            .clickable {
                                                viewModel.previewPrayerSound(
                                                    NotificationSoundOption.CALM_PRAYER,
                                                    prayerName = name,
                                                    isFriday = isFriday
                                                )
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = PureWhite
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = GlassBorderColorSubtle)

                    // Option 2: Phone default sound
                    SoundOptionRow(
                        title = NotificationSoundOption.PHONE_DEFAULT.titleAr,
                        subtitle = NotificationSoundOption.PHONE_DEFAULT.descriptionAr,
                        isSelected = uiState.selectedPrayerSoundOption == NotificationSoundOption.PHONE_DEFAULT,
                        onSelect = {
                            viewModel.setPrayerNotificationSound(NotificationSoundOption.PHONE_DEFAULT)
                        },
                        onPreview = {
                            viewModel.previewPrayerSound(NotificationSoundOption.PHONE_DEFAULT)
                        }
                    )
                }
            }
        }

        // LOCKSCREEN & SCREEN WAKE ENHANCEMENT
        item {
            Text(
                text = "LOCKSCREEN & DISPLAY WAKE • الظهور والشاشة مغلقة",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkBase)
                    .background(GlassGradientCard)
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1AFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ScreenLockPortrait,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ظهور التنبيه عند قفل الشاشة",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "إضاءة الشاشة تلقائياً وعرض إشعار الصلاة بأولوية قصوى حتى عند إغلاق أو قفل الهاتف.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.testPrayerNotification()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x22FFFFFF),
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تجربة إشعار الصلاة وقفل الشاشة الآن",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // PRAYER ADHAN TOGGLES
        item {
            Text(
                text = "PRAYER ALERTS • تفعيل تنبيهات الصلوات",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkBase)
                    .background(GlassGradientCard)
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    NotificationToggleRow("Fajr Adhan", uiState.userSettings.fajrNotification) {
                        viewModel.toggleNotification(PrayerType.FAJR)
                    }
                    HorizontalDivider(color = GlassBorderColorSubtle)
                    NotificationToggleRow("Dhuhr Adhan", uiState.userSettings.dhuhrNotification) {
                        viewModel.toggleNotification(PrayerType.DHUHR)
                    }
                    HorizontalDivider(color = GlassBorderColorSubtle)
                    NotificationToggleRow("Asr Adhan", uiState.userSettings.asrNotification) {
                        viewModel.toggleNotification(PrayerType.ASR)
                    }
                    HorizontalDivider(color = GlassBorderColorSubtle)
                    NotificationToggleRow("Maghrib Adhan", uiState.userSettings.maghribNotification) {
                        viewModel.toggleNotification(PrayerType.MAGHRIB)
                    }
                    HorizontalDivider(color = GlassBorderColorSubtle)
                    NotificationToggleRow("Isha Adhan", uiState.userSettings.ishaNotification) {
                        viewModel.toggleNotification(PrayerType.ISHA)
                    }
                }
            }
        }

        // RELIABILITY & EXACT ALARMS
        item {
            Text(
                text = "SYSTEM PERMISSIONS & RELIABILITY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkElevated)
                    .background(GlassGradientHero)
                    .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkBase)
                    .background(GlassGradientCard)
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
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
                            HorizontalDivider(color = GlassBorderColorSubtle, modifier = Modifier.padding(vertical = 10.dp))
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
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x18FFFFFF))
                                            .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(10.dp))
                                            .clickable {
                                                val component = android.content.ComponentName(context, providerClass)
                                                appWidgetManager.requestPinAppWidget(component, null, null)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Pin", style = MaterialTheme.typography.labelSmall, color = PureWhite)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(GlassDarkBase)
                    .background(GlassGradientCard)
                    .border(1.dp, GlassBorderBrushSubtle, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF080B0F))
                                .border(1.dp, GlassBorderBrushHighlight, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.ic_launcher),
                                contentDescription = "Noor App Icon",
                                modifier = Modifier.size(46.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NOOR ISLAMIC COMPANION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Version 1.0.0 • Offline-first",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Crafted with black-and-white elegance, glassmorphism gradients, high-precision astronomical algorithms, authentic Uthmani script, and responsive home screen widgets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button to open system Android App Info page
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) { }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PureWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrushSubtle),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_app_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PureWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open System App Info",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PureWhite
                        )
                    }
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
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(WhiteSilverGradient)
                                            .border(1.dp, PureWhite, shape)
                                    } else {
                                        Modifier
                                            .background(Color.Transparent)
                                    }
                                )
                                .clickable { viewModel.selectCalculationMethod(method) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = method.titleEn,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) VoidBlack else PureWhite
                                )
                                Text(
                                    text = "Fajr: ${method.fajrAngle}° | Isha: ${if (method.ishaMinutesAfterMaghrib > 0) "+${method.ishaMinutesAfterMaghrib}m" else "${method.ishaAngle}°"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFF333333) else TextMuted
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

    // QURAN RECITER SELECTION DIALOG
    if (uiState.showReciterDialog) {
        ReciterSelectionDialog(
            selectedReciter = uiState.selectedReciter,
            onSelectReciter = { reciter ->
                viewModel.selectReciter(reciter)
            },
            onDismiss = { viewModel.setShowReciterDialog(false) }
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

@Composable
fun SoundOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = PureWhite,
                    unselectedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) PureWhite else TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
        IconButton(
            onClick = onPreview,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) WhiteSilverGradient else SolidColor(Color(0x1AFFFFFF)))
        ) {
            Icon(
                imageVector = Icons.Filled.VolumeUp,
                contentDescription = "Preview Sound",
                tint = if (isSelected) VoidBlack else PureWhite,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
