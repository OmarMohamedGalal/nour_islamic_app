package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerItem
import com.example.data.model.PrayerType
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PrayerScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    // Live second ticker for countdown
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            viewModel.refreshPrayerSchedule()
            delay(1000)
        }
    }

    val schedule = uiState.daySchedule
    val nextPrayer = uiState.nextPrayerItem?.prayer
    val remainingSeconds = uiState.nextPrayerItem?.remainingSeconds ?: 0

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val countdownFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER: App Title & City Selector Pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NOOR",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = PureWhite
                    )
                    Text(
                        text = uiState.hijriDate.formatEn(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                // City Selector Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.setShowCityDialog(true) }
                        .testTag("city_selector_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.currentCity.nameEn,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                    }
                }
            }
        }

        // HERO COUNTDOWN CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_countdown_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UPCOMING: ${nextPrayer?.type?.displayNameEn?.uppercase() ?: "PRAYER"} • ${nextPrayer?.type?.displayNameAr ?: ""}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = countdownFormatted,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ),
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
                    val prayerTimeStr = nextPrayer?.time?.format(timeFormatter) ?: "--:--"
                    Text(
                        text = "at $prayerTimeStr in ${uiState.currentCity.nameEn}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prayer Streak Indicators
                    val log = uiState.todayPrayerLog
                    val prayersDone = listOf(log.fajr, log.dhuhr, log.asr, log.maghrib, log.isha).count { it }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceLow)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today's Prayers: $prayersDone/5",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "F" to log.fajr,
                                "D" to log.dhuhr,
                                "A" to log.asr,
                                "M" to log.maghrib,
                                "I" to log.isha
                            ).forEach { (label, done) ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (done) PureWhite else SurfaceCardHighest),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                        color = if (done) VoidBlack else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5 PRAYERS SCHEDULE LIST
        if (schedule != null) {
            item {
                Text(
                    text = "DAILY SCHEDULE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            val prayerItems = schedule.asList()
            items(prayerItems.size) { index ->
                val item = prayerItems[index]
                val isNext = nextPrayer?.type == item.type
                val isDone = when (item.type) {
                    PrayerType.FAJR -> uiState.todayPrayerLog.fajr
                    PrayerType.DHUHR -> uiState.todayPrayerLog.dhuhr
                    PrayerType.ASR -> uiState.todayPrayerLog.asr
                    PrayerType.MAGHRIB -> uiState.todayPrayerLog.maghrib
                    PrayerType.ISHA -> uiState.todayPrayerLog.isha
                    else -> false
                }
                val isNotificationEnabled = when (item.type) {
                    PrayerType.FAJR -> uiState.userSettings.fajrNotification
                    PrayerType.DHUHR -> uiState.userSettings.dhuhrNotification
                    PrayerType.ASR -> uiState.userSettings.asrNotification
                    PrayerType.MAGHRIB -> uiState.userSettings.maghribNotification
                    PrayerType.ISHA -> uiState.userSettings.ishaNotification
                    else -> false
                }

                PrayerRowCard(
                    prayerItem = item,
                    isNext = isNext,
                    isDone = isDone,
                    isNotificationEnabled = isNotificationEnabled,
                    onToggleDone = {
                        if (item.type != PrayerType.SUNRISE) {
                            viewModel.togglePrayerDone(item.type.name)
                        }
                    },
                    onToggleNotification = {
                        if (item.type != PrayerType.SUNRISE) {
                            viewModel.toggleNotification(item.type)
                        }
                    }
                )
            }

            // NIGHT PRAYERS / TAHAJJUD CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLow),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ISLAMIC MIDNIGHT",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.midnight.format(fmt),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                                .background(BorderOutline)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LAST THIRD (TAHAJJUD)",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.lastThird.format(fmt),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }

    // CITY DIALOG
    if (uiState.showCityDialog) {
        CitySelectionDialog(
            currentCity = uiState.currentCity,
            onDismiss = { viewModel.setShowCityDialog(false) },
            onSelectCity = { viewModel.selectCity(it) }
        )
    }
}

@Composable
fun PrayerRowCard(
    prayerItem: PrayerItem,
    isNext: Boolean,
    isDone: Boolean,
    isNotificationEnabled: Boolean,
    onToggleDone: () -> Unit,
    onToggleNotification: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    val formattedTime = prayerItem.time.format(fmt)

    val containerColor = if (isNext) PureWhite else SurfaceCard
    val contentColor = if (isNext) VoidBlack else PureWhite
    val secondaryColor = if (isNext) Color(0xFF444444) else TextMuted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_${prayerItem.type.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isNext) null else androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Done check button (only for 5 obligatory prayers)
                if (prayerItem.type != PrayerType.SUNRISE) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                color = if (isDone) (if (isNext) VoidBlack else PureWhite) else secondaryColor,
                                shape = CircleShape
                            )
                            .background(if (isDone) (if (isNext) VoidBlack else PureWhite) else Color.Transparent)
                            .clickable { onToggleDone() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Done",
                                tint = if (isNext) PureWhite else VoidBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayerItem.type.displayNameEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = prayerItem.type.displayNameAr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryColor
                        )
                    }
                    if (isNext) {
                        Text(
                            text = "Next Prayer",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = contentColor
                )

                if (prayerItem.type != PrayerType.SUNRISE) {
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onToggleNotification,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isNotificationEnabled) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsOff,
                            contentDescription = "Notification",
                            tint = if (isNotificationEnabled) contentColor else secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CitySelectionDialog(
    currentCity: com.example.data.model.CityLocation,
    onDismiss: () -> Unit,
    onSelectCity: (com.example.data.model.CityLocation) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardHigh,
        title = {
            Text(
                text = "Select City",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PureWhite
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                items(com.example.data.prayer.LocationHelper.WORLD_CITIES.size) { i ->
                    val city = com.example.data.prayer.LocationHelper.WORLD_CITIES[i]
                    val isSelected = city.nameEn == currentCity.nameEn
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectCity(city) },
                        color = if (isSelected) SurfaceCardHighest else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.nameEn,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                                Text(
                                    text = "${city.countryEn} • ${city.defaultMethod.titleEn}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = city.nameAr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PureWhite)
            }
        }
    )
}
