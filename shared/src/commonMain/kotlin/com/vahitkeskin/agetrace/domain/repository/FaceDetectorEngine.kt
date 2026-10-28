package com.vahitkeskin.agetrace.domain.repository

import com.vahitkeskin.agetrace.domain.model.BoundingBox
import com.vahitkeskin.agetrace.domain.model.PixelBuffer

data class RawFaceCandidate(
    val boundingBox: BoundingBox,
    val confidence: Float,
    val eyeDistanceRatio: Float,
    val mouthYRatio: Float,
    val skinScore: Float
)

/**
 * Port interface for Computer Vision face detection engines.
 */
interface FaceDetectorEngine {
    /**
     * Inspects raw image pixels and locates all human face regions.
     */
    suspend fun detectFaces(buffer: PixelBuffer): List<RawFaceCandidate>
}
