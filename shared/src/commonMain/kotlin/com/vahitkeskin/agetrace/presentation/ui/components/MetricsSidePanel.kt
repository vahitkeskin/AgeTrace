package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.AgeClassification
import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.AdultColor
import com.vahitkeskin.agetrace.presentation.theme.AdultColorBg
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassCardBgElevated
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicBadge
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.MinorColor
import com.vahitkeskin.agetrace.presentation.theme.MinorColorBg
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.TextAccent
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary

/**
 * Ultra-refined Glassmorphic Biometric Metrics Panel.
 * Polished, high-legibility layout presenting age, classification, and subtle facial traits.
 */
@Composable
fun MetricsSidePanel(
    modifier: Modifier = Modifier,
    metrics: AnalysisMetrics,
    selectedFace: DetectedFace?
) {
    val estimatedAge = selectedFace?.estimatedAge ?: metrics.primaryAge

    if (estimatedAge == null) {
        // Polite Frosted Placeholder when idle
        GlassmorphicCard(
            modifier = modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            borderBrush = GlassSpecularBorderBrush
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0x1A38BDF8))
                        .border(1.dp, Color(0x3338BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✦", color = AccentBlue, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Biyometrik Analiz",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Görsel seçildiğinde yüz tespiti, tahmini yaş ve anatomik metrikler burada listelenir.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    val confidence = selectedFace?.confidencePercentage ?: metrics.confidencePercentage
    val confFraction = (confidence / 100f).coerceIn(0f, 1f)
    val classification = selectedFace?.classification ?: metrics.primaryClassification ?: AgeClassification.ADULT
    val themeColor = if (classification.isAdult) AdultColor else MinorColor

    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderBrush = GlassSpecularBorderBrush
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Section title & Classification badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "✦", color = AccentBlue, fontSize = 13.sp)
                    Text(
                        text = "Biyometrik Analiz",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }

                GlassmorphicBadge(
                    text = classification.title,
                    accentColor = themeColor
                )
            }

            // Big Age Display Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0x33FFFFFF), Color(0x0FFFFFFF))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(Color(0x38FFFFFF), Color(0x0FFFFFFF))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Tahmini Yaş",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "• ${classification.rangeLabel}",
                                color = TextMuted.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$estimatedAge",
                                color = TextPrimary,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yaşında",
                                color = TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (classification.isAdult) AdultColorBg else MinorColorBg)
                            .border(
                                1.dp,
                                if (classification.isAdult) AdultColor.copy(alpha = 0.4f) else MinorColor.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (classification.isAdult) "18+ Yetişkin" else "Reşit Değil",
                            color = themeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Confidence Rating
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Model Güvenilirlik Oranı",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "%$confidence",
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sleek Gradient Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x1FFFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = confFraction)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                                )
                            )
                    )
                }
            }

            // Subtle Biometric Insights (if selected face has biometrics)
            val biometrics = selectedFace?.biometrics
            if (biometrics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tile 1: Yüz Oranı
                    MiniGlassTile(
                        modifier = Modifier.weight(1f),
                        title = "Kraniofasiyal",
                        value = if (biometrics.craniofacialRatio > 0.85f) "Geniş" else "Dengeli"
                    )

                    // Tile 2: Doku Analizi
                    MiniGlassTile(
                        modifier = Modifier.weight(1f),
                        title = "Cilt Dokusu",
                        value = if (biometrics.wrinkleDensityScore > 0.4f) "Olgun" else "Pürüzsüz"
                    )

                    // Tile 3: Simetri
                    MiniGlassTile(
                        modifier = Modifier.weight(1f),
                        title = "Simetri Skoru",
                        value = "%${(biometrics.bilateralSymmetryScore * 100).toInt()}"
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniGlassTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
