package com.vahitkeskin.agetrace.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Pure Executive Glassmorphism Palette (Apple visionOS & Linear Inspired)
// Polite, Minimalist, Ethereal Translucency
// =========================================================================

// Deep space background with soft atmospheric depth
val BackgroundDark = Color(0xFF080B14)
val BackgroundSurface = Color(0xFF0D121F)
val SurfaceDark = Color(0xFF111827)
val SurfaceCard = Color(0xFF161F33)
val SurfaceCardHover = Color(0xFF1E2B45)
val SurfaceBorder = Color(0x26FFFFFF)

// Ambient Glow Orbs (Subtle, non-distracting atmospheric refraction)
val AmbientIndigo = Color(0xFF4F46E5)
val AmbientCyan = Color(0xFF0EA5E9)
val AmbientRose = Color(0xFFE11D48)
val AmbientTeal = Color(0xFF14B8A6)

// Signature Biometric Accents (Polite, high-contrast, crystal clear)
val AccentRed = Color(0xFFF43F5E)
val AccentRedSubtle = Color(0x24F43F5E)
val NeonRed = Color(0xFFF43F5E)
val NeonRedGlow = Color(0x20F43F5E)
val NeonRedDark = Color(0xFFBE123C)

// Polite Status Tones (Refined emerald for minors, coral rose for adults)
val AdultColor = Color(0xFFFB7185)
val AdultColorGlow = Color(0x26FB7185)
val AdultColorBg = Color(0x1EFB7185)

val MinorColor = Color(0xFF34D399)
val MinorColorGlow = Color(0x2634D399)
val MinorColorBg = Color(0x1E34D399)

// Vision & Analysis Accents
val AccentBlue = Color(0xFF38BDF8)
val AccentBlueSubtle = Color(0x2038BDF8)
val CyanNeon = Color(0xFF00E5FF)
val PurpleAccent = Color(0xFF818CF8)
val AmberWarning = Color(0xFFFBBF24)
val GreenSuccess = Color(0xFF34D399)
val GreenSuccessSubtle = Color(0x2034D399)

// High-Legibility Typography
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextAccent = Color(0xFF7DD3FC)

// Glassmorphism System Constants
val GlassCardBg = Color(0x730E1526) // ~45% opacity deep obsidian
val GlassCardBgElevated = Color(0x99131C33) // ~60% opacity elevated glass
val GlassWhite = Color(0x0FFFFFFF)
val GlassBorder = Color(0x2EFFFFFF)
val GlassBorderSubtle = Color(0x1AFFFFFF)
val GlassHighlight = Color(0x4DFFFFFF)
val GlassRed = Color(0x24F43F5E)

// Frosted Glass Brushes for authentic specular lighting
val GlassSpecularBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x4DFFFFFF), // Top edge catches direct light
        Color(0x1FFFFFFF), // Mid edge transitions softly
        Color(0x0DFFFFFF)  // Bottom edge subtle refraction
    )
)

val GlassSpecularBorderBrushSelected = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF38BDF8).copy(alpha = 0.8f),
        Color(0xFF818CF8).copy(alpha = 0.5f),
        Color(0x26FFFFFF)
    )
)

val GlassSurfaceBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x99162035),
        Color(0x660D1424)
    )
)

val GlassElevatedSurfaceBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xB31E2A44),
        Color(0x80131B2E)
    )
)
