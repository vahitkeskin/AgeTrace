package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.presentation.theme.CyanNeon
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary

@Composable
fun FileUploadDropZone(
    modifier: Modifier = Modifier,
    sampleFaces: List<ImageSource.PreloadedSample>,
    currentImageSource: ImageSource?,
    onPickFileClick: () -> Unit,
    onSelectSample: (ImageSource.PreloadedSample) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Native Gallery / File System Trigger Card
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onPickFileClick() },
            cornerRadius = 16.dp,
            backgroundColor = SurfaceCard.copy(alpha = 0.92f),
            borderColor = NeonRed.copy(alpha = 0.55f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NeonRed.copy(alpha = 0.20f))
                            .border(1.dp, NeonRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🖼️",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Galeriden veya Dosyalardan Seç",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Android Galeri • iOS Fotoğraflar • Masaüstü Dosyaları",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonRed)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Fotoğraf Seç",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Curated Benchmark Gallery with horizontal scrolling for non-overlapping mobile UX
        if (sampleFaces.isNotEmpty()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BİYOMETRİK REFERANS SETİ",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "5 Farklı Yaş Grubu",
                        color = CyanNeon,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleFaces.forEach { sample ->
                        val isSelected = (currentImageSource is ImageSource.PreloadedSample && currentImageSource.id == sample.id)
                        val borderColor = if (isSelected) NeonRed else GlassBorder
                        val bgColor = if (isSelected) NeonRed.copy(alpha = 0.16f) else SurfaceCard.copy(alpha = 0.60f)

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable { onSelectSample(sample) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = sample.title.substringBefore(" ("),
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = sample.expectedAgeBracket,
                                    color = if (isSelected) NeonRed else TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
