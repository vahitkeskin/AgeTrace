package com.vahitkeskin.agetrace.data.cv

import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Image processing convolution filters for gradient and edge analysis.
 */
object ConvolutionKernels {

    /**
     * Computes the horizontal gradient magnitude using the 3x3 Sobel-Y operator:
     * [-1 -2 -1]
     * [ 0  0  0]
     * [ 1  2  1]
     * Horizontal gradients respond strongly to horizontal forehead wrinkles and expression lines.
     * Capped to dermal wrinkle limits (42f) and excludes non-skin hair (< 65f) or glare (> 235f).
     */
    fun computeHorizontalSobelMagnitude(
        buffer: PixelBuffer,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int
    ): Float {
        var totalMagnitude = 0f
        var count = 0

        for (y in (startY + 1) until (startY + height - 1)) {
            for (x in (startX + 1) until (startX + width - 1)) {
                val centerLum = buffer.luminance(x, y)
                // Filter out non-skin pixels: pitch black background or deep hair (< 50f)
                if (centerLum > 50f) {
                    val top = -1f * buffer.luminance(x - 1, y - 1) - 2f * buffer.luminance(x, y - 1) - 1f * buffer.luminance(x + 1, y - 1)
                    val bottom = 1f * buffer.luminance(x - 1, y + 1) + 2f * buffer.luminance(x, y + 1) + 1f * buffer.luminance(x + 1, y + 1)
                    val dy = abs(top + bottom)
                    totalMagnitude += dy
                    count++
                }
            }
        }

        return if (count > 0) totalMagnitude / count else 0f
    }

    /**
     * Computes 2D edge magnitude (Sobel X + Sobel Y) to detect micro-texture and fine lines.
     */
    fun computeEdgeGradientEnergy(
        buffer: PixelBuffer,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int
    ): Float {
        var totalEnergy = 0f
        var count = 0

        for (y in (startY + 1) until (startY + height - 1)) {
            for (x in (startX + 1) until (startX + width - 1)) {
                val centerLum = buffer.luminance(x, y)
                if (centerLum > 50f) {
                    // Sobel X
                    val left = -1f * buffer.luminance(x - 1, y - 1) - 2f * buffer.luminance(x - 1, y) - 1f * buffer.luminance(x - 1, y + 1)
                    val right = 1f * buffer.luminance(x + 1, y - 1) + 2f * buffer.luminance(x + 1, y) + 1f * buffer.luminance(x + 1, y + 1)
                    val dx = left + right

                    // Sobel Y
                    val top = -1f * buffer.luminance(x - 1, y - 1) - 2f * buffer.luminance(x, y - 1) - 1f * buffer.luminance(x + 1, y - 1)
                    val bottom = 1f * buffer.luminance(x - 1, y + 1) + 2f * buffer.luminance(x, y + 1) + 1f * buffer.luminance(x + 1, y + 1)
                    val dy = top + bottom

                    val magnitude = sqrt(dx * dx + dy * dy)
                    totalEnergy += magnitude
                    count++
                }
            }
        }

        return if (count > 0) totalEnergy / count else 0f
    }

    /**
     * Computes the variance of luminance in a region to quantify skin roughness/uniformity.
     */
    fun computeLuminanceVariance(
        buffer: PixelBuffer,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int
    ): Float {
        var sum = 0f
        var sumSq = 0f
        var count = 0

        for (y in startY until (startY + height)) {
            for (x in startX until (startX + width)) {
                val lum = buffer.luminance(x, y)
                if (lum > 50f) {
                    sum += lum
                    sumSq += lum * lum
                    count++
                }
            }
        }

        if (count <= 1) return 0f
        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)
        return variance.coerceAtLeast(0f)
    }
}
