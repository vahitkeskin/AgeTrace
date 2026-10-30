package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.CustomBlurRegion
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CustomBlurOverlay(
    modifier: Modifier = Modifier,
    customBlurRegions: List<CustomBlurRegion>,
    imageWidth: Int,
    imageHeight: Int,
    isAddingMode: Boolean,
    onAddRegion: (normX: Float, normY: Float) -> Unit,
    onUpdateRegion: (id: String, normX: Float, normY: Float, normW: Float, normH: Float) -> Unit,
    onRemoveRegion: (id: String) -> Unit
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        if (imageWidth <= 0 || imageHeight <= 0 || containerWidthPx <= 0f || containerHeightPx <= 0f) return@BoxWithConstraints

        val scale = min(containerWidthPx / imageWidth, containerHeightPx / imageHeight)
        val renderedWPx = imageWidth * scale
        val renderedHPx = imageHeight * scale
        val offsetXPx = (containerWidthPx - renderedWPx) / 2f
        val offsetYPx = (containerHeightPx - renderedHPx) / 2f

        // 1. Adding-mode Tap Detector: click anywhere on the image to place a blur region
        if (isAddingMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isAddingMode) {
                        detectTapGestures { offset ->
                            val localX = offset.x - offsetXPx
                            val localY = offset.y - offsetYPx
                            if (localX in 0f..renderedWPx && localY in 0f..renderedHPx) {
                                val normX = (localX / renderedWPx).coerceIn(0.01f, 0.90f)
                                val normY = (localY / renderedHPx).coerceIn(0.01f, 0.90f)
                                onAddRegion(normX, normY)
                            }
                        }
                    }
            )
        }

        // 2. Render and interact with each custom blur region
        for (region in customBlurRegions) {
            val rxPx = offsetXPx + region.normalizedX * renderedWPx
            val ryPx = offsetYPx + region.normalizedY * renderedHPx
            val rwPx = region.normalizedWidth * renderedWPx
            val rhPx = region.normalizedHeight * renderedHPx

            val rwDp = with(density) { rwPx.toDp() }
            val rhDp = with(density) { rhPx.toDp() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(rxPx.roundToInt(), ryPx.roundToInt()) }
                    .size(width = rwDp, height = rhDp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x3338BDF8))
                    .border(
                        width = 1.5.dp,
                        color = AccentBlue,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .pointerInput(region.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val curX = offsetXPx + region.normalizedX * renderedWPx + dragAmount.x
                            val curY = offsetYPx + region.normalizedY * renderedHPx + dragAmount.y

                            val newNormX = ((curX - offsetXPx) / renderedWPx)
                                .coerceIn(0f, (1f - region.normalizedWidth).coerceAtLeast(0f))
                            val newNormY = ((curY - offsetYPx) / renderedHPx)
                                .coerceIn(0f, (1f - region.normalizedHeight).coerceAtLeast(0f))

                            onUpdateRegion(
                                region.id,
                                newNormX,
                                newNormY,
                                region.normalizedWidth,
                                region.normalizedHeight
                            )
                        }
                    }
            ) {
                // Interactive Toolbar on top of region: [ - ] [ + ] [ ✕ ]
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shrink Button [-]
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xD90F172A))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape)
                            .clickable {
                                val newW = (region.normalizedWidth * 0.80f).coerceIn(0.04f, 0.85f)
                                val newH = (region.normalizedHeight * 0.80f).coerceIn(0.04f, 0.85f)
                                onUpdateRegion(region.id, region.normalizedX, region.normalizedY, newW, newH)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "−", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Enlarge Button [+]
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xD90F172A))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape)
                            .clickable {
                                val maxW = 1f - region.normalizedX
                                val maxH = 1f - region.normalizedY
                                val newW = (region.normalizedWidth * 1.25f).coerceIn(0.04f, maxW.coerceAtLeast(0.05f))
                                val newH = (region.normalizedHeight * 1.25f).coerceIn(0.04f, maxH.coerceAtLeast(0.05f))
                                onUpdateRegion(region.id, region.normalizedX, region.normalizedY, newW, newH)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete Button [✕]
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xCCEF4444))
                            .border(1.dp, Color(0x80EF4444), CircleShape)
                            .clickable { onRemoveRegion(region.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Center Move Icon
                Text(
                    text = "✥",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

