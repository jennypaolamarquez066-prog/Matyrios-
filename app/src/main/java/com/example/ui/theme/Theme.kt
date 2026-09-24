package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GeminiPrimary,
    onPrimary = Color.White,
    primaryContainer = GeminiSurfaceVariant,
    onPrimaryContainer = GeminiTextPrimary,
    secondary = GeminiSecondary,
    onSecondary = Color.White,
    secondaryContainer = GeminiSurfaceCard,
    onSecondaryContainer = GeminiTextPrimary,
    tertiary = GeminiTertiary,
    onTertiary = Color.Black,
    background = GeminiBackground,
    onBackground = GeminiTextPrimary,
    surface = GeminiSurface,
    onSurface = GeminiTextPrimary,
    surfaceVariant = GeminiSurfaceVariant,
    onSurfaceVariant = GeminiTextSecondary,
    outline = GeminiBorder,
    outlineVariant = GeminiSurfaceCard
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // We intentionally prioritize the bespoke Gemini dark theme aesthetic for music production
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
