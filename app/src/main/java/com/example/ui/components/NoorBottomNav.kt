package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = VoidBlack,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
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
                Triple(NoorDestination.QURAN, Icons.Outlined.MenuBook, Icons.Filled.MenuBook),
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
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onDestinationSelected(destination) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("nav_${destination.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PureWhite else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) selectedIcon else unselectedIcon,
                            contentDescription = destination.titleEn,
                            tint = if (isSelected) VoidBlack else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = destination.titleEn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) PureWhite else TextMuted
                    )
                }
            }
        }
    }
}
