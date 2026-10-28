package com.vahitkeskin.agetrace.data.repository

import com.vahitkeskin.agetrace.data.cv.EdgeAiFaceDetector
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.repository.FaceDetectorEngine
import com.vahitkeskin.agetrace.domain.repository.RawFaceCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FaceDetectorEngineImpl(
    private val detector: EdgeAiFaceDetector = EdgeAiFaceDetector()
) : FaceDetectorEngine {

    override suspend fun detectFaces(buffer: PixelBuffer): List<RawFaceCandidate> = withContext(Dispatchers.Default) {
        detector.detect(buffer)
    }
}
