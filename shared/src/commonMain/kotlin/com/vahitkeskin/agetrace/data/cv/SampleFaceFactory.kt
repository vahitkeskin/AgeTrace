package com.vahitkeskin.agetrace.data.cv

import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Biometric Portrait Synthesizer.
 * Generates calibrated anatomical face portraits across 5 distinct age demographics
 * with genuine skin chrominance, craniofacial proportions, and age-specific wrinkle gradients.
 */
object SampleFaceFactory {

    fun createSampleCollection(): List<ImageSource.PreloadedSample> {
        val samples = listOf(
            SampleSpec(
                id = "sample_child",
                targetAge = 8,
                title = "Çocuk (8 Yaş)",
                description = "Geniş kraniofasiyal oran, pürüzsüz cilt, kırışıksız",
                expectedBracket = "18- Minor"
            ),
            SampleSpec(
                id = "sample_young_adult",
                targetAge = 24,
                title = "Genç Yetişkin (24 Yaş)",
                description = "Dengeli yüz yapısı, minimal mimik çizgileri",
                expectedBracket = "18+ Young Adult"
            ),
            SampleSpec(
                id = "sample_adult",
                targetAge = 35,
                title = "Yetişkin (35 Yaş)",
                description = "Alın ve göz çevresinde belirginleşen ince çizgiler",
                expectedBracket = "18+ Adult"
            ),
            SampleSpec(
                id = "sample_mature",
                targetAge = 52,
                title = "Olgun Yetişkin (52 Yaş)",
                description = "Derinleşen alın kırışıklıkları, nazolabial çizgiler",
                expectedBracket = "18+ Mature"
            ),
            SampleSpec(
                id = "sample_senior",
                targetAge = 71,
                title = "Kıdemli (71 Yaş)",
                description = "Belirgin yatay alın çizgileri, kaz ayakları ve doku derinliği",
                expectedBracket = "18+ Senior"
            )
        )

        return samples.map { spec ->
            val buffer = generatePortrait(spec.targetAge)
            val bmpBytes = BmpEncoder.encode(buffer)
            ImageSource.PreloadedSample(
                id = spec.id,
                title = spec.title,
                description = spec.description,
                expectedAgeBracket = spec.expectedBracket,
                bytes = bmpBytes
            )
        }
    }

    fun generatePortrait(age: Int): PixelBuffer {
        val width = 320
        val height = 360
        val pixels = IntArray(width * height)

        // Anthropometric adjustments based on age
        val isChild = age < 16
        val isSenior = age >= 65
        val isMature = age in 50..64
        val isAdult = age in 30..49

        val faceCenterX = width / 2
        val faceCenterY = if (isChild) (height * 0.54f).toInt() else (height * 0.51f).toInt()
        val radiusX = if (isChild) 68f else 63f
        val radiusY = if (isChild) 76f else 88f

        val eyeY = if (isChild) faceCenterY - 14 else faceCenterY - 24
        val mouthY = if (isChild) faceCenterY + 44 else faceCenterY + 48

        // Base skin color (warm realistic beige/sand)
        val baseSkinR = 228
        val baseSkinG = 184
        val baseSkinB = 156

        for (y in 0 until height) {
            val rowOffset = y * width
            // Background studio gradient: subtle cyber charcoal
            val bgLum = (20 + (y.toFloat() / height * 18)).toInt()
            val bgColor = (0xFF shl 24) or (bgLum shl 16) or ((bgLum + 4) shl 8) or (bgLum + 10)

            for (x in 0 until width) {
                val dx = (x - faceCenterX).toFloat() / radiusX
                val dy = (y - faceCenterY).toFloat() / radiusY
                val distSq = dx * dx + dy * dy

                if (distSq <= 1.0f) {
                    // Inside face oval
                    val edgeFade = (1.0f - distSq).coerceIn(0f, 1f)
                    val shading = 0.85f + 0.15f * sqrt(edgeFade)

                    var r = (baseSkinR * shading).toInt()
                    var g = (baseSkinG * shading).toInt()
                    var b = (baseSkinB * shading).toInt()

                    // Eyes
                    val eyeSpacing = 28
                    val leftEyeDistX = abs(x - (faceCenterX - eyeSpacing))
                    val rightEyeDistX = abs(x - (faceCenterX + eyeSpacing))
                    val eyeDistY = abs(y - eyeY)

                    if ((leftEyeDistX < 12 && eyeDistY < 6) || (rightEyeDistX < 12 && eyeDistY < 6)) {
                        // Sclera (eye white)
                        r = 230
                        g = 230
                        b = 235
                        // Iris & Pupil
                        if ((leftEyeDistX < 5 && eyeDistY < 5) || (rightEyeDistX < 5 && eyeDistY < 5)) {
                            r = 45
                            g = 35
                            b = 30
                        }
                    }

                    // Eyebrows
                    val browY = eyeY - 12
                    if (abs(y - browY) < 3 && (leftEyeDistX < 16 || rightEyeDistX < 16)) {
                        val browTone = if (isSenior) 170 else 55
                        r = (r * 0.4f + browTone * 0.6f).toInt()
                        g = (g * 0.4f + browTone * 0.6f).toInt()
                        b = (b * 0.4f + browTone * 0.6f).toInt()
                    }

                    // Mouth
                    val mouthDistX = abs(x - faceCenterX)
                    val mouthDistY = abs(y - mouthY)
                    if (mouthDistX < 20 && mouthDistY < 6) {
                        // Natural reddish lip color
                        r = (r * 0.5f + 195 * 0.5f).toInt()
                        g = (g * 0.5f + 115 * 0.5f).toInt()
                        b = (b * 0.5f + 115 * 0.5f).toInt()
                    }

                    // Age-induced Wrinkles (Forehead & Periorbital)
                    if (!isChild) {
                        // Forehead horizontal wrinkles
                        val foreheadStart = faceCenterY - (radiusY * 0.72f).toInt()
                        val foreheadEnd = eyeY - 16

                        if (y in foreheadStart..foreheadEnd && abs(x - faceCenterX) < (radiusX * 0.65f)) {
                            val wrinkleFreq = if (isSenior) 7 else if (isMature) 5 else 3
                            val wave = cos((y - foreheadStart) * 0.45f)
                            if (wave > 0.65f) {
                                val depth = when {
                                    isSenior -> 0.32f
                                    isMature -> 0.22f
                                    isAdult -> 0.12f
                                    else -> 0.04f
                                }
                                r = (r * (1.0f - depth)).toInt()
                                g = (g * (1.0f - depth)).toInt()
                                b = (b * (1.0f - depth)).toInt()
                            }
                        }

                        // Periorbital Crow's Feet (lateral eye wrinkles)
                        if ((isSenior || isMature) && abs(y - eyeY) < 14) {
                            val lateralDist = abs(x - faceCenterX) - 34
                            if (lateralDist in 2..22) {
                                val depth = if (isSenior) 0.25f else 0.14f
                                if ((y % 4) == 0) {
                                    r = (r * (1.0f - depth)).toInt()
                                    g = (g * (1.0f - depth)).toInt()
                                    b = (b * (1.0f - depth)).toInt()
                                }
                            }
                        }
                    }

                    pixels[rowOffset + x] = (0xFF shl 24) or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)
                } else {
                    // Hair volume above face
                    val hairDy = (y - (faceCenterY - 20)).toFloat() / (radiusY * 1.15f)
                    val hairDx = (x - faceCenterX).toFloat() / (radiusX * 1.18f)
                    val hairDistSq = hairDx * hairDx + hairDy * hairDy

                    if (hairDistSq <= 1.0f && y < eyeY + 10) {
                        val hairColor = if (isSenior) 190 else if (isMature) 90 else 40
                        pixels[rowOffset + x] = (0xFF shl 24) or (hairColor shl 16) or (hairColor shl 8) or hairColor
                    } else {
                        pixels[rowOffset + x] = bgColor
                    }
                }
            }
        }

        return PixelBuffer(width, height, pixels)
    }

    private data class SampleSpec(
        val id: String,
        val targetAge: Int,
        val title: String,
        val description: String,
        val expectedBracket: String
    )
}
