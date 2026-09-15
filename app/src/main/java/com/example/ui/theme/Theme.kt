package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NoorDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = VoidBlack,
    primaryContainer = SurfaceCardHigh,
    onPrimaryContainer = PureWhite,
    secondary = TextSecondary,
    onSecondary = VoidBlack,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = PureWhite,
    background = VoidBlack,
    onBackground = PureWhite,
    surface = VoidBlack,
    onSurface = PureWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderOutline,
    outlineVariant = BorderSubtle
)

private val NoorLightColorScheme = lightColorScheme(
    primary = VoidBlack,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFF1F1F1),
    onPrimaryContainer = VoidBlack,
    secondary = Color(0xFF4A4A4A),
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFE5E5E5),
    onSecondaryContainer = VoidBlack,
    background = PureWhite,
    onBackground = VoidBlack,
    surface = PureWhite,
    onSurface = VoidBlack,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE5E5E5)
)

@Composable
fun NoorTheme(
    darkTheme: Boolean = true, // Default to stunning Stitch OLED black theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NoorDarkColorScheme else NoorLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NoorTheme(darkTheme = darkTheme, content = content)
}

