package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.data.platform.PlatformImageBridge
import com.vahitkeskin.agetrace.domain.model.BoundingBox
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.AdultColor
import com.vahitkeskin.agetrace.presentation.theme.AdultColorBg
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrushSelected
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicBadge
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.MinorColor
import com.vahitkeskin.agetrace.presentation.theme.MinorColorBg
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary

/**
 * Polite, refined horizontal strip of detected face cards encased in authentic frosted glassmorphic styling.
 */
@Composable
fun DetectedFaceAvatarsStrip(
    modifier: Modifier = Modifier,
    faces: List<DetectedFace>,
    pixelBuffer: PixelBuffer?,
    selectedFace: DetectedFace?,
    blurredFaceIds: Set<String> = emptySet(),
    onFaceClick: (DetectedFace) -> Unit,
    onToggleFaceBlur: (String) -> Unit = {}
) {
    if (faces.isEmpty() || pixelBuffer == null) return

    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderBrush = GlassSpecularBorderBrush
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Count indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AccentBlue)
                    )
                    Text(
                        text = "Tespit Edilen Yüzler",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x1AFFFFFF))
                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(999.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${faces.size} Kişi",
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Horizontal LazyRow of Frosted Face Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 1.dp, vertical = 2.dp)
            ) {
                items(faces, key = { it.id }) { face ->
                    val isSelected = (face.id == selectedFace?.id)
                    val themeColor = if (face.classification.isAdult) AdultColor else MinorColor

                    // Memoize cropped head image bitmap
                    val headBitmap = remember(face.id, pixelBuffer) {
                        try {
                            val box = face.boundingBox
                            val padX = box.normalizedWidth * 0.16f
                            val padTop = box.normalizedHeight * 0.22f
                            val padBottom = box.normalizedHeight * 0.10f

                            val left = (box.normalizedX - padX).coerceIn(0f, 0.96f)
                            val top = (box.normalizedY - padTop).coerceIn(0f, 0.96f)
                            val right = (box.normalizedX + box.normalizedWidth + padX).coerceIn(left + 0.04f, 1f)
                            val bottom = (box.normalizedY + box.normalizedHeight + padBottom).coerceIn(top + 0.04f, 1f)

                            val headBox = BoundingBox(
                                normalizedX = left,
                                normalizedY = top,
                                normalizedWidth = right - left,
                                normalizedHeight = bottom - top
                            )
                            val sub = pixelBuffer.subBuffer(headBox)
                            PlatformImageBridge.pixelBufferToImageBitmap(sub)
                        } catch (t: Throwable) {
                            null
                        }
                    }

                    // Frosted Card for this face
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(122.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = if (isSelected) {
                                    Brush.verticalGradient(
                                        listOf(
                                            AccentBlue.copy(alpha = 0.22f),
                                            Color(0x660F172A)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x38FFFFFF),
                                            Color(0x1A162033),
                                            Color(0x100D1424)
                                        )
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                brush = if (isSelected) GlassSpecularBorderBrushSelected else GlassSpecularBorderBrush,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onFaceClick(face) }
                            .padding(8.dp)
                    ) {
                        // Head Crop Frame
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF090D18))
                                .border(
                                    1.dp,
                                    if (isSelected) AccentBlue.copy(alpha = 0.6f) else Color(0x22FFFFFF),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (headBitmap != null) {
                                Image(
                                    bitmap = headBitmap,
                                    contentDescription = "Yüz",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "👤",
                                    fontSize = 24.sp
                                )
                            }

                            val isBlurred = blurredFaceIds.contains(face.id)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(3.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isBlurred) Color(0xD90F172A) else Color(0x99000000))
                                    .border(1.dp, if (isBlurred) AccentBlue else Color(0x40FFFFFF), CircleShape)
                                    .clickable { onToggleFaceBlur(face.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBlurred) "🛡️" else "👁️",
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        // "%Confidence • Age" Info Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(themeColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "%${face.confidencePercentage} • ${face.estimatedAge} Yaş",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Classification Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (face.classification.isAdult) AdultColorBg else MinorColorBg)
                                .border(
                                    width = 1.dp,
                                    color = if (face.classification.isAdult) AdultColor.copy(alpha = 0.45f) else MinorColor.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(999.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = if (face.classification.isAdult) "Yetişkin" else "Reşit Değil",
                                color = themeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val isBlurred = blurredFaceIds.contains(face.id)
                        // Per-Face Blur Toggle Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isBlurred) Color(0x3338BDF8) else Color(0x1AFFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (isBlurred) AccentBlue.copy(alpha = 0.7f) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(999.dp)
                                )
                                .clickable { onToggleFaceBlur(face.id) }
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isBlurred) "🛡️ Bulanık" else "👁️ Net",
                                color = if (isBlurred) AccentBlue else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
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
}
