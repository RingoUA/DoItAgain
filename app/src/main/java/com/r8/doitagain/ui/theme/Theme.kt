package com.r8.doitagain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006B5E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9EF2DE),
    onPrimaryContainer = Color(0xFF00201A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82D6C3),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005044),
    onPrimaryContainer = Color(0xFF9EF2DE),
)

@Composable
internal fun DoItAgainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
