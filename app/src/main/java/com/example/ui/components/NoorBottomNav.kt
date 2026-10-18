package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.getValue
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
                .clip(RoundedCornerShape(36.dp))
                .border(1.dp, GlassBorderBrush, RoundedCornerShape(36.dp)),
            color = GlassDarkElevated,
            shape = RoundedCornerShape(36.dp),
            tonalElevation = 14.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassGradientCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navItems = listOf(
                        Triple(NoorDestination.PRAYER, Icons.Outlined.AccessTime, Icons.Filled.AccessTime),
                        Triple(NoorDestination.QURAN, Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
                        Triple(NoorDestination.AZKAR, Icons.Outlined.Fingerprint, Icons.Filled.Fingerprint),
                        Triple(NoorDestination.DUAA, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
                        Triple(NoorDestination.QIBLA, Icons.Outlined.Explore, Icons.Filled.Explore),
                        Triple(NoorDestination.CALENDAR, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
                    )

                    navItems.forEach { (destination, unselectedIcon, selectedIcon) ->
                        val isSelected = currentDestination == destination
                        val interactionSource = remember { MutableInteractionSource() }

                        // Animated properties for smooth tab transitions
                        val iconContainerSize by animateDpAsState(
                            targetValue = if (isSelected) 44.dp else 40.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "navContainerSize"
                        )

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) VoidBlack else TextSecondary,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "navIconColor"
                        )

                        val labelColor by animateColorAsState(
                            targetValue = if (isSelected) PureWhite else TextMuted,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "navLabelColor"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onDestinationSelected(destination) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("nav_${destination.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(iconContainerSize)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) WhiteSilverGradient
                                        else androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(Color(0x10FFFFFF), Color(0x04FFFFFF))
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = if (isSelected) androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(PureWhite, SilverMuted)
                                        ) else GlassBorderBrushSubtle,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                    contentDescription = destination.titleAr,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = destination.titleAr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = labelColor
                            )
                        }
                    }
                }
            }
        }
    }
}
