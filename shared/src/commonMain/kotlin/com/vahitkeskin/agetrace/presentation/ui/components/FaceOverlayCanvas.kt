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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
            animation = tween(durationMillis = 2200, easing = LinearEasing),
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

            // 1. High-Precision Canvas Layer: Red Bounding Box & HUD Corner Brackets
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val currentScale = min(canvasW / imageWidth, canvasH / imageHeight)
                val curRenderedW = imageWidth * currentScale
                val curRenderedH = imageHeight * currentScale
                val curOffX = (canvasW - curRenderedW) / 2f
                val curOffY = (canvasH - curRenderedH) / 2f

                // Sweeping laser scanner line if active
                if (isScanning) {
                    val scanY = curOffY + (curRenderedH * scanLineProgress)
                    drawLine(
                        color = NeonRed.copy(alpha = 0.85f),
                        start = Offset(curOffX, scanY),
                        end = Offset(curOffX + curRenderedW, scanY),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = NeonRed.copy(alpha = 0.25f),
                        start = Offset(curOffX, scanY - 6f),
                        end = Offset(curOffX + curRenderedW, scanY - 6f),
                        strokeWidth = 6f
                    )
                }

                // Draw bounding boxes for detected faces
                for (face in faces) {
                    val box = face.boundingBox
                    val fx = curOffX + box.normalizedX * curRenderedW
                    val fy = curOffY + box.normalizedY * curRenderedH
                    val fw = box.normalizedWidth * curRenderedW
                    val fh = box.normalizedHeight * curRenderedH

                    val isSelected = (face.id == selectedFace?.id)
                    val boxColor = NeonRed
                    val strokeW = if (isSelected) 3.0f else 2.0f

                    // Red Bounding Box (Main rectangle)
                    drawRect(
                        color = boxColor,
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh),
                        style = Stroke(width = strokeW)
                    )

                    // Translucent red fill
                    drawRect(
                        color = boxColor.copy(alpha = if (isSelected) 0.14f else 0.08f),
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh)
                    )

                    // Cyber Corner Brackets (Thicker high-tech corner accents)
                    val cornerLen = min(fw, fh) * 0.18f
                    val bracketWidth = 4.0f

                    // Top-Left corner
                    drawLine(boxColor, Offset(fx, fy), Offset(fx + cornerLen, fy), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx, fy), Offset(fx, fy + cornerLen), strokeWidth = bracketWidth)

                    // Top-Right corner
                    drawLine(boxColor, Offset(fx + fw, fy), Offset(fx + fw - cornerLen, fy), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx + fw, fy), Offset(fx + fw, fy + cornerLen), strokeWidth = bracketWidth)

                    // Bottom-Left corner
                    drawLine(boxColor, Offset(fx, fy + fh), Offset(fx + cornerLen, fy + fh), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx, fy + fh), Offset(fx, fy + fh - cornerLen), strokeWidth = bracketWidth)

                    // Bottom-Right corner
                    drawLine(boxColor, Offset(fx + fw, fy + fh), Offset(fx + fw - cornerLen, fy + fh), strokeWidth = bracketWidth)
                    drawLine(boxColor, Offset(fx + fw, fy + fh), Offset(fx + fw, fy + cornerLen), strokeWidth = bracketWidth)

                    // Small center crosshair
                    val cx = fx + fw / 2f
                    val cy = fy + fh / 2f
                    val chLen = 5f
                    drawLine(boxColor.copy(alpha = 0.5f), Offset(cx - chLen, cy), Offset(cx + chLen, cy), strokeWidth = 1.5f)
                    drawLine(boxColor.copy(alpha = 0.5f), Offset(cx, cy - chLen), Offset(cx, cy + chLen), strokeWidth = 1.5f)
                }
            }

            // 2. Interactive Badges Layer directly above the top edge (with smart collision avoidance)
            for (face in faces) {
                val box = face.boundingBox
                val fx = offsetX + box.normalizedX * renderedW
                val fy = offsetY + box.normalizedY * renderedH
                val fw = box.normalizedWidth * renderedW

                // Non-overlapping smart placement:
                // If the face is near the very top of the container, render badge right inside the top border
                val badgeY = if (fy < 45f) {
                    (fy + 8f).roundToInt()
                } else {
                    (fy - 38f).roundToInt()
                }

                val badgeX = fx.coerceIn(8f, (containerWidth - 150f).coerceAtLeast(8f)).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(badgeX, badgeY) }
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundDark.copy(alpha = 0.94f))
                        .border(1.dp, NeonRed, RoundedCornerShape(8.dp))
                        .clickable { onFaceClick(face) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing red indicator
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(NeonRed)
                        )
                        // Exact Label Format: "%87 - 32 Age"
                        Text(
                            text = " ${face.badgeLabel}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
