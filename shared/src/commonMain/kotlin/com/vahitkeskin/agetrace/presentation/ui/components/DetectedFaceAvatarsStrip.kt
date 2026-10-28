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

/**
 * Prominent horizontal strip displaying cropped head portraits of all detected people
 * with their exact "%Confidence - Age" label immediately below each avatar.
 */
@Composable
fun DetectedFaceAvatarsStrip(
    modifier: Modifier = Modifier,
    faces: List<DetectedFace>,
    pixelBuffer: PixelBuffer?,
    selectedFace: DetectedFace?,
    onFaceClick: (DetectedFace) -> Unit
) {
    if (faces.isEmpty() || pixelBuffer == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A).copy(alpha = 0.95f),
                        Color(0xFF1E293B).copy(alpha = 0.85f)
                    )
                )
            )
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
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
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
                Text(
                    text = "Tespit Edilen Kişiler (${faces.size})",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }

            Text(
                text = "Seçmek için dokunun",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        // Horizontal Row of Head Avatars with Age Info
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
        ) {
            items(faces, key = { it.id }) { face ->
                val isSelected = (face.id == selectedFace?.id)
                val themeColor = if (face.classification.isAdult) Color(0xFFEF4444) else Color(0xFF10B981)

                // Memoize cropped head image bitmap safely
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
                    } catch (e: Exception) {
                        null
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(120.dp)
                        .shadow(
                            elevation = if (isSelected) 8.dp else 2.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = if (isSelected) themeColor else Color.Black
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) themeColor.copy(alpha = 0.16f) else Color(0xFF0F172A).copy(alpha = 0.7f)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) themeColor else Color(0xFF334155),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onFaceClick(face) }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    // Head Crop Avatar Frame
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B1120))
                            .border(1.dp, if (isSelected) themeColor.copy(alpha = 0.6f) else Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (headBitmap != null) {
                            Image(
                                bitmap = headBitmap,
                                contentDescription = "Kişi Yüzü",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "👤",
                                fontSize = 28.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    // "%Blabla - blabla Age" Label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(themeColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%${face.confidencePercentage} - ${face.estimatedAge} Age",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Minor / Adult Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(themeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = if (face.classification.isAdult) "Yetişkin" else "Reşit Değil",
                            color = themeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
