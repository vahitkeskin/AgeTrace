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
    val confidence = selectedFace?.confidencePercentage ?: metrics.confidencePercentage
    val confFraction = (confidence / 100f).coerceIn(0f, 1f)
    val classification = selectedFace?.classification ?: metrics.primaryClassification ?: AgeClassification.ADULT
    val isAdult = classification.isAdult

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Prominent Accuracy & Confidence Percentage Card (NET YÜZDELİK DİLİMİ)
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            borderColor = NeonRed.copy(alpha = 0.40f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DOĞRULUK & GÜVEN YÜZDESİ",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x20FF1744))
                            .border(1.dp, NeonRed.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (confidence >= 80) "YÜKSEK GÜVEN" else "STANDART",
                            color = NeonRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Net, Büyük ve Belirgin Yüzdelik Değer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%$confidence",
                            color = NeonRed,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Model Doğruluğu",
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2210B981))
                            .border(1.dp, GreenSuccess, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = classification.title,
                            color = GreenSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Confidence Progress Bar
                LinearProgressIndicator(
                    progress = { confFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = NeonRed,
                    trackColor = Color(0x33333333)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("%0 Min", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("%50 Nominal", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("%100 Peak", color = NeonRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // 2. Tahmin Edilen Yaş ve Biyometrik Kohort
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TAHMİN EDİLEN BİYOLOJİK YAŞ",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = selectedFace?.estimatedAge?.toString() ?: metrics.primaryAge?.toString() ?: "--",
                            color = TextPrimary,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yaşında",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Yaş Grubu",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = classification.rangeLabel,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 3. Biometric Feature Breakdown (Gerçek Bilgisayarlı Görü Verileri)
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
                        label = "Cilt Doku Pürüzlülüğü",
                        value = "%${(bio.skinTextureVariance * 100).roundToInt()}"
                    )
                    MetricRow(
                        label = "Bilateral Yüz Simetrisi",
                        value = "%${(bio.bilateralSymmetryScore * 100).roundToInt()}"
                    )
                }
            }
        }

        // 4. Donanım & Çıkarım Performansı
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "İŞLEM MOTORU VE TELEMETRİ",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
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
                    label = "Çözünürlük",
                    value = metrics.imageResolution
                )
                MetricRow(
                    label = "Tespit Edilen Yüz",
                    value = "${metrics.facesFoundCount} Adet"
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
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
