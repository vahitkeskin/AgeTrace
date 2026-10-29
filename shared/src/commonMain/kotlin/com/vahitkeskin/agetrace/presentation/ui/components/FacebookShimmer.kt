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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary

/**
 * Animated Facebook-style diagonal wave shimmer brush.
 * Creates an authentic smooth sweeping light wave across placeholder elements.
 */
@Composable
fun rememberFacebookShimmerBrush(
    shimmerColors: List<Color> = listOf(
        Color(0x08FFFFFF),
        Color(0x1838BDF8),
        Color(0x3838BDF8),
        Color(0x6638BDF8),
        Color(0x3838BDF8),
        Color(0x1838BDF8),
        Color(0x08FFFFFF)
    ),
    durationMillis: Int = 1300
): Brush {
    val transition = rememberInfiniteTransition(label = "facebook_shimmer_wave")
    val translateAnimation by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation - 450f, y = translateAnimation - 450f),
        end = Offset(x = translateAnimation, y = translateAnimation)
    )
}

/**
 * Full viewport shimmer skeleton rendered while an image is being loaded / analyzed from empty state.
 */
@Composable
fun FacebookShimmerSkeletonViewport(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberFacebookShimmerBrush()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xD90B0F19)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Full-bleed diagonal shimmer wave
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerBrush)
        )

        // 2. Shimmering Biometric Skeleton Anatomy
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Simulated shimmering face focus bracket box
            Box(
                modifier = Modifier
                    .size(130.dp, 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.5.dp, shimmerBrush, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Shimmering Face silhouette circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )

                // Shimmering scanning line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AccentBlue.copy(alpha = 0.8f))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Elegant Frosted Status Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xB30F172A))
                    .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = AccentBlue,
                    strokeWidth = 2.dp
                )

                Text(
                    text = "Görsel yükleniyor & analiz ediliyor...",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Yapay zeka biyometrik yüz tespiti devrede",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Lightweight shimmer wave overlay drawn over an existing image while re-analysis or rotation is running.
 */
@Composable
fun FacebookShimmerViewportOverlay(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberFacebookShimmerBrush(
        shimmerColors = listOf(
            Color.Transparent,
            Color(0x1538BDF8),
            Color(0x4038BDF8),
            Color(0x6038BDF8),
            Color(0x4038BDF8),
            Color(0x1538BDF8),
            Color.Transparent
        ),
        durationMillis = 1100
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(shimmerBrush)
    )
}

