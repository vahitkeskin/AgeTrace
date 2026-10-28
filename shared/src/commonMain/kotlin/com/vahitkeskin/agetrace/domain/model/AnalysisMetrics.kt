package com.vahitkeskin.agetrace.domain.model

/**
 * Real-time performance and biometric telemetry metrics.
 */
data class AnalysisMetrics(
    val inferenceTimeMs: Long = 0L,
    val imageResolution: String = "--",
    val facesFoundCount: Int = 0,
    val primaryConfidence: Float = 0f,
    val primaryAge: Int? = null,
    val primaryClassification: AgeClassification? = null,
    val engineName: String = "Edge AI Vision Core (Offline)",
    val isFaceDetected: Boolean = false,
    val statusMessage: String = "Awaiting input image"
) {
    val confidencePercentage: Int
        get() = (primaryConfidence * 100).toInt()
}
