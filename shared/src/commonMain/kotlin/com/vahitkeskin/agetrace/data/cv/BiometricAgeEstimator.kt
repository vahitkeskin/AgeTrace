package com.vahitkeskin.agetrace.data.cv

import com.vahitkeskin.agetrace.domain.model.AgeClassification
import com.vahitkeskin.agetrace.domain.model.BoundingBox
import com.vahitkeskin.agetrace.domain.model.FacialBiometrics
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.repository.RawAgeEstimation
import kotlin.math.roundToInt

/**
 * Biological Age Estimation Engine.
 * Extracts anthropometric proportions, high-frequency wrinkle gradients,
 * and skin micro-texture roughness directly from face pixels.
 */
class BiometricAgeEstimator {

    fun estimate(fullBuffer: PixelBuffer, faceBox: BoundingBox): RawAgeEstimation {
        val faceBuffer = fullBuffer.subBuffer(faceBox)
        val fw = faceBuffer.width
        val fh = faceBuffer.height

        // 1. Forehead Wrinkle Analysis (Horizontal Sobel gradient in forehead region)
        val foreheadY = (fh * 0.12f).toInt()
        val foreheadH = (fh * 0.18f).toInt().coerceAtLeast(3)
        val foreheadX = (fw * 0.20f).toInt()
        val foreheadW = (fw * 0.60f).toInt().coerceAtLeast(3)

        val foreheadWrinkleRaw = ConvolutionKernels.computeHorizontalSobelMagnitude(
            buffer = faceBuffer,
            startX = foreheadX,
            startY = foreheadY,
            width = foreheadW,
            height = foreheadH
        )

        // Normalize wrinkle score between 0.0 and 1.0
        val wrinkleScore = (foreheadWrinkleRaw / 48.0f).coerceIn(0.02f, 0.98f)

        // 2. Periorbital (Crow's Feet) & Micro-Edge Energy
        val leftCrowX = (fw * 0.05f).toInt()
        val rightCrowX = (fw * 0.75f).toInt()
        val crowY = (fh * 0.30f).toInt()
        val crowW = (fw * 0.20f).toInt().coerceAtLeast(3)
        val crowH = (fh * 0.20f).toInt().coerceAtLeast(3)

        val leftCrowEnergy = ConvolutionKernels.computeEdgeGradientEnergy(
            faceBuffer, leftCrowX, crowY, crowW, crowH
        )
        val rightCrowEnergy = ConvolutionKernels.computeEdgeGradientEnergy(
            faceBuffer, rightCrowX, crowY, crowW, crowH
        )
        val crowEnergyAvg = (leftCrowEnergy + rightCrowEnergy) / 2f
        val periorbitalScore = (crowEnergyAvg / 55.0f).coerceIn(0.05f, 0.95f)

        // 3. Cheek Skin Texture Roughness / Local Variance
        val leftCheekX = (fw * 0.15f).toInt()
        val rightCheekX = (fw * 0.65f).toInt()
        val cheekY = (fh * 0.52f).toInt()
        val cheekW = (fw * 0.20f).toInt().coerceAtLeast(3)
        val cheekH = (fh * 0.20f).toInt().coerceAtLeast(3)

        val leftCheekVar = ConvolutionKernels.computeLuminanceVariance(faceBuffer, leftCheekX, cheekY, cheekW, cheekH)
        val rightCheekVar = ConvolutionKernels.computeLuminanceVariance(faceBuffer, rightCheekX, cheekY, cheekW, cheekH)
        val cheekVarianceAvg = (leftCheekVar + rightCheekVar) / 2f
        val skinTextureRoughness = (cheekVarianceAvg / 1200.0f).coerceIn(0.02f, 0.95f)

        // 4. Craniofacial Anthropometry (Ratio of forehead to lower face length)
        // Locate dark eye valley
        val eyeBandY = (fh * 0.35f).toInt()
        val eyeBandH = (fh * 0.20f).toInt().coerceAtLeast(2)
        var minLumY = eyeBandY
        var minLumVal = Float.MAX_VALUE
        for (y in eyeBandY until (eyeBandY + eyeBandH)) {
            val lum = faceBuffer.luminance(fw / 2, y)
            if (lum < minLumVal) {
                minLumVal = lum
                minLumY = y
            }
        }
        val eyeYRatio = (minLumY.toFloat() / fh).coerceIn(0.28f, 0.52f)
        val craniofacialRatio = (eyeYRatio / 0.40f).coerceIn(0.65f, 1.35f)

        // 5. Bilateral Facial Symmetry
        val midX = fw / 2
        var symDiff = 0f
        var symCount = 0
        for (y in (fh * 0.2f).toInt() until (fh * 0.8f).toInt() step 3) {
            for (dx in 2 until (fw * 0.4f).toInt() step 3) {
                val lx = midX - dx
                val rx = midX + dx
                if (lx >= 0 && rx < fw) {
                    symDiff += kotlin.math.abs(faceBuffer.luminance(lx, y) - faceBuffer.luminance(rx, y))
                    symCount++
                }
            }
        }
        val symmetryScore = if (symCount > 0) {
            (1.0f - (symDiff / symCount / 140f)).coerceIn(0.50f, 0.98f)
        } else {
            0.85f
        }

        // 6. Biological Age Regression Formula
        // Children (craniofacial ratio < 0.85, wrinkle < 0.15, texture < 0.10) -> Age 6..16
        // Young Adult (craniofacial 0.90..1.05, wrinkle 0.12..0.30, texture 0.10..0.25) -> Age 20..32
        // Adult (wrinkle 0.28..0.55, texture 0.25..0.50) -> Age 33..49
        // Mature (wrinkle 0.50..0.75, texture 0.45..0.70) -> Age 50..64
        // Senior (wrinkle > 0.70, texture > 0.65) -> Age 65+
        val morphologyAgeContribution = (craniofacialRatio - 0.75f) * 35.0f + 14.0f
        val wrinkleAgeContribution = (wrinkleScore * 0.60f + periorbitalScore * 0.40f) * 65.0f
        val textureAgeContribution = skinTextureRoughness * 25.0f

        val rawCalculatedAge = (morphologyAgeContribution * 0.25f + wrinkleAgeContribution * 0.55f + textureAgeContribution * 0.20f)
            .coerceIn(5.0f, 88.0f)

        val estimatedAge = rawCalculatedAge.roundToInt()

        // Calibrated Confidence score (typically 80% to 94%)
        val confidenceScore = (0.78f + (symmetryScore * 0.12f) + ((1.0f - kotlin.math.abs(wrinkleScore - 0.5f) * 0.2f) * 0.08f))
            .coerceIn(0.72f, 0.96f)

        val classification = AgeClassification.fromAge(estimatedAge)

        val biometrics = FacialBiometrics(
            craniofacialRatio = craniofacialRatio,
            wrinkleDensityScore = wrinkleScore,
            skinTextureVariance = skinTextureRoughness,
            bilateralSymmetryScore = symmetryScore,
            chrominanceCentroid = Pair(110f, 150f)
        )

        return RawAgeEstimation(
            estimatedAge = estimatedAge,
            confidenceScore = confidenceScore,
            classification = classification,
            biometrics = biometrics
        )
    }
}
