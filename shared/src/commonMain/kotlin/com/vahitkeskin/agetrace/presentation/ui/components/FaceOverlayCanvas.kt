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
import com.vahitkeskin.agetrace.presentation.theme.*
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

            // 1. High-Precision Canvas Layer: Clean Rounded Bounding Boxes & Biometric Corner Accents
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val currentScale = min(canvasW / imageWidth, canvasH / imageHeight)
                val curRenderedW = imageWidth * currentScale
                val curRenderedH = imageHeight * currentScale
                val curOffX = (canvasW - curRenderedW) / 2f
                val curOffY = (canvasH - curRenderedH) / 2f

                // Subtle ambient scanning line (Smooth, non-intrusive)
                if (isScanning) {
                    val scanY = curOffY + (curRenderedH * scanLineProgress)
                    drawLine(
                        color = Color(0x6638BDF8),
                        start = Offset(curOffX, scanY),
                        end = Offset(curOffX + curRenderedW, scanY),
                        strokeWidth = 2f
                    )
                }

                // Precision Bounding Boxes
                for (face in faces) {
                    val box = face.boundingBox
                    val fx = curOffX + box.normalizedX * curRenderedW
                    val fy = curOffY + box.normalizedY * curRenderedH
                    val fw = box.normalizedWidth * curRenderedW
                    val fh = box.normalizedHeight * curRenderedH

                    val isSelected = (face.id == selectedFace?.id)
                    val frameColor = if (isSelected) NeonRed else Color(0xFF38BDF8)
                    val strokeW = if (isSelected) 2.2f else 1.5f

                    // Subtle, realistic rounded bounding box (Apple Vision & Google Lens style)
                    drawRoundRect(
                        color = frameColor,
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = strokeW)
                    )

                    // Minimal, soft interior tint
                    drawRoundRect(
                        color = frameColor.copy(alpha = if (isSelected) 0.08f else 0.03f),
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )

                    // Subtle Corner Focus Brackets (Refined, elegant biometric markers)
                    val cornerLen = min(fw, fh) * 0.16f
                    val bracketW = strokeW + 1.2f

                    // Top-Left
                    drawLine(frameColor, Offset(fx, fy + cornerLen), Offset(fx, fy + 4f), strokeWidth = bracketW)
                    drawLine(frameColor, Offset(fx + 4f, fy), Offset(fx + cornerLen, fy), strokeWidth = bracketW)

                    // Top-Right
                    drawLine(frameColor, Offset(fx + fw, fy + cornerLen), Offset(fx + fw, fy + 4f), strokeWidth = bracketW)
                    drawLine(frameColor, Offset(fx + fw - cornerLen, fy), Offset(fx + fw - 4f, fy), strokeWidth = bracketW)

                    // Bottom-Left
                    drawLine(frameColor, Offset(fx, fy + fh - cornerLen), Offset(fx, fy + fh - 4f), strokeWidth = bracketW)
                    drawLine(frameColor, Offset(fx + 4f, fy + fh), Offset(fx + cornerLen, fy + fh), strokeWidth = bracketW)

                    // Bottom-Right
                    drawLine(frameColor, Offset(fx + fw, fy + fh - cornerLen), Offset(fx + fw, fy + fh - 4f), strokeWidth = bracketW)
                    drawLine(frameColor, Offset(fx + fw - cornerLen, fy + fh), Offset(fx + fw - 4f, fy + fh), strokeWidth = bracketW)
                }
            }

            // 2. High-Precision Frosted Pill Badge (Clean, realistic, floating gracefully only on highest percentage face)
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

                // Dedicated Frosted Pill Badge: Only displayed on top of the box with the highest confidence percentage
                if (isHighestConfidence) {
                    val badgeHeightPx = 28f
                    val badgeY = if (fy >= badgeHeightPx + 6f) {
                        (fy - badgeHeightPx - 6f).roundToInt()
                    } else {
                        (fy + 6f).roundToInt()
                    }

                    val badgeX = fx.coerceIn(4f, (containerWidth - 130f).coerceAtLeast(4f)).roundToInt()
                    val badgeBorderColor = if (isSelected) NeonRed.copy(alpha = 0.8f) else Color(0x33FFFFFF)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(badgeX, badgeY) }
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xE6101724))
                            .border(
                                width = 1.dp,
                                color = badgeBorderColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onFaceClick(face) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtle Status Indicator LED
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NeonRed else Color(0xFF10B981))
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Yüzdelik Oranı: %87
                            Text(
                                text = "%${face.confidencePercentage}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )

                            Text(
                                text = " • ",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )

                            // Tahmin Edilen Yaş: 32 Yaş
                            Text(
                                text = "${face.estimatedAge} Yaş",
                                color = if (isSelected) NeonRed else Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


