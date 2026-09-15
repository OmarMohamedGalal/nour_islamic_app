package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UpcomingHolidayItem
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val hijri = uiState.hijriDate
    val ramadan = uiState.ramadanStatus
    val holidays = uiState.upcomingHolidays

    val gregorianFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)
    val formattedGregorian = LocalDate.now().format(gregorianFmt)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "HIJRI CALENDAR & OCCASIONS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted
                )
                Text(
                    text = "التقويم الهجري والمناسبات",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite
                )
            }
        }

        // HERO HIJRI DATE CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hijri_date_card"),
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
                    Text(
                        text = "${hijri.day}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 72.sp,
                            lineHeight = 76.sp
                        ),
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${hijri.monthNameEn} ${hijri.year} AH",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )

                    Text(
                        text = "${hijri.monthNameAr} ${hijri.year} هـ",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = formattedGregorian,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // REGIONAL HIJRI CALENDAR ADJUSTMENT CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hijri_adjustment_card"),
                shape = RoundedCornerShape(22.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "REGIONAL CALENDAR ADJUSTMENT",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                    color = TextMuted
                                )
                                Text(
                                    text = "تعديل الرؤية الشرعية المحلية",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = PureWhite
                                )
                            }
                        }

                        // Current offset indicator badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.hijriAdjustmentDays == -2) PureWhite else SurfaceLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Text(
                                text = if (uiState.hijriAdjustmentDays > 0) "+${uiState.hijriAdjustmentDays}d" else "${uiState.hijriAdjustmentDays}d",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.hijriAdjustmentDays == -2) VoidBlack else PureWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Calibrated to ${hijri.day} ${hijri.monthNameEn} (offset: ${if (uiState.hijriAdjustmentDays > 0) "+${uiState.hijriAdjustmentDays}" else "${uiState.hijriAdjustmentDays}"} days) according to your local region's crescent moon sighting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Selection Chips: -2 Days (Region), -1 Day, 0 Days (Umm Al Qura), +1 Day, +2 Days
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val offsets = listOf(-2 to "-2d", -1 to "-1d", 0 to "0d", 1 to "+1d", 2 to "+2d")
                        offsets.forEach { (offset, label) ->
                            val isSelected = uiState.hijriAdjustmentDays == offset
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PureWhite else SurfaceLow,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) PureWhite else BorderSubtle
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        view.performHapticFeedback(
                                            HapticFeedbackConstants.KEYBOARD_TAP,
                                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                        )
                                        viewModel.setHijriAdjustment(offset)
                                    }
                                    .testTag("adjustment_chip_$offset")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) VoidBlack else PureWhite
                                    )
                                    if (offset == -2) {
                                        Text(
                                            text = "Region",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = if (isSelected) VoidBlack else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // RAMADAN STATUS & FASTING TRACKER CARD
        if (ramadan != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ramadan_status_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLow),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.NightsStay,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (ramadan.isRamadan) "RAMADAN DAY ${ramadan.currentDayOfRamadan}" else "RAMADAN COUNTDOWN",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = PureWhite
                                )
                            }

                            Text(
                                text = if (ramadan.isRamadan) "${ramadan.daysUntilEid} days to Eid" else "${ramadan.daysUntilRamadan} days left",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "SUHOOR / FAJR", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = ramadan.suhoorDeadline, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PureWhite)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "IFTAR / MAGHRIB", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(text = ramadan.iftarTime, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PureWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fasting progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SurfaceCardHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ramadan.fastingProgressPercent.coerceIn(0f, 1f))
                                    .background(PureWhite)
                            )
                        }
                    }
                }
            }
        }

        // UPCOMING HOLIDAYS LIST
        item {
            Text(
                text = "UPCOMING ISLAMIC OCCASIONS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                color = TextMuted,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        items(holidays, key = { it.holiday.id }) { item ->
            HolidayItemCard(holidayItem = item)
        }
    }
}

@Composable
fun HolidayItemCard(holidayItem: UpcomingHolidayItem) {
    val h = holidayItem.holiday
    val isSoon = holidayItem.daysRemaining <= 30

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("holiday_item_${h.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSoon) PureWhite else BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSoon) PureWhite else SurfaceLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = null,
                        tint = if (isSoon) VoidBlack else PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = h.nameEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                    Text(
                        text = h.nameAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Text(
                        text = h.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSoon) PureWhite else SurfaceLow,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (holidayItem.daysRemaining == 0L) "TODAY" else "in ${holidayItem.daysRemaining}d",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSoon) VoidBlack else TextSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
