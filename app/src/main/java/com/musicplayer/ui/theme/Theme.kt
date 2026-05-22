package com.musicplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PlayerInactive,
    secondary = PlayerActive,
    tertiary = PlayerDormant,
    background = PlayerBg,
    surface = PlayerBg,
    onPrimary = PlayerBg,
    onSecondary = PlayerBg,
    onTertiary = PlayerText,
    onBackground = PlayerText,
    onSurface = PlayerText,
)

@Composable
fun MusicPlayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
