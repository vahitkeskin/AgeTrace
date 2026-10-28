package com.vahitkeskin.agetrace.domain.model

data class FaceAnalysisResult(
    val faces: List<DetectedFace>,
    val metrics: AnalysisMetrics,
    val imageWidth: Int,
    val imageHeight: Int,
    val rotationApplied: Int = 0
)
