package com.vahitkeskin.agetrace.domain.repository

import com.vahitkeskin.agetrace.domain.model.AgeClassification
import com.vahitkeskin.agetrace.domain.model.BoundingBox
import com.vahitkeskin.agetrace.domain.model.FacialBiometrics
import com.vahitkeskin.agetrace.domain.model.PixelBuffer

data class RawAgeEstimation(
    val estimatedAge: Int,
    val confidenceScore: Float,
    val classification: AgeClassification,
    val biometrics: FacialBiometrics
)

/**
 * Port interface for Biometric Age Estimation algorithms.
 */
interface AgeEstimatorEngine {
    /**
     * Extracts morphological, anthropometric, and high-frequency wrinkle/texture
     * features from the face sub-region and estimates the biological age.
     */
    suspend fun estimateAge(
        fullBuffer: PixelBuffer,
        faceBox: BoundingBox
    ): RawAgeEstimation
}
