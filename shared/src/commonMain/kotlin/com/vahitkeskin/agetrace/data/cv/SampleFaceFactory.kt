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
            ),
            SampleSpec(
                id = "sample_family_group",
                targetAge = -1,
                title = "Grup / Aile (7'den 70'e)",
                description = "Aynı karede 3 kişi: Çocuk (7 Yaş), Yetişkin (35 Yaş) ve Dede (70 Yaş)",
                expectedBracket = "7-70 Çoklu Yüz"
            )
        )

        return samples.map { spec ->
            val buffer = if (spec.targetAge == -1) generateGroupPortrait() else generatePortrait(spec.targetAge)
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

        val isChild = age < 16
        val faceCenterX = width / 2
        val faceCenterY = if (isChild) (height * 0.54f).toInt() else (height * 0.51f).toInt()
        val radiusX = if (isChild) 68f else 63f
        val radiusY = if (isChild) 76f else 88f

        // Studio gradient background
        for (y in 0 until height) {
            val rowOffset = y * width
            val bgLum = (20 + (y.toFloat() / height * 18)).toInt()
            val bgColor = (0xFF shl 24) or (bgLum shl 16) or ((bgLum + 4) shl 8) or (bgLum + 10)
            for (x in 0 until width) {
                pixels[rowOffset + x] = bgColor
            }
        }

        renderFaceAt(pixels, width, height, faceCenterX, faceCenterY, radiusX, radiusY, age)
        return PixelBuffer(width, height, pixels)
    }

    fun generateGroupPortrait(): PixelBuffer {
        val width = 480
        val height = 360
        val pixels = IntArray(width * height)

        // Studio gradient background
        for (y in 0 until height) {
            val rowOffset = y * width
            val bgLum = (18 + (y.toFloat() / height * 18)).toInt()
            val bgColor = (0xFF shl 24) or (bgLum shl 16) or ((bgLum + 4) shl 8) or (bgLum + 8)
            for (x in 0 until width) {
                pixels[rowOffset + x] = bgColor
            }
        }

        // Render 3 distinct people across the frame (from 7 to 70 years old):
        // 1. Child (7 Age)
        renderFaceAt(pixels, width, height, centerX = 95, centerY = 195, radiusX = 46f, radiusY = 55f, age = 7)
        // 2. Adult (35 Age)
        renderFaceAt(pixels, width, height, centerX = 240, centerY = 168, radiusX = 52f, radiusY = 70f, age = 35)
        // 3. Senior (70 Age)
        renderFaceAt(pixels, width, height, centerX = 385, centerY = 165, radiusX = 50f, radiusY = 68f, age = 70)

        return PixelBuffer(width, height, pixels)
    }

    private fun renderFaceAt(
        pixels: IntArray,
        canvasW: Int,
        canvasH: Int,
        centerX: Int,
        centerY: Int,
        radiusX: Float,
        radiusY: Float,
        age: Int
    ) {
        val isChild = age < 16
        val isSenior = age >= 65
        val isMature = age in 50..64
        val isAdult = age in 30..49

        val eyeY = if (isChild) centerY - (radiusY * 0.18f).toInt() else centerY - (radiusY * 0.28f).toInt()
        val mouthY = if (isChild) centerY + (radiusY * 0.58f).toInt() else centerY + (radiusY * 0.55f).toInt()

        val baseSkinR = 228
        val baseSkinG = 184
        val baseSkinB = 156

        val startX = (centerX - radiusX * 1.3f).toInt().coerceIn(0, canvasW - 1)
        val endX = (centerX + radiusX * 1.3f).toInt().coerceIn(0, canvasW - 1)
        val startY = (centerY - radiusY * 1.3f).toInt().coerceIn(0, canvasH - 1)
        val endY = (centerY + radiusY * 1.3f).toInt().coerceIn(0, canvasH - 1)

        for (y in startY..endY) {
            val rowOffset = y * canvasW
            for (x in startX..endX) {
                val dx = (x - centerX).toFloat() / radiusX
                val dy = (y - centerY).toFloat() / radiusY
                val distSq = dx * dx + dy * dy

                if (distSq <= 1.0f) {
                    val edgeFade = (1.0f - distSq).coerceIn(0f, 1f)
                    val shading = 0.85f + 0.15f * sqrt(edgeFade)

                    var r = (baseSkinR * shading).toInt()
                    var g = (baseSkinG * shading).toInt()
                    var b = (baseSkinB * shading).toInt()

                    // Eyes
                    val eyeSpacing = (radiusX * 0.44f).toInt()
                    val leftEyeDistX = abs(x - (centerX - eyeSpacing))
                    val rightEyeDistX = abs(x - (centerX + eyeSpacing))
                    val eyeDistY = abs(y - eyeY)

                    if ((leftEyeDistX < 10 && eyeDistY < 5) || (rightEyeDistX < 10 && eyeDistY < 5)) {
                        r = 230
                        g = 230
                        b = 235
                        if ((leftEyeDistX < 4 && eyeDistY < 4) || (rightEyeDistX < 4 && eyeDistY < 4)) {
                            r = 45
                            g = 35
                            b = 30
                        }
                    }

                    // Eyebrows
                    val browY = eyeY - 10
                    if (abs(y - browY) < 3 && (leftEyeDistX < 14 || rightEyeDistX < 14)) {
                        val browTone = if (isSenior) 170 else 55
                        r = (r * 0.4f + browTone * 0.6f).toInt()
                        g = (g * 0.4f + browTone * 0.6f).toInt()
                        b = (b * 0.4f + browTone * 0.6f).toInt()
                    }

                    // Mouth
                    val mouthDistX = abs(x - centerX)
                    val mouthDistY = abs(y - mouthY)
                    if (mouthDistX < (radiusX * 0.32f) && mouthDistY < 5) {
                        r = (r * 0.5f + 195 * 0.5f).toInt()
                        g = (g * 0.5f + 115 * 0.5f).toInt()
                        b = (b * 0.5f + 115 * 0.5f).toInt()
                    }

                    // Forehead Wrinkles
                    if (!isChild) {
                        val foreheadStart = centerY - (radiusY * 0.72f).toInt()
                        val foreheadEnd = eyeY - 12
                        if (y in foreheadStart..foreheadEnd && abs(x - centerX) < (radiusX * 0.65f)) {
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

                        // Crow's feet
                        if ((isSenior || isMature) && abs(y - eyeY) < 12) {
                            val lateralDist = abs(x - centerX) - (radiusX * 0.55f).toInt()
                            if (lateralDist in 2..18) {
                                val depth = if (isSenior) 0.25f else 0.14f
                                if ((y % 3) == 0) {
                                    r = (r * (1.0f - depth)).toInt()
                                    g = (g * (1.0f - depth)).toInt()
                                    b = (b * (1.0f - depth)).toInt()
                                }
                            }
                        }
                    }

                    pixels[rowOffset + x] = (0xFF shl 24) or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)
                } else {
                    // Hair
                    val hairDy = (y - (centerY - 16)).toFloat() / (radiusY * 1.15f)
                    val hairDx = (x - centerX).toFloat() / (radiusX * 1.18f)
                    val hairDistSq = hairDx * hairDx + hairDy * hairDy

                    if (hairDistSq <= 1.0f && y < eyeY + 8) {
                        val hairColor = if (isSenior) 190 else if (isMature) 90 else 40
                        pixels[rowOffset + x] = (0xFF shl 24) or (hairColor shl 16) or (hairColor shl 8) or hairColor
                    }
                }
            }
        }
    }

    private data class SampleSpec(
        val id: String,
        val targetAge: Int,
        val title: String,
        val description: String,
        val expectedBracket: String
    )
}
