package com.rushd.calisthenicscoach.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC8F56A),
    onPrimary = Color(0xFF152000),
    primaryContainer = Color(0xFF26380B),
    onPrimaryContainer = Color(0xFFE2FFAE),
    secondary = Color(0xFF77D9FF),
    onSecondary = Color(0xFF002F3E),
    secondaryContainer = Color(0xFF0D3A49),
    onSecondaryContainer = Color(0xFFBFEAFF),
    tertiary = Color(0xFFFFB86B),
    background = Color(0xFF0A0E12),
    onBackground = Color(0xFFF4F7F8),
    surface = Color(0xFF10161C),
    onSurface = Color(0xFFF4F7F8),
    surfaceVariant = Color(0xFF182028),
    onSurfaceVariant = Color(0xFFB9C2C9),
    outline = Color(0xFF34404A),
    outlineVariant = Color(0xFF222D35),
    error = Color(0xFFFFB4AB)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF456700),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8F56A),
    onPrimaryContainer = Color(0xFF142000),
    secondary = Color(0xFF006780),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBCE9FA),
    onSecondaryContainer = Color(0xFF001F29),
    tertiary = Color(0xFF805500),
    background = Color(0xFFF7F9F8),
    onBackground = Color(0xFF171C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171C1E),
    surfaceVariant = Color(0xFFE9EEF0),
    onSurfaceVariant = Color(0xFF4A555B),
    outline = Color(0xFF758188),
    outlineVariant = Color(0xFFD5DDE1)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

@Composable
fun CalisthenicsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
