package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier that applies a translucent glass effect with angled gradient and specular border.
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassDarkBase,
    gradientBrush: Brush = GlassGradientCard,
    borderBrush: Brush = GlassBorderBrush,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .background(gradientBrush)
    .border(borderWidth, borderBrush, shape)

/**
 * Modifier for elevated or featured glass cards (like Hero countdown card).
 */
fun Modifier.glassHeroEffect(
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = GlassDarkElevated,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .background(GlassGradientHero)
    .border(borderWidth, GlassBorderBrushHighlight, shape)

/**
 * Modifier for interactive glass pills and filter chips.
 */
fun Modifier.glassPill(
    isSelected: Boolean,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp
): Modifier = if (isSelected) {
    this
        .clip(shape)
        .background(WhiteSilverGradient)
        .border(borderWidth, Color.White, shape)
} else {
    this
        .clip(shape)
        .background(GlassDarkBase)
        .background(GlassGradientCard)
        .border(borderWidth, GlassBorderBrushSubtle, shape)
}

/**
 * A Card pre-configured with the Glassmorphism styling.
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
            indication = androidx.compose.material3.ripple()
        ) { onClick() }
    } else Modifier

    val baseModifier = if (isHero) {
        modifier
            .clip(shape)
            .glassHeroEffect(shape = shape)
            .then(clickModifier)
    } else {
        modifier
            .clip(shape)
            .glassEffect(shape = shape)
            .then(clickModifier)
    }

    Box(
        modifier = baseModifier,
        content = content
    )
}
