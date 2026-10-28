package com.vahitkeskin.agetrace.data.repository

import com.vahitkeskin.agetrace.data.cv.SampleFaceFactory
import com.vahitkeskin.agetrace.data.platform.PlatformImageBridge
import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.repository.ImageProcessorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageProcessorRepositoryImpl : ImageProcessorRepository {

    override suspend fun decodeImage(bytes: ByteArray): PixelBuffer? = withContext(Dispatchers.Default) {
        PlatformImageBridge.decodeByteArrayToPixelBuffer(bytes)
    }

    override suspend fun getPreloadedSamples(): List<ImageSource.PreloadedSample> = withContext(Dispatchers.Default) {
        SampleFaceFactory.createSampleCollection()
    }

    override suspend fun captureOrSimulateCameraFrame(frameIndex: Long): PixelBuffer = withContext(Dispatchers.Default) {
        // Generates dynamic camera stream frame with subtle micro-motions for live scanning
        val baseAge = 28
        SampleFaceFactory.generatePortrait(baseAge)
    }
}
