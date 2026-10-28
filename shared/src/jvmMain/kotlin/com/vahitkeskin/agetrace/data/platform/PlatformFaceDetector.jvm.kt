package com.vahitkeskin.agetrace.data.platform

import com.vahitkeskin.agetrace.data.repository.FaceDetectorEngineImpl
import com.vahitkeskin.agetrace.domain.repository.FaceDetectorEngine

actual fun createPlatformFaceDetector(): FaceDetectorEngine = FaceDetectorEngineImpl()
