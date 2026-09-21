package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NoorDestination
import com.example.ui.theme.*

@Composable
fun NoorBottomNavBar(
    currentDestination: NoorDestination,
    onDestinationSelected: (NoorDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, GlassBorderBrush, RoundedCornerShape(28.dp)),
            color = GlassDarkElevated,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 12.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassGradientCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navItems = listOf(
                        Triple(NoorDestination.PRAYER, Icons.Outlined.AccessTime, Icons.Filled.AccessTime),
                        Triple(NoorDestination.QURAN, Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
                        Triple(NoorDestination.AZKAR, Icons.Outlined.Fingerprint, Icons.Filled.Fingerprint),
                        Triple(NoorDestination.DUAA, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
                        Triple(NoorDestination.QIBLA, Icons.Outlined.Explore, Icons.Filled.Explore),
                        Triple(NoorDestination.CALENDAR, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
                        Triple(NoorDestination.SETTINGS, Icons.Outlined.Settings, Icons.Filled.Settings)
                    )

                    navItems.forEach { (destination, unselectedIcon, selectedIcon) ->
                        val isSelected = currentDestination == destination
                        val interactionSource = remember { MutableInteractionSource() }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onDestinationSelected(destination) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("nav_${destination.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) WhiteSilverGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0x10FFFFFF), Color(0x04FFFFFF))))
                                    .border(
                                        width = 1.dp,
                                        brush = if (isSelected) androidx.compose.ui.graphics.Brush.linearGradient(listOf(PureWhite, SilverMuted)) else GlassBorderBrushSubtle,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                    contentDescription = destination.titleEn,
                                    tint = if (isSelected) VoidBlack else TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = destination.titleEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) PureWhite else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

