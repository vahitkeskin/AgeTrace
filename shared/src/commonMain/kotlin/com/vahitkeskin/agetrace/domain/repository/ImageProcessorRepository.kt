package com.vahitkeskin.agetrace.domain.repository

import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.model.PixelBuffer

interface ImageProcessorRepository {
    /**
     * Decodes raw compressed image bytes (PNG, JPEG, WebP, etc.) into an uncompressed PixelBuffer.
     */
    suspend fun decodeImage(bytes: ByteArray): PixelBuffer?

    /**
     * Retrieves preloaded real portrait benchmarks across different age cohorts.
     */
    suspend fun getPreloadedSamples(): List<ImageSource.PreloadedSample>

    /**
     * Generates a camera frame for real-time video stream simulation / capture.
     */
    suspend fun captureOrSimulateCameraFrame(frameIndex: Long): PixelBuffer
}
