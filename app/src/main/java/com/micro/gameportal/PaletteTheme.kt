package com.micro.gameportal

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/** Maps a user-selectable [Palette] onto a Material 3 color scheme. */
fun Palette.toColorScheme(): ColorScheme = darkColorScheme(
    primary = Color(primary),
    onPrimary = Color(background),
    surface = Color(background),
    onSurface = Color(text),
    surfaceVariant = Color(surfaceDim),
    onSurfaceVariant = Color(textDim),
    secondary = Color(accent),
    onSecondary = Color(background),
)
