package com.vahitkeskin.agetrace

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vahitkeskin.agetrace.presentation.theme.BackgroundDark
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.SurfaceDark
import com.vahitkeskin.agetrace.presentation.ui.AgeTraceScreen

private val AgeTraceDarkColors = darkColorScheme(
    primary = NeonRed,
    onPrimary = Color.White,
    primaryContainer = Color(0x33FF1744),
    onPrimaryContainer = NeonRed,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = Color(0xFFCBD5E1),
    background = BackgroundDark,
    onBackground = Color.White
)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = AgeTraceDarkColors
    ) {
        AgeTraceScreen()
    }
}