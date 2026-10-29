package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicIconButton
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary
import kotlin.math.roundToInt

/**
 * Fullscreen Interactive Zoomable Image Viewer.
 * Features:
 * - Multi-touch smooth pinch-to-zoom (1.0x to 6.0x)
 * - Two-finger pan & smooth drag when zoomed in
 * - Double-tap to instantly zoom in (2.5x) or zoom out (1.0x)
 * - Synchronous zooming of both image pixels and face analysis overlay brackets & age tags
 * - Floating frosted glass header with zoom level badge, reset action, overlay toggle, and close button
 */
@Composable
fun FullscreenZoomableImageViewer(
    modifier: Modifier = Modifier,
    state: AgeTraceContract.State,
    onFaceClick: (com.vahitkeskin.agetrace.domain.model.DetectedFace) -> Unit,
    onDismiss: () -> Unit
) {
    val displayBitmap = state.displayBitmap ?: return
    val pixelBuffer = state.pixelBuffer ?: return

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showOverlayInViewer by remember { mutableStateOf(state.isAgeBalloonVisible) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color(0xFA070B14))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Gesture Zoomable Viewport
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            if (scale > 1.15f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                val targetScale = 2.5f
                                scale = targetScale
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val diff = center - tapOffset
                                val maxOffsetX = (size.width * (targetScale - 1f)) / 2f
                                val maxOffsetY = (size.height * (targetScale - 1f)) / 2f
                                offset = Offset(
                                    x = (diff.x * 1.5f).coerceIn(-maxOffsetX, maxOffsetX),
                                    y = (diff.y * 1.5f).coerceIn(-maxOffsetY, maxOffsetY)
                                )
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 6f)
                        scale = newScale

                        if (newScale > 1f) {
                            val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                            val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = displayBitmap,
                    contentDescription = "Fullscreen Target",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                if (showOverlayInViewer) {
                    FaceOverlayCanvas(
                        modifier = Modifier.fillMaxSize(),
                        faces = state.detectedFaces,
                        imageWidth = pixelBuffer.width,
                        imageHeight = pixelBuffer.height,
                        selectedFace = state.selectedFace,
                        onFaceClick = onFaceClick,
                        isScanning = false,
                        isAgeBalloonVisible = true
                    )
                }
            }
        }

        // 2. Floating Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Title & Zoom Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassmorphicCard(
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)),
                    cornerRadius = 999.dp,
                    borderBrush = GlassSpecularBorderBrush
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "✦", color = AccentBlue, fontSize = 12.sp)
                        Text(
                            text = "Tam Ekran",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Dynamic Zoom Level Indicator Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x3338BDF8))
                        .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    val formattedZoom = ((scale * 10f).roundToInt() / 10f)
                    Text(
                        text = "🔍 ${formattedZoom}x",
                        color = AccentBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: Actions [ Toggle Overlay | Reset Zoom | Close ]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Overlay Toggle Button (Etiketleri Aç/Kapat)
                GlassmorphicIconButton(
                    icon = if (showOverlayInViewer) "🎈" else "👁️",
                    onClick = { showOverlayInViewer = !showOverlayInViewer },
                    size = 36.dp
                )

                // Reset Zoom Button (Only when zoomed in)
                AnimatedVisibility(
                    visible = scale > 1.05f,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    GlassmorphicIconButton(
                        icon = "↺ 1x",
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        size = 36.dp
                    )
                }

                // Close Button
                GlassmorphicIconButton(
                    icon = "✕",
                    onClick = onDismiss,
                    size = 36.dp
                )
            }
        }

        // 3. Floating Bottom Hint Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            GlassmorphicCard(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)),
                cornerRadius = 999.dp,
                borderBrush = GlassSpecularBorderBrush
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (scale > 1.05f) {
                            "Kaydırmak için sürükleyin • Sıfırlamak için çift dokunun"
                        } else {
                            "Çift dokunarak veya iki parmağınızla yakınlaştırabilirsiniz"
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (state.detectedFaces.isNotEmpty()) {
                        Text(
                            text = "• ${state.detectedFaces.size} Yüz",
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

