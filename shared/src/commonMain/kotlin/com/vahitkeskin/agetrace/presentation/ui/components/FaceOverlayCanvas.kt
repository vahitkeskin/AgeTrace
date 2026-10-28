package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.presentation.theme.BackgroundDark
import com.vahitkeskin.agetrace.presentation.theme.CyanNeon
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun FaceOverlayCanvas(
    modifier: Modifier = Modifier,
    faces: List<DetectedFace>,
    imageWidth: Int,
    imageHeight: Int,
    selectedFace: DetectedFace?,
    onFaceClick: (DetectedFace) -> Unit,
    isScanning: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_line")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    BoxWithConstraints(modifier = modifier) {
        val containerWidth = maxWidth.value
        val containerHeight = maxHeight.value

        if (imageWidth > 0 && imageHeight > 0 && containerWidth > 0f && containerHeight > 0f) {
            val scale = min(containerWidth / imageWidth, containerHeight / imageHeight)
            val renderedW = imageWidth * scale
            val renderedH = imageHeight * scale
            val offsetX = (containerWidth - renderedW) / 2f
            val offsetY = (containerHeight - renderedH) / 2f
            val highestConfidenceFace = faces.maxByOrNull { it.confidencePercentage }

            // 1. High-Precision Canvas Layer: Red Bounding Box & HUD Corner Brackets
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val currentScale = min(canvasW / imageWidth, canvasH / imageHeight)
                val curRenderedW = imageWidth * currentScale
                val curRenderedH = imageHeight * currentScale
                val curOffX = (canvasW - curRenderedW) / 2f
                val curOffY = (canvasH - curRenderedH) / 2f

                // Laser scanning sweep
                if (isScanning) {
                    val scanY = curOffY + (curRenderedH * scanLineProgress)
                    drawLine(
                        color = NeonRed.copy(alpha = 0.90f),
                        start = Offset(curOffX, scanY),
                        end = Offset(curOffX + curRenderedW, scanY),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = NeonRed.copy(alpha = 0.30f),
                        start = Offset(curOffX, scanY - 8f),
                        end = Offset(curOffX + curRenderedW, scanY - 8f),
                        strokeWidth = 8f
                    )
                }

                // Precision Red Bounding Boxes
                for (face in faces) {
                    val box = face.boundingBox
                    val fx = curOffX + box.normalizedX * curRenderedW
                    val fy = curOffY + box.normalizedY * curRenderedH
                    val fw = box.normalizedWidth * curRenderedW
                    val fh = box.normalizedHeight * curRenderedH

                    val isSelected = (face.id == selectedFace?.id)
                    val isHighestConfidence = (face.id == highestConfidenceFace?.id)
                    val boxColor = if (isSelected) Color(0xFFFF2A4D) else NeonRed
                    val strokeW = if (isSelected) 3.5f else 2.5f

                    // Header Tab attached directly on top of the box (Physical HUD tab)
                    // Only rendered for the face box with the highest confidence percentage
                    if (isHighestConfidence) {
                        val tabHeight = 26f
                        val tabY = if (fy >= tabHeight) fy - tabHeight else fy
                        val tabWidth = min(fw.coerceAtLeast(115f), 155f)

                        // Fill Header Tab
                        drawRect(
                            color = BackgroundDark.copy(alpha = 0.95f),
                            topLeft = Offset(fx, tabY),
                            size = Size(tabWidth, tabHeight)
                        )
                        drawRect(
                            color = boxColor,
                            topLeft = Offset(fx, tabY),
                            size = Size(tabWidth, tabHeight),
                            style = Stroke(width = strokeW)
                        )
                    }

                    // Red Bounding Box (Main outline)
                    drawRect(
                        color = boxColor,
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh),
                        style = Stroke(width = strokeW)
                    )

                    // Translucent interior red tint
                    drawRect(
                        color = boxColor.copy(alpha = if (isSelected) 0.16f else 0.08f),
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh)
                    )

                    // Thick Cyber Corner Reticles (Military HUD aesthetic)
                    val cornerLen = min(fw, fh) * 0.22f
                    val bracketWidth = 5.0f

                    // Top-Left
                    drawLine(boxColor, Offset(fx, fy), Offset(fx + cornerLen, fy), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx, fy), Offset(fx, fy + cornerLen), strokeWidth = bracketWidth)

                    // Top-Right
                    drawLine(boxColor, Offset(fx + fw, fy), Offset(fx + fw - cornerLen, fy), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx + fw, fy), Offset(fx + fw, fy + cornerLen), strokeWidth = bracketWidth)

                    // Bottom-Left
                    drawLine(boxColor, Offset(fx, fy + fh), Offset(fx + cornerLen, fy + fh), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx, fy + fh), Offset(fx, fy + fh - cornerLen), strokeWidth = bracketWidth)

                    // Bottom-Right
                    drawLine(boxColor, Offset(fx + fw, fy + fh), Offset(fx + fw - cornerLen, fy + fh), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx + fw, fy + fh), Offset(fx + fw, fy + cornerLen), strokeWidth = bracketWidth)

                    // Center Crosshair
                    val cx = fx + fw / 2f
                    val cy = fy + fh / 2f
                    val chLen = 6f
                    drawLine(boxColor.copy(alpha = 0.7f), Offset(cx - chLen, cy), Offset(cx + chLen, cy), strokeWidth = 2f)
                    drawLine(boxColor.copy(alpha = 0.7f), Offset(cx, cy - chLen), Offset(cx, cy + chLen), strokeWidth = 2f)
                }
            }

            // 2. High-Contrast Interactive Badge Displayed ONLY on the Highest Percentage Box
            val density = androidx.compose.ui.platform.LocalDensity.current
            for (face in faces) {
                val box = face.boundingBox
                val fx = offsetX + box.normalizedX * renderedW
                val fy = offsetY + box.normalizedY * renderedH
                val fw = box.normalizedWidth * renderedW
                val fh = box.normalizedHeight * renderedH
                val isSelected = (face.id == selectedFace?.id)
                val isHighestConfidence = (face.id == highestConfidenceFace?.id)

                // Interactive Face Box Area (Click anywhere on face to select)
                val fwDp = with(density) { fw.toDp() }
                val fhDp = with(density) { fh.toDp() }
                if (fwDp > 10.dp && fhDp > 10.dp) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(fx.roundToInt(), fy.roundToInt()) }
                            .size(width = fwDp, height = fhDp)
                            .clickable { onFaceClick(face) }
                    )
                }

                // Dedicated Header Badge: Only displayed on top of the box with the highest confidence percentage
                if (isHighestConfidence) {
                    val badgeHeightPx = 26f
                    val badgeY = if (fy >= badgeHeightPx) {
                        (fy - badgeHeightPx).roundToInt()
                    } else {
                        fy.roundToInt()
                    }

                    val badgeX = fx.coerceIn(0f, (containerWidth - 120f).coerceAtLeast(0f)).roundToInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(badgeX, badgeY) }
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(BackgroundDark.copy(alpha = 0.95f))
                            .border(
                                width = if (isSelected) 2.dp else 1.5.dp,
                                color = if (isSelected) Color(0xFFFF2A4D) else NeonRed,
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                            )
                            .clickable { onFaceClick(face) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sensor LED
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFFF3366) else NeonRed)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Net Yüzdelik Dilimi: %87
                            Text(
                                text = "%${face.confidencePercentage}",
                                color = if (isSelected) Color(0xFFFF3366) else NeonRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )

                            Text(
                                text = " - ",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Tahmin Edilen Yaş: 32 Age
                            Text(
                                text = "${face.estimatedAge} Age",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

