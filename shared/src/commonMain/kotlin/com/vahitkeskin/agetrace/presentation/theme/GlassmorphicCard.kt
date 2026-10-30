package com.vahitkeskin.agetrace.presentation.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * High-end Glassmorphic Card (Apple visionOS / Liquid Glass style).
 * Combines translucent frosted gradient surfaces with authentic specular rim highlights.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    backgroundColor: Color = GlassCardBg,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    borderBrush: Brush? = null,
    backgroundBrush: Brush? = null,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveBgBrush = backgroundBrush ?: Brush.verticalGradient(
        colors = listOf(
            backgroundColor.copy(alpha = (backgroundColor.alpha * 1.15f).coerceAtMost(1f)),
            backgroundColor,
            backgroundColor.copy(alpha = (backgroundColor.alpha * 0.85f).coerceAtLeast(0.2f))
        )
    )

    val effectiveBorderBrush = borderBrush ?: Brush.verticalGradient(
        colors = listOf(
            borderColor.copy(alpha = (borderColor.alpha * 1.4f).coerceAtMost(1f)),
            borderColor,
            borderColor.copy(alpha = (borderColor.alpha * 0.4f).coerceAtLeast(0.05f))
        )
    )

    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = shape,
                        spotColor = Color(0x33000000),
                        ambientColor = Color(0x22000000)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(brush = effectiveBgBrush)
            .border(
                border = BorderStroke(width = borderWidth, brush = effectiveBorderBrush),
                shape = shape
            ),
        content = content
    )
}

/**
 * Capsule-shaped frosted glass pill for selections, actions, and tags.
 */
@Composable
fun GlassmorphicPill(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectedColor: Color = AccentBlue,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    val bgBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                selectedColor.copy(alpha = 0.28f),
                selectedColor.copy(alpha = 0.14f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x38FFFFFF),
                Color(0x14FFFFFF),
                Color(0x0AFFFFFF)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                selectedColor.copy(alpha = 0.85f),
                selectedColor.copy(alpha = 0.40f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x38FFFFFF),
                Color(0x14FFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(brush = bgBrush)
            .border(
                border = BorderStroke(width = 1.dp, brush = borderBrush),
                shape = shape
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Polite, delicate status badge with a glowing status dot.
 */
@Composable
fun GlassmorphicBadge(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    hasGlowDot: Boolean = true
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.18f),
                        accentColor.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.55f),
                            accentColor.copy(alpha = 0.20f)
                        )
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 10.dp, vertical = 4.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasGlowDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Circular frosted glass action button.
 */
@Composable
fun GlassmorphicIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    tint: Color = TextSecondary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x3DFFFFFF),
                        Color(0x1A1E293B),
                        Color(0x0F0F172A)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = GlassSpecularBorderBrush
                ),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = tint,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Ambient backdrop that creates soft, ethereal colored light spots behind frosted panels,
 * providing authentic visual depth and luminous refraction for glassmorphism.
 */
@Composable
fun AmbientGlassBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Top-left subtle Azure/Cyan atmospheric glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AmbientCyan.copy(alpha = 0.12f),
                    AmbientCyan.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(w * 0.15f, h * 0.15f),
                radius = w * 0.50f
            ),
            center = Offset(w * 0.15f, h * 0.15f),
            radius = w * 0.50f
        )

        // Mid-right subtle Indigo atmospheric glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AmbientIndigo.copy(alpha = 0.14f),
                    AmbientIndigo.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(w * 0.85f, h * 0.45f),
                radius = w * 0.60f
            ),
            center = Offset(w * 0.85f, h * 0.45f),
            radius = w * 0.60f
        )

        // Bottom-left subtle Rose atmospheric glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AmbientRose.copy(alpha = 0.08f),
                    AmbientRose.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                center = Offset(w * 0.30f, h * 0.85f),
                radius = w * 0.55f
            ),
            center = Offset(w * 0.30f, h * 0.85f),
            radius = w * 0.55f
        )
    }
}
