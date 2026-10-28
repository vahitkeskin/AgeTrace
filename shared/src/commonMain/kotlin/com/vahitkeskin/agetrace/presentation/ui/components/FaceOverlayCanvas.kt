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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.presentation.theme.*
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * High-Accuracy Biometric Face Overlay.
 * Renders 4 smooth rounded-corner camera focus brackets (pure crisp white)
 * with a high-contrast floating badge positioned gracefully on the right edge
 * formatted as: "%96 - 25 Age".
 */
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
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    val density = LocalDensity.current

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

            // 1. Canvas Layer: 4 Smooth Rounded Corner Brackets & Dynamic Scan Beam
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val curScale = min(canvasW / imageWidth, canvasH / imageHeight)
                val curW = imageWidth * curScale
                val curH = imageHeight * curScale
                val curOffX = (canvasW - curW) / 2f
                val curOffY = (canvasH - curH) / 2f

                val strokeW = with(density) { 2.6.dp.toPx() }

                for (face in faces) {
                    val box = face.boundingBox
                    val fx = curOffX + box.normalizedX * curW
                    val fy = curOffY + box.normalizedY * curH
                    val fw = box.normalizedWidth * curW
                    val fh = box.normalizedHeight * curH

                    val isSelected = (face.id == selectedFace?.id)
                    val bracketColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.95f)

                    // 4 Smooth Rounded Corner Focus Brackets
                    val radius = (min(fw, fh) * 0.16f).coerceIn(16f, 32f)
                    val armLen = (radius + min(fw, fh) * 0.12f).coerceIn(radius + 8f, min(fw, fh) * 0.32f)

                    val strokeStyle = Stroke(
                        width = strokeW,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )

                    // 1. Top-Left Corner Bracket
                    val tlPath = Path().apply {
                        moveTo(fx, fy + armLen)
                        lineTo(fx, fy + radius)
                        arcTo(
                            rect = Rect(fx, fy, fx + 2f * radius, fy + 2f * radius),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(fx + armLen, fy)
                    }
                    drawPath(tlPath, bracketColor, style = strokeStyle)

                    // 2. Top-Right Corner Bracket
                    val trPath = Path().apply {
                        moveTo(fx + fw - armLen, fy)
                        lineTo(fx + fw - radius, fy)
                        arcTo(
                            rect = Rect(fx + fw - 2f * radius, fy, fx + fw, fy + 2f * radius),
                            startAngleDegrees = 270f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(fx + fw, fy + armLen)
                    }
                    drawPath(trPath, bracketColor, style = strokeStyle)

                    // 3. Bottom-Right Corner Bracket
                    val brPath = Path().apply {
                        moveTo(fx + fw, fy + fh - armLen)
                        lineTo(fx + fw, fy + fh - radius)
                        arcTo(
                            rect = Rect(fx + fw - 2f * radius, fy + fh - 2f * radius, fx + fw, fy + fh),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(fx + fw - armLen, fy + fh)
                    }
                    drawPath(brPath, bracketColor, style = strokeStyle)

                    // 4. Bottom-Left Corner Bracket
                    val blPath = Path().apply {
                        moveTo(fx + armLen, fy + fh)
                        lineTo(fx + radius, fy + fh)
                        arcTo(
                            rect = Rect(fx, fy + fh - 2f * radius, fx + 2f * radius, fy + fh),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(fx, fy + fh - armLen)
                    }
                    drawPath(blPath, bracketColor, style = strokeStyle)

                    // Active Scanning Beam (Moving smoothly across face area)
                    if (isScanning) {
                        val scanY = fy + (fh * scanProgress)
                        // Gradient glow under scan line
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x0038BDF8), Color(0x2838BDF8), Color(0x0038BDF8)),
                                startY = scanY - 30f,
                                endY = scanY + 10f
                            ),
                            topLeft = Offset(fx, scanY - 30f),
                            size = Size(fw, 40f)
                        )
                        // Laser scan line
                        drawLine(
                            color = Color(0xE6FFFFFF),
                            start = Offset(fx + 4f, scanY),
                            end = Offset(fx + fw - 4f, scanY),
                            strokeWidth = 2.2f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 2. Interactive Face Area & Prominent Floating Badge on the Right
            for (face in faces) {
                val box = face.boundingBox
                val fx = offsetX + box.normalizedX * renderedW
                val fy = offsetY + box.normalizedY * renderedH
                val fw = box.normalizedWidth * renderedW
                val fh = box.normalizedHeight * renderedH

                val isHighestConfidence = (face.id == highestConfidenceFace?.id)

                // Clickable Face Area
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

                // Dedicated High-Contrast Floating Badge on the Right Side
                if (isHighestConfidence) {
                    // Position badge on the right side of the face box (or clamped inside viewport)
                    val preferredBadgeX = fx + fw - 8f
                    val badgeX = if (preferredBadgeX + 130f <= containerWidth) {
                        preferredBadgeX
                    } else {
                        (fx + fw - 135f).coerceAtLeast(4f)
                    }
                    val badgeY = (fy + fh * 0.40f).coerceIn(8f, (containerHeight - 44f).coerceAtLeast(8f))

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(badgeX.roundToInt(), badgeY.roundToInt()) }
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color(0x55000000),
                                ambientColor = Color(0x33000000)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color(0x18000000),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onFaceClick(face) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Text: "%96 - 25 Age"
                            Text(
                                text = "%${face.confidencePercentage} - ${face.estimatedAge} Age",
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
