package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    secondary = Ink2,
    onSecondary = Paper2,
    background = Paper,
    surface = White,
    surfaceVariant = Paper2,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Ink3,
    outline = Rule,
    outlineVariant = Rule2
)

@Composable
fun AIHubTheme(
    darkTheme: Boolean = false, // Force Light mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
