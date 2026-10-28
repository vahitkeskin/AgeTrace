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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.AgeClassification
import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.GreenSuccess
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun MetricsSidePanel(
    modifier: Modifier = Modifier,
    metrics: AnalysisMetrics,
    selectedFace: DetectedFace?
) {
    val confidence = selectedFace?.confidencePercentage ?: metrics.confidencePercentage
    val confFraction = (confidence / 100f).coerceIn(0f, 1f)
    val classification = selectedFace?.classification ?: metrics.primaryClassification ?: AgeClassification.ADULT
    val estimatedAge = selectedFace?.estimatedAge ?: metrics.primaryAge

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Primary Intelligence Result: Estimated Age & Confidence Rating
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            backgroundColor = SurfaceCard.copy(alpha = 0.90f),
            borderColor = GlassBorder
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BİYOLOJİK YAŞ ANALİZİ",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    // Classification Pill Badge (e.g., Yetişkin, Çocuk, Kıdemli)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x1810B981))
                            .border(1.dp, Color(0x3310B981), RoundedCornerShape(20.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = classification.title,
                            color = GreenSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Age & Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = estimatedAge?.toString() ?: "--",
                            color = TextPrimary,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (estimatedAge != null) "Yaşında" else "Fotoğraf Bekleniyor",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Kohort",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = classification.rangeLabel,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Confidence Accuracy Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Model Doğruluk Oranı",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "%$confidence",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { confFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF38BDF8),
                    trackColor = Color(0x22FFFFFF)
                )
            }
        }

        // 2. Biometric Feature Breakdown (Computer Vision Metrics)
        if (selectedFace != null) {
            val bio = selectedFace.biometrics
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                backgroundColor = SurfaceCard.copy(alpha = 0.90f),
                borderColor = GlassBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BİYOMETRİK ÖZNİTELİK VERİLERİ",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MetricRow(
                        label = "Kraniofasiyal Yüz Oranı",
                        value = "${(bio.craniofacialRatio * 100).roundToInt() / 100f}x"
                    )
                    MetricRow(
                        label = "Alın Kırışıklık İndeksi",
                        value = "%${(bio.wrinkleDensityScore * 100).roundToInt()}"
                    )
                    MetricRow(
                        label = "Cilt Doku Varyansı",
                        value = "%${(bio.skinTextureVariance * 100).roundToInt()}"
                    )
                    MetricRow(
                        label = "Bilateral Yüz Simetrisi",
                        value = "%${(bio.bilateralSymmetryScore * 100).roundToInt()}"
                    )
                }
            }
        }

        // 3. System & Hardware Telemetry Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            backgroundColor = SurfaceCard.copy(alpha = 0.90f),
            borderColor = GlassBorder
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "İŞLEM MOTORU VE TELEMETRİ",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                MetricRow(
                    label = "Aktif Motor",
                    value = metrics.engineName
                )
                MetricRow(
                    label = "Çıkarım Süresi (Latency)",
                    value = "${metrics.inferenceTimeMs} ms"
                )
                MetricRow(
                    label = "Görsel Çözünürlüğü",
                    value = metrics.imageResolution
                )
                MetricRow(
                    label = "Tespit Edilen Yüz",
                    value = "${metrics.facesFoundCount} Kişi"
                )
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

