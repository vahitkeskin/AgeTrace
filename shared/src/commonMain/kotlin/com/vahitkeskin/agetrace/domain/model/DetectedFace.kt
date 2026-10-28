package com.vahitkeskin.agetrace.domain.model

import kotlin.math.roundToInt

/**
 * Represents a single detected human face with localized bounding box,
 * real biometric age estimation, and confidence metrics.
 */
data class DetectedFace(
    val id: String,
    val boundingBox: BoundingBox,
    val confidenceScore: Float, // 0.0f .. 1.0f
    val estimatedAge: Int,       // In years (e.g., 32)
    val classification: AgeClassification,
    val biometrics: FacialBiometrics
) {
    /**
     * Exact format specified in requirements: "%87 - 32 Age"
     */
    val badgeLabel: String
        get() = "%${(confidenceScore * 100).roundToInt()} - $estimatedAge Age"

    val confidencePercentage: Int
        get() = (confidenceScore * 100).roundToInt()
}
