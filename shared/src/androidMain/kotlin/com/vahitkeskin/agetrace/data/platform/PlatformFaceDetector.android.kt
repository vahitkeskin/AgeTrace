package com.vahitkeskin.agetrace.data.platform

import com.vahitkeskin.agetrace.data.cv.AndroidFaceDetectorEngine
import com.vahitkeskin.agetrace.domain.repository.FaceDetectorEngine

actual fun createPlatformFaceDetector(): FaceDetectorEngine = AndroidFaceDetectorEngine()
