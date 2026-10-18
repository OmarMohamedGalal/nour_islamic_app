package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranReciter
import com.example.ui.components.glass.LiquidGlassSecondaryButton
import com.example.ui.theme.*

@Composable
fun ReciterSelectionDialog(
    selectedReciter: QuranReciter,
    onSelectReciter: (QuranReciter) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogShape = RoundedCornerShape(28.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LiquidGlassSurfaceElevated,
        modifier = Modifier
            .clip(dialogShape)
            .border(1.dp, LiquidGlassBorderSpecular, dialogShape),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x18FFFFFF))
                        .border(1.dp, LiquidGlassBorderSubtle, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = LiquidCyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "اختيار قارئ القرآن",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                    Text(
                        text = "Select Quran Reciter",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(QuranReciter.entries.size) { i ->
                    val reciter = QuranReciter.entries[i]
                    val isSelected = reciter == selectedReciter
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(LiquidGlassButtonActiveGradient)
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0x90A0D0FF))),
                                            shape
                                        )
                                } else {
                                    Modifier
                                        .background(LiquidGlassSurfaceBase)
                                        .background(LiquidGlassSurfaceGradient)
                                        .border(1.dp, LiquidGlassBorderSubtle, shape)
                                }
                            )
                            .clickable { onSelectReciter(reciter) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = reciter.nameAr,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) VoidBlack else PureWhite
                                    )
                                    if (!reciter.isComplete) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) VoidBlack.copy(alpha = 0.15f) else Color(0x33FFFFFF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "سور محددة",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = if (isSelected) VoidBlack else TextSecondary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = reciter.nameEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) Color(0xFF333333) else TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = VoidBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            LiquidGlassSecondaryButton(
                text = "Close • إغلاق",
                onClick = onDismiss
            )
        }
    )
}
