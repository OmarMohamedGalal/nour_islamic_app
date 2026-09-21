package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val VoidBlack = Color(0xFF0D0D11)
val SurfaceLowest = Color(0xFF08080A)
val SurfaceLow = Color(0xFF131317)
val SurfaceCard = Color(0xFF1B1B22)
val SurfaceCardHigh = Color(0xFF24242D)
val SurfaceCardHighest = Color(0xFF30303B)

val BorderSubtle = Color(0xFF262630)
val BorderOutline = Color(0xFF3A3A47)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFD1D3D8)
val TextMuted = Color(0xFF8E909A)

val PureWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF2F2F5)
val SilverMuted = Color(0xFFC8C9CE)
val AccentGold = Color(0xFFDFC67A)
val AccentGreen = Color(0xFF34D399)

// GLASSMORPHISM & BLACK & WHITE GRADIENT SYSTEM
val GlassDarkBase = Color(0xD9121217)
val GlassDarkElevated = Color(0xEB181820)
val GlassDarkDeep = Color(0xF20F0F14)

val GlassWhiteOverlayLight = Color(0x14FFFFFF)
val GlassWhiteOverlayMedium = Color(0x22FFFFFF)
val GlassWhiteOverlayHigh = Color(0x35FFFFFF)

val GlassBorderColorLight = Color(0x4DFFFFFF)
val GlassBorderColorSubtle = Color(0x22FFFFFF)
val GlassBorderColorFaint = Color(0x10FFFFFF)

// Gradients for Black and White Glassify
val GlassGradientCard: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0x20FFFFFF),
        Color(0x0AFFFFFF),
        Color(0x02FFFFFF)
    )
)

val GlassGradientHero: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0x2EFFFFFF),
        Color(0x12FFFFFF),
        Color(0x04FFFFFF)
    )
)

val GlassGradientActive: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0x45FFFFFF),
        Color(0x20FFFFFF),
        Color(0x0AFFFFFF)
    )
)

val GlassBorderBrush: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0x66FFFFFF),
        Color(0x24FFFFFF),
        Color(0x0AFFFFFF)
    )
)

val GlassBorderBrushHighlight: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0x99FFFFFF),
        Color(0x45FFFFFF),
        Color(0x15FFFFFF)
    )
)

val GlassBorderBrushSubtle: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0x35FFFFFF),
        Color(0x14FFFFFF),
        Color(0x05FFFFFF)
    )
)

val WhiteSilverGradient: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFE8E8EC),
        Color(0xFFCECED4)
    )
)

val BlackCharcoalGradient: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF22222A),
        Color(0xFF141419),
        Color(0xFF0C0C0F)
    )
)

val AmbientBackgroundGradient: Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF15151E),
        Color(0xFF0D0D12),
        Color(0xFF07070A)
    )
)


