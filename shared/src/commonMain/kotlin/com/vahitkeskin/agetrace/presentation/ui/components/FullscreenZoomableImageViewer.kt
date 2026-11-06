package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.min
import kotlin.math.roundToInt
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrushSelected
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicIconButton
import com.vahitkeskin.agetrace.presentation.theme.MinorColor
import com.vahitkeskin.agetrace.presentation.theme.MinorColorBg
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary

/**
 * Dedicated Fullscreen Interactive Zoom Viewer.
 *
 * Provides a dedicated high-fidelity canvas for:
 * - Multi-touch smooth pinch-to-zoom (1.0x to 6.0x) and pan
 * - Double-tap to zoom in (2.5x) or zoom out (1.0x)
 * - Tap to toggle face blur (with accurate coordinate mapping)
 * - Custom blur creation & resizing (with [CustomBlurOverlay])
 * - Universal face blur toggles (Blur All / Unblur All)
 * - Face bounding box & age balloon visibility toggle
 * - Image rotation and multi-page switching
 */
@Composable
fun FullscreenZoomableImageViewer(
    modifier: Modifier = Modifier,
    state: AgeTraceContract.State,
    onPageChange: (Int) -> Unit = {},
    onFaceClick: (DetectedFace, Int) -> Unit,
    onRotateClick: () -> Unit = {},
    onToggleBlurClick: () -> Unit = {},
    onToggleAgeBalloonClick: () -> Unit = {},
    onToggleCustomBlurMode: () -> Unit = {},
    onAddCustomBlur: (normX: Float, normY: Float) -> Unit = { _, _ -> },
    onUpdateCustomBlur: (id: String, normX: Float, normY: Float, normW: Float, normH: Float) -> Unit = { _, _, _, _, _ -> },
    onRemoveCustomBlur: (id: String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val pages = state.pages
    val activeIndex = if (pages.isNotEmpty()) {
        state.activePageIndex.coerceIn(0, pages.size - 1)
    } else {
        0
    }
    val currentPage = pages.getOrNull(activeIndex) ?: state.currentPage
    val displayBitmap = currentPage?.displayBitmap ?: state.displayBitmap ?: return
    val pixelBuffer = currentPage?.pixelBuffer ?: state.pixelBuffer ?: return
    val faces = currentPage?.detectedFaces ?: state.detectedFaces
    val customBlurs = currentPage?.customBlurRegions ?: state.customBlurRegions

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(activeIndex) {
        scale = 1f
        offset = Offset.Zero
    }

    val density = LocalDensity.current
    val currentScale by rememberUpdatedState(scale)
    val currentOffset by rememberUpdatedState(offset)
    val currentFaces by rememberUpdatedState(faces)
    val currentIsAddingCustomBlur by rememberUpdatedState(state.isAddingCustomBlur)
    val currentIsAgeBalloonVisible by rememberUpdatedState(state.isAgeBalloonVisible)

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
                .pointerInput(activeIndex) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            if (currentScale > 1.15f) {
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
                        },
                        onTap = { tapOffset ->
                            if (size.width > 0 && size.height > 0) {
                                val imgW = pixelBuffer.width.toFloat()
                                val imgH = pixelBuffer.height.toFloat()
                                if (imgW > 0f && imgH > 0f) {
                                    val fitScale = min(size.width.toFloat() / imgW, size.height.toFloat() / imgH)
                                    val renderedWPx = imgW * fitScale
                                    val renderedHPx = imgH * fitScale
                                    val offsetXPx = (size.width.toFloat() - renderedWPx) / 2f
                                    val offsetYPx = (size.height.toFloat() - renderedHPx) / 2f

                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val contentX = (tapOffset.x - currentOffset.x - centerX) / currentScale + centerX
                                    val contentY = (tapOffset.y - currentOffset.y - centerY) / currentScale + centerY

                                    if (currentIsAddingCustomBlur) {
                                        val localX = contentX - offsetXPx
                                        val localY = contentY - offsetYPx
                                        if (localX in 0f..renderedWPx && localY in 0f..renderedHPx) {
                                            val normX = (localX / renderedWPx).coerceIn(0.01f, 0.90f)
                                            val normY = (localY / renderedHPx).coerceIn(0.01f, 0.90f)
                                            onAddCustomBlur(normX, normY)
                                        }
                                    } else {
                                        val normX = (contentX - offsetXPx) / renderedWPx
                                        val normY = (contentY - offsetYPx) / renderedHPx

                                        val hitFaces = currentFaces.filter { face ->
                                            val box = face.boundingBox
                                            val marginX = box.normalizedWidth * 0.12f
                                            val marginY = box.normalizedHeight * 0.12f
                                            val inFaceBox = (normX >= (box.normalizedX - marginX) &&
                                                             normX <= (box.normalizedX + box.normalizedWidth + marginX) &&
                                                             normY >= (box.normalizedY - marginY) &&
                                                             normY <= (box.normalizedY + box.normalizedHeight + marginY))

                                            val fxPx = offsetXPx + box.normalizedX * renderedWPx
                                            val fyPx = offsetYPx + box.normalizedY * renderedHPx
                                            val fwPx = box.normalizedWidth * renderedWPx
                                            val fhPx = box.normalizedHeight * renderedHPx
                                            val minBalloonWPx = with(density) { 64.dp.toPx() }
                                            val maxBalloonWPx = with(density) { 100.dp.toPx() }
                                            val balloonWidthPx = (fwPx * 0.90f).coerceIn(minBalloonWPx, maxBalloonWPx)
                                            val faceCenterX = fxPx + fwPx / 2f
                                            val balloonXPx = (faceCenterX - balloonWidthPx / 2f).coerceIn(0f, (size.width.toFloat() - balloonWidthPx).coerceAtLeast(0f))
                                            val balloonHeightPx = with(density) { 26.dp.toPx() }
                                            val balloonSpacingPx = with(density) { 3.dp.toPx() }
                                            val hasRoomAbove = (fyPx >= balloonHeightPx + balloonSpacingPx + 2f)
                                            val balloonYPx = if (hasRoomAbove) {
                                                fyPx - balloonHeightPx - balloonSpacingPx
                                            } else {
                                                fyPx + fhPx + balloonSpacingPx
                                            }

                                            val inBalloon = currentIsAgeBalloonVisible && (
                                                contentX >= (balloonXPx - 4f) &&
                                                contentX <= (balloonXPx + balloonWidthPx + 4f) &&
                                                contentY >= (balloonYPx - 4f) &&
                                                contentY <= (balloonYPx + balloonHeightPx + 4f)
                                            )

                                            inFaceBox || inBalloon
                                        }

                                        val clickedFace = if (hitFaces.size <= 1) {
                                            hitFaces.firstOrNull()
                                        } else {
                                            hitFaces.minByOrNull { face ->
                                                val dx = normX - face.boundingBox.centerX
                                                val dy = normY - face.boundingBox.centerY
                                                dx * dx + dy * dy
                                            }
                                        }

                                        if (clickedFace != null) {
                                            onFaceClick(clickedFace, activeIndex)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                .pointerInput(activeIndex) {
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

                FaceOverlayCanvas(
                    modifier = Modifier.fillMaxSize(),
                    faces = faces,
                    imageWidth = pixelBuffer.width,
                    imageHeight = pixelBuffer.height,
                    selectedFace = currentPage?.selectedFace ?: state.selectedFace,
                    onFaceClick = { face -> onFaceClick(face, activeIndex) },
                    isScanning = currentPage?.isAnalyzing == true || state.isCameraStreaming,
                    isAgeBalloonVisible = state.isAgeBalloonVisible
                )

                CustomBlurOverlay(
                    modifier = Modifier.fillMaxSize(),
                    customBlurRegions = customBlurs,
                    imageWidth = pixelBuffer.width,
                    imageHeight = pixelBuffer.height,
                    isAddingMode = state.isAddingCustomBlur,
                    onAddRegion = onAddCustomBlur,
                    onUpdateRegion = onUpdateCustomBlur,
                    onRemoveRegion = onRemoveCustomBlur
                )

                if (currentPage?.isAnalyzing == true) {
                    FacebookShimmerViewportOverlay()
                }
            }
        }

        // 2. Floating Top Control Bar (Horizontally scrollable with all viewport controls)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Close Action
            GlassmorphicIconButton(
                icon = "✕",
                onClick = onDismiss,
                size = 34.dp
            )

            // Dynamic Zoom Level Badge
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x3338BDF8))
                    .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                val formattedZoom = ((scale * 10f).roundToInt() / 10f)
                Text(
                    text = "🔍 ${formattedZoom}x",
                    color = AccentBlue,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reset Zoom Action (when zoomed in)
            if (scale > 1.05f) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xCC0F172A))
                        .border(1.dp, GlassSpecularBorderBrushSelected, RoundedCornerShape(999.dp))
                        .clickable {
                            scale = 1f
                            offset = Offset.Zero
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↺ 1x",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Multi-Page Selector
            if (pages.size > 1) {
                Row(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xCC0F172A))
                        .border(1.dp, GlassSpecularBorderBrush, RoundedCornerShape(999.dp))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (activeIndex > 0) {
                        Text(
                            text = "‹",
                            color = AccentBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onPageChange(activeIndex - 1) }
                                .padding(horizontal = 6.dp)
                        )
                    }

                    Text(
                        text = "${activeIndex + 1}/${pages.size}",
                        color = TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (activeIndex < pages.size - 1) {
                        Text(
                            text = "›",
                            color = AccentBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onPageChange(activeIndex + 1) }
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }

            // Rotate Action
            GlassmorphicIconButton(
                icon = "🔄",
                onClick = onRotateClick,
                size = 34.dp
            )

            // Custom Blur Region Button [ ➕ Sansür ]
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = if (state.isAddingCustomBlur) {
                            Brush.verticalGradient(
                                listOf(Color(0xD90284C7), Color(0xB30369A1))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xD90F172A), Color(0xB30F172A))
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (state.isAddingCustomBlur) AccentBlue else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onToggleCustomBlurMode() }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "➕", fontSize = 10.sp)
                Text(
                    text = "Sansür",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isAddingCustomBlur) Color.White else TextSecondary
                )
            }

            // Universal Face Blur Switch [ 🛡️ Bulanık / 👁️ Net ]
            val isBlurActive = state.isBlurAllEnabled || state.isMinorBlurEnabled
            val blurThumbOffset by animateDpAsState(
                targetValue = if (isBlurActive) 13.dp else 2.dp,
                label = "viewer_blur_switch"
            )
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = if (isBlurActive) {
                            Brush.verticalGradient(
                                listOf(MinorColorBg, Color(0xB3064E3B))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xD90F172A), Color(0xB30F172A))
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = if (isBlurActive) {
                            Brush.verticalGradient(
                                listOf(MinorColor.copy(alpha = 0.8f), MinorColor.copy(alpha = 0.3f))
                            )
                        } else {
                            GlassSpecularBorderBrush
                        },
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onToggleBlurClick() }
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (isBlurActive) "🛡️ Bulanık" else "👁️ Net",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBlurActive) MinorColor else TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )

                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(15.dp)
                        .clip(CircleShape)
                        .background(if (isBlurActive) MinorColor else Color(0xFF334155))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = blurThumbOffset, top = 2.dp)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            // Face Bounding Box & Age Balloon Switch [ 🎯 Yaş & Çerçeve ]
            val ageBalloonThumbOffset by animateDpAsState(
                targetValue = if (state.isAgeBalloonVisible) 13.dp else 2.dp,
                label = "viewer_age_balloon_switch"
            )
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = if (state.isAgeBalloonVisible) {
                            Brush.verticalGradient(
                                listOf(Color(0xD90284C7), Color(0xB30369A1))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xD90F172A), Color(0xB30F172A))
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = if (state.isAgeBalloonVisible) {
                            Brush.verticalGradient(
                                listOf(AccentBlue, AccentBlue.copy(alpha = 0.4f))
                            )
                        } else {
                            GlassSpecularBorderBrush
                        },
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable { onToggleAgeBalloonClick() }
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (state.isAgeBalloonVisible) "🎯 Yaş & Çerçeve" else "⚪ Yaş & Çerçeve",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isAgeBalloonVisible) Color.White else TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )

                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(15.dp)
                        .clip(CircleShape)
                        .background(if (state.isAgeBalloonVisible) AccentBlue else Color(0xFF334155))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = ageBalloonThumbOffset, top = 2.dp)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        // 3. Floating Bottom Dynamic Instruction Bar
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
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val hintText = when {
                        state.isAddingCustomBlur -> "➕ Görsele dokunarak sansür alanı ekleyin"
                        scale > 1.05f -> "Sıfırlamak için çift dokunun • Yüze dokunarak sansürleyin"
                        else -> "Yakınlaştırmak için iki parmakla açın veya çift dokunun"
                    }

                    Text(
                        text = hintText,
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (faces.isNotEmpty()) {
                        Text(
                            text = "• ${faces.size} Yüz",
                            color = AccentBlue,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
