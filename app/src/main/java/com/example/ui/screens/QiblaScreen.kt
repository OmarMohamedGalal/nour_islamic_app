package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun QiblaScreen(
    viewModel: NoorViewModel,
    uiState: NoorUiState,
    modifier: Modifier = Modifier
) {
    val qiblaState = uiState.qiblaState

    // Start sensors when screen is visible
    DisposableEffect(Unit) {
        viewModel.qiblaSensorManager.start()
        onDispose {
            viewModel.qiblaSensorManager.stop()
        }
    }

    // Smooth compass animation with spring spec
    val animatedAzimuth by animateFloatAsState(
        targetValue = qiblaState.deviceAzimuth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "azimuthAnim"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(horizontal = 16.dp),
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
                    text = "QIBLA COMPASS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted
                )
                Text(
                    text = "اتجاه القبلة الشريفة",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Text(
                    text = "${qiblaState.distanceKm.toInt()} KM",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TARGET ALIGNMENT BANNER
        AnimatedVisibility(visible = qiblaState.isAligned) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = AccentGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALIGNED DIRECTLY WITH THE HOLY KAABA",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AccentGreen
                    )
                }
            }
        }

        // COMPASS DIAL CANVAS
        Box(
            modifier = Modifier
                .size(300.dp)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 16.dp.toPx()

                // Outer decorative rings
                drawCircle(
                    color = BorderOutline,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = SurfaceCard,
                    radius = radius - 12.dp.toPx(),
                    center = center
                )

                // Rotate ticks according to device heading
                rotate(-animatedAzimuth, center) {
                    // Draw 360 degree tick marks
                    for (deg in 0 until 360 step 15) {
                        val isMajor = deg % 90 == 0
                        val tickLength = if (isMajor) 14.dp.toPx() else 6.dp.toPx()
                        val tickColor = if (isMajor) PureWhite else BorderOutline
                        val rad = Math.toRadians(deg.toDouble())

                        val startX = center.x + (radius - tickLength) * sin(rad).toFloat()
                        val startY = center.y - (radius - tickLength) * cos(rad).toFloat()
                        val endX = center.x + radius * sin(rad).toFloat()
                        val endY = center.y - radius * cos(rad).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                        )
                    }

                    // Draw Kaaba pointer icon at qibla bearing
                    val qiblaRad = Math.toRadians(qiblaState.qiblaBearing.toDouble())
                    val kaabaX = center.x + (radius - 35.dp.toPx()) * sin(qiblaRad).toFloat()
                    val kaabaY = center.y - (radius - 35.dp.toPx()) * cos(qiblaRad).toFloat()

                    drawCircle(
                        color = if (qiblaState.isAligned) AccentGreen else PureWhite,
                        radius = 12.dp.toPx(),
                        center = Offset(kaabaX, kaabaY)
                    )

                    // Connecting vector to Kaaba
                    drawLine(
                        color = if (qiblaState.isAligned) AccentGreen else TextMuted,
                        start = center,
                        end = Offset(kaabaX, kaabaY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Center fixed indicator
                drawCircle(
                    color = if (qiblaState.isAligned) AccentGreen else PureWhite,
                    radius = 8.dp.toPx(),
                    center = center
                )
            }

            // Fixed Top Heading Marker
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = if (qiblaState.isAligned) AccentGreen else PureWhite,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DEGREE READOUT STATS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QIBLA BEARING",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${qiblaState.qiblaBearing.toInt()}°",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = PureWhite
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT HEADING",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${qiblaState.deviceAzimuth.toInt()}°",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = PureWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CALIBRATION ADVICE
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceLow,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "For optimal accuracy, place the device on a flat surface away from magnets or calibrate by moving in a figure-8.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}
