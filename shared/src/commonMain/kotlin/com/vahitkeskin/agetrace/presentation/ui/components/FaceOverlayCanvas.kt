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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
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
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        if (imageWidth > 0 && imageHeight > 0 && containerWidthPx > 0f && containerHeightPx > 0f) {
            val scale = min(containerWidthPx / imageWidth, containerHeightPx / imageHeight)
            val renderedWPx = imageWidth * scale
            val renderedHPx = imageHeight * scale
            val offsetXPx = (containerWidthPx - renderedWPx) / 2f
            val offsetYPx = (containerHeightPx - renderedHPx) / 2f
            val highestConfidenceFace = faces.maxByOrNull { it.confidencePercentage }

            // 1. Canvas Layer: 4 Smooth Rounded Corner Brackets & Dynamic Scan Beam
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = with(density) { 2.6.dp.toPx() }

                for (face in faces) {
                    val box = face.boundingBox
                    val fx = offsetXPx + box.normalizedX * renderedWPx
                    val fy = offsetYPx + box.normalizedY * renderedHPx
                    val fw = box.normalizedWidth * renderedWPx
                    val fh = box.normalizedHeight * renderedHPx

                    val isSelected = (face.id == selectedFace?.id)
                    val bracketColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.95f)

                    // 4 Smooth Rounded Corner Focus Brackets
                    val minDim = min(fw, fh).coerceAtLeast(10f)
                    val maxRadius = (minDim * 0.28f).coerceAtLeast(4f)
                    val radius = (minDim * 0.14f).coerceIn(4f, min(24f, maxRadius))
                    val minArm = radius + 2f
                    val maxArm = (minDim * 0.45f).coerceAtLeast(minArm + 2f)
                    val armLen = (radius + minDim * 0.12f).coerceIn(minArm, maxArm)

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

            // 2. Interactive Face Area & Prominent Floating Badge on the Right (Pixel-Synchronized)
            for (face in faces) {
                val box = face.boundingBox
                val fxPx = offsetXPx + box.normalizedX * renderedWPx
                val fyPx = offsetYPx + box.normalizedY * renderedHPx
                val fwPx = box.normalizedWidth * renderedWPx
                val fhPx = box.normalizedHeight * renderedHPx

                val isHighestConfidence = (face.id == highestConfidenceFace?.id)

                // Clickable Face Area (Using exact pixel offsets)
                val fwDp = with(density) { fwPx.toDp() }
                val fhDp = with(density) { fhPx.toDp() }
                if (fwDp > 10.dp && fhDp > 10.dp) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(fxPx.roundToInt(), fyPx.roundToInt()) }
                            .size(width = fwDp, height = fhDp)
                            .clickable { onFaceClick(face) }
                    )
                }

                // Dedicated High-Contrast Floating Badge placed directly ABOVE the face box
                // Constrained strictly to NOT exceed the box width (fwDp)
                val badgeHeightDp = 22.dp
                val badgeHeightPx = with(density) { badgeHeightDp.toPx() }
                val isSelected = (face.id == selectedFace?.id)
                val themeColor = if (face.classification.isAdult) Color(0xFFEF4444) else Color(0xFF10B981)

                // Position: Directly above the face box if room permits; otherwise just inside the top
                val badgeYPx = if (fyPx >= badgeHeightPx + 4f) {
                    fyPx - badgeHeightPx - 4f
                } else {
                    fyPx + 4f
                }
                val badgeXPx = fxPx.coerceIn(0f, (containerWidthPx - fwPx).coerceAtLeast(0f))

                val labelText = if (fwDp < 38.dp) {
                    "${face.estimatedAge}"
                } else if (fwDp < 52.dp) {
                    "%${face.confidencePercentage} • ${face.estimatedAge}"
                } else if (fwDp < 80.dp) {
                    "%${face.confidencePercentage} - ${face.estimatedAge}"
                } else {
                    "%${face.confidencePercentage} - ${face.estimatedAge} Age"
                }

                val fontSize = if (fwDp < 45.dp) 8.sp else if (fwDp < 60.dp) 9.sp else if (fwDp < 80.dp) 10.sp else 11.sp

                Box(
                    modifier = Modifier
                        .offset { IntOffset(badgeXPx.roundToInt(), badgeYPx.roundToInt()) }
                        .width(fwDp)
                        .height(badgeHeightDp)
                        .shadow(
                            elevation = if (isSelected) 8.dp else 3.dp,
                            shape = RoundedCornerShape(6.dp),
                            spotColor = if (isSelected) themeColor else Color.Black
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xF00F172A))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color.White else themeColor.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onFaceClick(face) }
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(themeColor)
                        )
                        Spacer(modifier = Modifier.width(2.5.dp))
                        Text(
                            text = labelText,
                            color = Color.White,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
