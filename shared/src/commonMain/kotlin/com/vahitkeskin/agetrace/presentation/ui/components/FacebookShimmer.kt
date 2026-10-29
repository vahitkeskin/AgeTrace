package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import kotlin.math.sqrt

/**
 * Full-bleed Facebook-style diagonal shimmering wave brush.
 * Dynamically calculated according to container dimensions so the wave spans and completely
 * covers the entire area of the large image viewport without gaps.
 */
@Composable
fun FullFacebookShimmer(
    modifier: Modifier = Modifier,
    isOverlay: Boolean = false
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val diagonal = sqrt(widthPx * widthPx + heightPx * heightPx).coerceAtLeast(600f)

        val transition = rememberInfiniteTransition(label = "facebook_shimmer_wave")
        val translateAnimation by transition.animateFloat(
            initialValue = -diagonal * 0.4f,
            targetValue = diagonal * 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translation"
        )

        val waveSpan = diagonal * 0.55f

        val shimmerColors = if (isOverlay) {
            // Radiant translucent wave when sweeping over an already visible photo
            listOf(
                Color.Transparent,
                Color(0x1F38BDF8),
                Color(0x5538BDF8),
                Color(0xC0FFFFFF),
                Color(0x5538BDF8),
                Color(0x1F38BDF8),
                Color.Transparent
            )
        } else {
            // High-contrast vivid Facebook shimmer over deep luxury dark background
            listOf(
                Color(0x0A38BDF8),
                Color(0x2838BDF8),
                Color(0x6038BDF8),
                Color(0xE6FFFFFF),
                Color(0x6038BDF8),
                Color(0x2838BDF8),
                Color(0x0A38BDF8)
            )
        }

        val shimmerBrush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x = translateAnimation - waveSpan, y = translateAnimation - waveSpan),
            end = Offset(x = translateAnimation, y = translateAnimation)
        )

        // 1. Shimmer base layer covering the entire large image viewport
        if (!isOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD90B0F19))
            )
        }

        // 2. The full-bleed sweeping wave covering the entire large image area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerBrush)
        )

        // 3. Floating Frosted Status Badge at bottom center
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(Color(0xE60F172A), RoundedCornerShape(999.dp))
                    .border(1.dp, Color(0x4038BDF8), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(13.dp),
                    color = AccentBlue,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Görsel taranıyor...",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Full viewport shimmer skeleton rendered while an image is being loaded / analyzed from empty state.
 * Fully covers the large image container area.
 */
@Composable
fun FacebookShimmerSkeletonViewport(
    modifier: Modifier = Modifier
) {
    FullFacebookShimmer(
        modifier = modifier,
        isOverlay = false
    )
}

/**
 * Lightweight shimmer wave overlay drawn over an existing image while re-analysis or rotation is running.
 * Fully covers the large image area.
 */
@Composable
fun FacebookShimmerViewportOverlay(
    modifier: Modifier = Modifier
) {
    FullFacebookShimmer(
        modifier = modifier,
        isOverlay = true
    )
}
