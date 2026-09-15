package com.example.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AzkarCategory
import com.example.data.model.AzkarItem
import com.example.data.repository.AzkarRepository
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*

@Composable
fun AzkarScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val items = AzkarRepository.getByCategory(uiState.selectedAzkarCategory)
    val currentItem = items.getOrNull(uiState.currentAzkarIndex) ?: items.firstOrNull()
    val count = currentItem?.let { uiState.azkarProgressMap[it.id] } ?: 0
    val target = currentItem?.targetCount ?: 1
    val isCompleted = count >= target

    val view = LocalView.current
    val scrollState = rememberScrollState()

    // Tactile feedback using Android HapticFeedbackConstants
    val triggerCounterHaptic = remember(view) {
        { isMilestoneReached: Boolean ->
            if (isMilestoneReached) {
                // Milestone or completion reached (e.g. 33/33, 100/100) - distinctive, satisfying confirmation pulse
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.CONFIRM,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    )
                } else {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    )
                }
            } else {
                // Individual count tick - crisp, responsive mechanical clock tick or segment tick
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.SEGMENT_TICK,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.CLOCK_TICK,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    )
                } else {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.KEYBOARD_TAP,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "REMEMBRANCE & TASBEEH",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted
                )
                Text(
                    text = "الأذكار والتسبيح",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite
                )
            }

            IconButton(
                onClick = {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.KEYBOARD_TAP,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                    )
                    viewModel.resetAzkarCategory()
                },
                modifier = Modifier.testTag("reset_azkar_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Reset Category",
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CATEGORIES CAROUSEL
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AzkarCategory.entries) { cat ->
                val isSelected = cat == uiState.selectedAzkarCategory
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PureWhite else SurfaceCard,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            view.performHapticFeedback(
                                HapticFeedbackConstants.KEYBOARD_TAP,
                                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                            )
                            viewModel.setAzkarCategory(cat)
                        }
                        .testTag("azkar_cat_${cat.id}")
                ) {
                    Text(
                        text = cat.titleEn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) VoidBlack else TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (currentItem != null) {
            // ITEM PROGRESS / INDEX (e.g. 1 of 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dhikr ${uiState.currentAzkarIndex + 1} of ${items.size}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextMuted
                )

                // Linear completion bar
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceCardHighest)
                ) {
                    val progress = if (target > 0) count.toFloat() / target.toFloat() else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(if (isCompleted) AccentGreen else PureWhite)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ARABIC DHIKR CARD - also clickable for natural, easy tap during recitation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        val willComplete = (count + 1) >= target
                        triggerCounterHaptic(willComplete && !isCompleted)
                        viewModel.incrementAzkarCount(currentItem)
                    }
                    .testTag("azkar_text_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentItem.arabicText,
                        style = ArabicAzkarTextStyle,
                        color = PureWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentItem.transliteration,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentItem.translation,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = "${currentItem.reference} • ${currentItem.benefit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TACTILE HAPTIC STATUS BADGE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isCompleted) AccentGreen else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCompleted) "Target Completed • Dhikr Finished" else "Tactile Haptic Feedback Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) AccentGreen else TextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LARGE TACTILE SPRING COUNTER BUTTON
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.90f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "counterScale"
            )

            Box(
                modifier = Modifier
                    .size(144.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(if (isCompleted) SurfaceCardHigh else PureWhite)
                    .border(
                        width = 4.dp,
                        color = if (isCompleted) AccentGreen else BorderOutline,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        val willComplete = (count + 1) >= target
                        triggerCounterHaptic(willComplete && !isCompleted)
                        viewModel.incrementAzkarCount(currentItem)
                    }
                    .testTag("azkar_counter_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ),
                        color = if (isCompleted) AccentGreen else VoidBlack
                    )
                    Text(
                        text = "OF $target",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isCompleted) TextMuted else Color(0xFF555555)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // PREV / NEXT BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        view.performHapticFeedback(
                            HapticFeedbackConstants.KEYBOARD_TAP,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        )
                        viewModel.previousAzkar()
                    },
                    enabled = uiState.currentAzkarIndex > 0,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Previous")
                }

                Button(
                    onClick = {
                        view.performHapticFeedback(
                            HapticFeedbackConstants.KEYBOARD_TAP,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        )
                        viewModel.nextAzkar()
                    },
                    enabled = uiState.currentAzkarIndex < items.size - 1,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = VoidBlack)
                ) {
                    Text("Next", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
