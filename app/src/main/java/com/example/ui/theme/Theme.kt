package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkGlassColorScheme = darkColorScheme(
    primary = VividGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = VividGreen,
    secondary = DeepGreen,
    onSecondary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = GlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceTransparent,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorderLight,
    error = AlertRed,
    onError = TextPrimary,
    errorContainer = AlertRedContainer,
    onErrorContainer = AlertRed
)

val GlassShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun StoreOSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkGlassColorScheme,
        typography = Typography,
        shapes = GlassShapes,
        content = content
    )
}
