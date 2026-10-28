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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.AgeClassification
import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.presentation.theme.AmberWarning
import com.vahitkeskin.agetrace.presentation.theme.CyanNeon
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Primary Live Age & Confidence Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CANLI METRİKLER",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    // Edge AI status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2000F5D4))
                            .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "EDGE AI • AKTİF",
                            color = CyanNeon,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Age Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tahmin Edilen Yaş",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = selectedFace?.estimatedAge?.toString() ?: "--",
                                color = TextPrimary,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Yaş",
                                color = NeonRed,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    // Yaş Sınır Durumu (18+ Adult veya 18- Minor)
                    val classification = selectedFace?.classification ?: AgeClassification.ADULT
                    val isAdult = classification.isAdult
                    val badgeBg = if (isAdult) Color(0x2210B981) else Color(0x2200F5D4)
                    val badgeBorder = if (isAdult) GreenSuccess else CyanNeon

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Yaş Sınır Durumu",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .border(1.dp, badgeBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = classification.title, // e.g. "18+ Adult" or "18- Minor"
                                color = badgeBorder,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Model Güven Oranı (Confidence Gauge)
                val confidence = selectedFace?.confidencePercentage ?: metrics.confidencePercentage
                val confFloat = (confidence / 100f).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Model Doğruluk Oranı (Güven)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "%$confidence",
                        color = NeonRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { confFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonRed,
                    trackColor = SurfaceCard
                )
            }
        }

        // 2. Performance & Hardware Telemetry
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "İŞLEM PERFORMANSI",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                    value = "${metrics.facesFoundCount} Adet"
                )
                MetricRow(
                    label = "İşlem Motoru",
                    value = "Edge AI Vision Core"
                )
            }
        }

        // 3. Biometric Feature Breakdown (Anthropometrics & Texture)
        if (selectedFace != null) {
            val bio = selectedFace.biometrics
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BİYOMETRİK ÖZELLİK ANALİZİ",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    MetricRow(
                        label = "Kraniofasiyal Oran",
                        value = "${(bio.craniofacialRatio * 100).roundToInt() / 100f}x"
                    )
                    MetricRow(
                        label = "Alın Kırışıklık İndeksi",
                        value = "%${(bio.wrinkleDensityScore * 100).roundToInt()}"
                    )
                    MetricRow(
                        label = "Cilt Dokusu Varyansı",
                        value = "${(bio.skinTextureVariance * 100).roundToInt()}%"
                    )
                    MetricRow(
                        label = "Bilateral Yüz Simetrisi",
                        value = "%${(bio.bilateralSymmetryScore * 100).roundToInt()}"
                    )
                }
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
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
