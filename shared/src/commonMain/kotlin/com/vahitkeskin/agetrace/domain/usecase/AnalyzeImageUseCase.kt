package com.vahitkeskin.agetrace.domain.usecase

import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.FaceAnalysisResult
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.repository.AgeEstimatorEngine
import com.vahitkeskin.agetrace.domain.repository.FaceDetectorEngine
import kotlin.math.roundToInt
import kotlin.time.TimeSource

/**
 * UseCase implementing the complete Edge AI computer vision analysis pipeline.
 */
class AnalyzeImageUseCase(
    private val faceDetectorEngine: FaceDetectorEngine,
    private val ageEstimatorEngine: AgeEstimatorEngine
) {
    suspend operator fun invoke(buffer: PixelBuffer): FaceAnalysisResult {
        val timeSource = TimeSource.Monotonic
        val startMark = timeSource.markNow()

        // 1. Detect candidate faces in the image pixels
        val rawCandidates = faceDetectorEngine.detectFaces(buffer)

        // 2. For each detected face, compute anthropometry and estimate age
        val detectedFaces = mutableListOf<DetectedFace>()
        for ((index, candidate) in rawCandidates.withIndex()) {
            val estimation = ageEstimatorEngine.estimateAge(buffer, candidate.boundingBox)

            // Combined confidence of face detection and age classification
            val combinedConfidence = (candidate.confidence * 0.55f + estimation.confidenceScore * 0.45f)
                .coerceIn(0.51f, 0.99f)

            detectedFaces.add(
                DetectedFace(
                    id = "face_${index + 1}",
                    boundingBox = candidate.boundingBox,
                    confidenceScore = combinedConfidence,
                    estimatedAge = estimation.estimatedAge,
                    classification = estimation.classification,
                    biometrics = estimation.biometrics
                )
            )
        }

        val elapsedMs = startMark.elapsedNow().inWholeMilliseconds

        // Sort faces by confidence descending
        val sortedFaces = detectedFaces.sortedByDescending { it.confidenceScore }
        val primaryFace = sortedFaces.firstOrNull()

        val metrics = AnalysisMetrics(
            inferenceTimeMs = elapsedMs,
            imageResolution = "${buffer.width} × ${buffer.height}",
            facesFoundCount = sortedFaces.size,
            primaryConfidence = primaryFace?.confidenceScore ?: 0f,
            primaryAge = primaryFace?.estimatedAge,
            primaryClassification = primaryFace?.classification,
            isFaceDetected = sortedFaces.isNotEmpty(),
            statusMessage = if (sortedFaces.isNotEmpty()) {
                "${sortedFaces.size} Yüz Tespit Edildi • Analiz Tamamlandı"
            } else {
                "Yüz Tespit Edilemedi"
            }
        )

        return FaceAnalysisResult(
            faces = sortedFaces,
            metrics = metrics,
            imageWidth = buffer.width,
            imageHeight = buffer.height
        )
    }
}
