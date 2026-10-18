package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier that applies Apple iOS 26 Liquid Glass optical surface with
 * translucent depth, subtle refractive cyan/indigo tint, and specular rim lighting.
 */
fun Modifier.liquidGlassEffect(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = LiquidGlassSurfaceBase,
    gradientBrush: Brush = LiquidGlassSurfaceGradient,
    borderBrush: Brush = LiquidGlassBorderSpecular,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .background(gradientBrush)
    .border(borderWidth, borderBrush, shape)

/**
 * Hero modifier for featured cards (countdown, surah headers, etc.)
 */
fun Modifier.liquidGlassHeroEffect(
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = LiquidGlassSurfaceElevated,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .background(LiquidGlassHeroGradient)
    .border(borderWidth, LiquidGlassBorderSpecular, shape)

/**
 * Legacy alias mapped to Liquid Glass for seamless compatibility
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = LiquidGlassSurfaceBase,
    gradientBrush: Brush = LiquidGlassSurfaceGradient,
    borderBrush: Brush = LiquidGlassBorderSpecular,
    borderWidth: Dp = 1.dp
): Modifier = liquidGlassEffect(shape, backgroundColor, gradientBrush, borderBrush, borderWidth)

fun Modifier.glassHeroEffect(
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = LiquidGlassSurfaceElevated,
    borderWidth: Dp = 1.dp
): Modifier = liquidGlassHeroEffect(shape, backgroundColor, borderWidth)

/**
 * Interactive glass pill for chips, filters, and capsule indicators.
 */
fun Modifier.glassPill(
    isSelected: Boolean,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp
): Modifier = if (isSelected) {
    this
        .clip(shape)
        .background(LiquidGlassButtonActiveGradient)
        .border(borderWidth, Color(0xFFFFFFFF), shape)
} else {
    this
        .clip(shape)
        .background(LiquidGlassSurfaceBase)
        .background(LiquidGlassSurfaceGradient)
        .border(borderWidth, LiquidGlassBorderSubtle, shape)
}

/**
 * A Card pre-configured with the Apple iOS 26 Liquid Glass styling.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    isHero: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = androidx.compose.material3.ripple(color = PureWhite)
        ) { onClick() }
    } else Modifier

    val baseModifier = if (isHero) {
        modifier
            .clip(shape)
            .liquidGlassHeroEffect(shape = shape)
            .then(clickModifier)
    } else {
        modifier
            .clip(shape)
            .liquidGlassEffect(shape = shape)
            .then(clickModifier)
    }

    Box(
        modifier = baseModifier,
        content = content
    )
}
