package com.vahitkeskin.agetrace.domain.usecase

import com.vahitkeskin.agetrace.data.cv.BmpEncoder
import com.vahitkeskin.agetrace.data.cv.PythonDetectionParser
import com.vahitkeskin.agetrace.data.platform.PlatformPythonBridge
import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.FaceAnalysisResult
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.model.VisionEngineType
import com.vahitkeskin.agetrace.domain.repository.AgeEstimatorEngine
import com.vahitkeskin.agetrace.domain.repository.FaceDetectorEngine
import kotlin.time.TimeSource

/**
 * UseCase implementing the hybrid Python & Edge AI computer vision analysis pipeline.
 */
class AnalyzeImageUseCase(
    private val faceDetectorEngine: FaceDetectorEngine,
    private val ageEstimatorEngine: AgeEstimatorEngine
) {
    suspend operator fun invoke(
        buffer: PixelBuffer,
        rawBytes: ByteArray? = null,
        preferredEngine: VisionEngineType = VisionEngineType.PYTHON_CORE
    ): FaceAnalysisResult {
        val timeSource = TimeSource.Monotonic
        val startMark = timeSource.markNow()

        // 1. If Python Vision Core is requested, attempt Python execution
        if (preferredEngine == VisionEngineType.PYTHON_CORE && PlatformPythonBridge.isPythonSupported()) {
            val bytesToAnalyze = rawBytes ?: BmpEncoder.encode(buffer)
            val pythonJson = PlatformPythonBridge.analyzeWithPython(bytesToAnalyze)

            if (pythonJson != null) {
                val parsed = PythonDetectionParser.parse(pythonJson)
                if (parsed != null && parsed.faces.isNotEmpty()) {
                    val elapsedMs = startMark.elapsedNow().inWholeMilliseconds
                    val primaryFace = parsed.faces.first()

                    val finalBuffer = if (parsed.rotationApplied != 0) {
                        try {
                            when (parsed.rotationApplied) {
                                90 -> buffer.rotate90(clockwise = false)
                                270 -> buffer.rotate90(clockwise = true)
                                180 -> buffer.rotate90(true).rotate90(true)
                                else -> buffer
                            }
                        } catch (t: Throwable) {
                            buffer
                        }
                    } else buffer

                    return FaceAnalysisResult(
                        faces = parsed.faces,
                        metrics = AnalysisMetrics(
                            inferenceTimeMs = elapsedMs,
                            imageResolution = "${finalBuffer.width} × ${finalBuffer.height}",
                            facesFoundCount = parsed.faces.size,
                            primaryConfidence = primaryFace.confidenceScore,
                            primaryAge = primaryFace.estimatedAge,
                            primaryClassification = primaryFace.classification,
                            engineName = parsed.engine,
                            isFaceDetected = true,
                            statusMessage = "${parsed.faces.size} Yüz Tespit Edildi • ${parsed.engine}"
                        ),
                        imageWidth = finalBuffer.width,
                        imageHeight = finalBuffer.height,
                        rotationApplied = parsed.rotationApplied
                    )
                }
            }
        }

        // 2. Standalone Native Edge AI Core Execution (also serves as 100% reliable fallback)
        val rawCandidates = faceDetectorEngine.detectFaces(buffer)
        val detectedFaces = mutableListOf<DetectedFace>()

        for ((index, candidate) in rawCandidates.withIndex()) {
            val estimation = ageEstimatorEngine.estimateAge(buffer, candidate.boundingBox)
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
        val sortedFaces = detectedFaces.sortedByDescending { it.confidenceScore }
        val primaryFace = sortedFaces.firstOrNull()

        val activeEngineTitle = if (preferredEngine == VisionEngineType.PYTHON_CORE) {
            "Python Core (Yerel Fallback)"
        } else {
            "Edge AI Vision Core"
        }

        val metrics = AnalysisMetrics(
            inferenceTimeMs = elapsedMs,
            imageResolution = "${buffer.width} × ${buffer.height}",
            facesFoundCount = sortedFaces.size,
            primaryConfidence = primaryFace?.confidenceScore ?: 0f,
            primaryAge = primaryFace?.estimatedAge,
            primaryClassification = primaryFace?.classification,
            engineName = activeEngineTitle,
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
