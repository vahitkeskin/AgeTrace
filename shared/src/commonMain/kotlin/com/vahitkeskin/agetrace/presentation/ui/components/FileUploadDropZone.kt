package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicPill
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary

/**
 * Polite, refined glassmorphic upload bar with preset sample face chips.
 */
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Primary Frosted Glass Upload Bar
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onPickFileClick() },
            cornerRadius = 16.dp,
            borderBrush = GlassSpecularBorderBrush
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🖼️",
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Fotoğraf Seç veya Yükle",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• Galeri",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // 2. Preset Sample Portrait Chips
        if (sampleFaces.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleFaces.forEach { sample ->
                    val isSelected = (currentImageSource is ImageSource.PreloadedSample && currentImageSource.id == sample.id)
                    val cleanTitle = sample.title.substringBefore(" -").substringBefore(" (")

                    GlassmorphicPill(
                        isSelected = isSelected,
                        selectedColor = AccentBlue,
                        onClick = { onSelectSample(sample) },
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when (sample.id) {
                                    "sample_child" -> "👶"
                                    "sample_young_adult" -> "🧑"
                                    "sample_adult" -> "👨"
                                    "sample_mature" -> "🧔"
                                    "sample_senior" -> "👴"
                                    else -> "👥"
                                },
                                fontSize = 12.sp
                            )
                            Text(
                                text = cleanTitle,
                                color = if (isSelected) AccentBlue else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
