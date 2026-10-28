package com.vahitkeskin.agetrace.domain.model

/**
 * Pure Kotlin representation of an uncompressed 2D raster image (ARGB_8888).
 * Completely decoupled from Android Bitmap, Java AWT, or Skia.
 */
class PixelBuffer(
    val width: Int,
    val height: Int,
    val pixels: IntArray
) {
    init {
        require(width > 0 && height > 0) { "Dimensions must be positive: ${width}x$height" }
        require(pixels.size >= width * height) {
            "Pixel buffer size (${pixels.size}) is smaller than width * height (${width * height})"
        }
    }

    fun getPixel(x: Int, y: Int): Int {
        if (x !in 0 until width || y !in 0 until height) return 0
        return pixels[y * width + x]
    }

    fun alpha(x: Int, y: Int): Int = (getPixel(x, y) ushr 24) and 0xFF
    fun red(x: Int, y: Int): Int = (getPixel(x, y) ushr 16) and 0xFF
    fun green(x: Int, y: Int): Int = (getPixel(x, y) ushr 8) and 0xFF
    fun blue(x: Int, y: Int): Int = getPixel(x, y) and 0xFF

    /**
     * Standard ITU-R BT.601 perceptual luminance (0.0 .. 255.0).
     */
    fun luminance(x: Int, y: Int): Float {
        val p = getPixel(x, y)
        val r = (p ushr 16) and 0xFF
        val g = (p ushr 8) and 0xFF
        val b = p and 0xFF
        return 0.299f * r + 0.587f * g + 0.114f * b
    }

    /**
     * Extracts a cropped sub-region using a normalized bounding box.
     */
    fun subBuffer(box: BoundingBox): PixelBuffer {
        val startX = (box.left * width).toInt().coerceIn(0, width - 1)
        val startY = (box.top * height).toInt().coerceIn(0, height - 1)
        val endX = (box.right * width).toInt().coerceIn(startX + 1, width)
        val endY = (box.bottom * height).toInt().coerceIn(startY + 1, height)

        val subW = (endX - startX).coerceAtLeast(1)
        val subH = (endY - startY).coerceAtLeast(1)
        val subPixels = IntArray(subW * subH)

        for (row in 0 until subH) {
            val srcOffset = (startY + row) * width + startX
            val dstOffset = row * subW
            pixels.copyInto(subPixels, dstOffset, srcOffset, srcOffset + subW)
        }

        return PixelBuffer(subW, subH, subPixels)
    }

    /**
     * Fast nearest-neighbor downsampling for real-time edge detection.
     */
    fun downsample(targetW: Int, targetH: Int): PixelBuffer {
        if (targetW >= width && targetH >= height) return this
        val outPixels = IntArray(targetW * targetH)
        val xRatio = width.toFloat() / targetW
        val yRatio = height.toFloat() / targetH

        for (y in 0 until targetH) {
            val srcY = (y * yRatio).toInt().coerceIn(0, height - 1)
            val srcRowOffset = srcY * width
            val dstRowOffset = y * targetW
            for (x in 0 until targetW) {
                val srcX = (x * xRatio).toInt().coerceIn(0, width - 1)
                outPixels[dstRowOffset + x] = pixels[srcRowOffset + srcX]
            }
        }
        return PixelBuffer(targetW, targetH, outPixels)
    }

    fun copy(): PixelBuffer {
        return PixelBuffer(width, height, pixels.copyOf())
    }
}
