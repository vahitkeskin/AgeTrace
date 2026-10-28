package com.vahitkeskin.agetrace.data.platform

import com.vahitkeskin.agetrace.domain.repository.FaceDetectorEngine

/**
 * Platform factory for face detector engine.
 * Android: Google ML Kit Face Detection + Android FaceDetector + Edge AI fallback.
 * Desktop / JVM: Precision Edge AI + Python Core.
 * iOS: Edge AI + Vision Framework.
 */
expect fun createPlatformFaceDetector(): FaceDetectorEngine
