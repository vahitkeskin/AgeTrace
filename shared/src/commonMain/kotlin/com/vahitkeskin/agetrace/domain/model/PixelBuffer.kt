package com.vahitkeskin.agetrace.domain.model

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

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

    fun rotate90(clockwise: Boolean = true): PixelBuffer {
        val newW = height
        val newH = width
        return try {
            val newPixels = IntArray(newW * newH)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val srcIndex = y * width + x
                    val dstIndex = if (clockwise) {
                        x * newW + (newW - 1 - y)
                    } else {
                        (newH - 1 - x) * newW + y
                    }
                    newPixels[dstIndex] = pixels[srcIndex]
                }
            }
            PixelBuffer(newW, newH, newPixels)
        } catch (oom: Throwable) {
            println("PixelBuffer.rotate90 OOM avoided: ${oom.message}")
            this
        }
    }

    fun copy(): PixelBuffer {
        return PixelBuffer(width, height, pixels.copyOf())
    }

    /**
     * Applies a Google Maps Street View style soft elliptical Gaussian blur
     * exclusively over the specified minor face bounding boxes.
     * Adults and background outside the face ellipses remain completely untouched.
     */
    /**
     * Applies an authentic Google Maps Street View privacy blur over detected face bounding boxes.
     * Encompasses the entire head, hairline, forehead, jaw, and chin with a superellipse mask,
     * ensuring 100% full frosted Gaussian blur across the entire face with zero empty gaps or bleed-through.
     */
    fun withBlurredFaces(boxes: List<BoundingBox>): PixelBuffer {
        if (boxes.isEmpty()) return this
        return try {
            val outPixels = pixels.copyOf()
            for (box in boxes) {
                applyGoogleMapsFaceBlur(box, outPixels)
            }
            PixelBuffer(width, height, outPixels)
        } catch (t: Throwable) {
            println("PixelBuffer.withBlurredFaces error: ${t.message}")
            this
        }
    }

    fun withBlurredMinorFaces(minorBoxes: List<BoundingBox>): PixelBuffer = withBlurredFaces(minorBoxes)

    private fun applyGoogleMapsFaceBlur(box: BoundingBox, targetPixels: IntArray) {
        val boxW = box.normalizedWidth * width
        val boxH = box.normalizedHeight * height
        val cx = box.centerX * width
        val cy = box.centerY * height

        // Center shifted slightly upward to account for forehead, hairline, and hair volume
        val headCy = cy - boxH * 0.05f

        // Generous head-proportional semi-axes (covers temples, jaw, chin, and hair boundary)
        val rx = (boxW * 0.72f).coerceAtLeast(10f)
        val ry = (boxH * 0.82f).coerceAtLeast(12f)

        // Bounding rectangle encompassing the superellipse + feather margin
        val minX = (cx - rx * 1.12f).toInt().coerceIn(0, width - 1)
        val maxX = (cx + rx * 1.12f).toInt().coerceIn(minX + 1, width)
        val minY = (headCy - ry * 1.12f).toInt().coerceIn(0, height - 1)
        val maxY = (headCy + ry * 1.12f).toInt().coerceIn(minY + 1, height)

        val regionW = maxX - minX
        val regionH = maxY - minY
        if (regionW < 4 || regionH < 4) return

        // Downsample factor: downsamples patch for a smooth, frosted Gaussian diffusion
        val gridW = (regionW / 8).coerceIn(16, 42)
        val gridH = (regionH / 8).coerceIn(16, 42)

        val smallGrid = IntArray(gridW * gridH)
        val xStep = regionW.toFloat() / gridW
        val yStep = regionH.toFloat() / gridH

        // Extract downsampled patch
        for (gy in 0 until gridH) {
            val srcY = (minY + gy * yStep).toInt().coerceIn(0, height - 1)
            val srcRowOffset = srcY * width
            val dstRowOffset = gy * gridW
            for (gx in 0 until gridW) {
                val srcX = (minX + gx * xStep).toInt().coerceIn(0, width - 1)
                smallGrid[dstRowOffset + gx] = pixels[srcRowOffset + srcX]
            }
        }

        // Multi-pass separable box blur (3 passes H+V yields true Gaussian bell curve via CLT)
        val tempGrid = IntArray(gridW * gridH)
        val blurRadius = (min(gridW, gridH) / 6).coerceIn(2, 5)

        repeat(3) {
            boxBlurHorizontal(smallGrid, tempGrid, gridW, gridH, blurRadius)
            boxBlurVertical(tempGrid, smallGrid, gridW, gridH, blurRadius)
        }

        // Superellipse power p = 2.8 for head-conforming squircle shape (fills jaw and forehead corners)
        val p = 2.8f
        val invP = 1.0f / p

        for (y in minY until maxY) {
            val dy = abs(y - headCy)
            val ny = dy / ry
            if (ny >= 1.12f) continue
            val nyPow = ny.pow(p)

            val rowOffset = y * width
            val v = ((y - minY).toFloat() / regionH * (gridH - 1)).coerceIn(0f, (gridH - 1).toFloat())

            for (x in minX until maxX) {
                val dx = abs(x - cx)
                val nx = dx / rx
                if (nx >= 1.12f) continue

                val d = (nx.pow(p) + nyPow).pow(invP)

                // Outside Google Maps perimeter feather zone -> leave original pixel untouched
                if (d >= 1.05f) continue

                // Bilinear sample from Gaussian frosted grid
                val u = ((x - minX).toFloat() / regionW * (gridW - 1)).coerceIn(0f, (gridW - 1).toFloat())
                val blurredRgb = sampleBilinear(smallGrid, gridW, gridH, u, v)

                // Google Maps Street View weight: 1.0 (100% solid blur) across entire face (d <= 0.90)
                // Smooth cubic hermite feather between 0.90 and 1.05 at outer perimeter
                val weight = if (d <= 0.90f) {
                    1.0f
                } else {
                    val t = (d - 0.90f) / 0.15f
                    1.0f - (t * t * (3f - 2f * t))
                }

                val origPixel = targetPixels[rowOffset + x]
                val origA = (origPixel ushr 24) and 0xFF
                val origR = (origPixel ushr 16) and 0xFF
                val origG = (origPixel ushr 8) and 0xFF
                val origB = origPixel and 0xFF

                val blurR = (blurredRgb ushr 16) and 0xFF
                val blurG = (blurredRgb ushr 8) and 0xFF
                val blurB = blurredRgb and 0xFF

                val finalR = (blurR * weight + origR * (1f - weight)).roundToInt().coerceIn(0, 255)
                val finalG = (blurG * weight + origG * (1f - weight)).roundToInt().coerceIn(0, 255)
                val finalB = (blurB * weight + origB * (1f - weight)).roundToInt().coerceIn(0, 255)

                targetPixels[rowOffset + x] = (origA shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
            }
        }
    }

    private fun sampleBilinear(grid: IntArray, w: Int, h: Int, u: Float, v: Float): Int {
        val x0 = u.toInt().coerceIn(0, w - 1)
        val x1 = (x0 + 1).coerceIn(0, w - 1)
        val y0 = v.toInt().coerceIn(0, h - 1)
        val y1 = (y0 + 1).coerceIn(0, h - 1)

        val fx = u - x0
        val fy = v - y0

        val p00 = grid[y0 * w + x0]
        val p10 = grid[y0 * w + x1]
        val p01 = grid[y1 * w + x0]
        val p11 = grid[y1 * w + x1]

        val r0 = ((p00 ushr 16) and 0xFF) * (1f - fx) + ((p10 ushr 16) and 0xFF) * fx
        val r1 = ((p01 ushr 16) and 0xFF) * (1f - fx) + ((p11 ushr 16) and 0xFF) * fx
        val r = (r0 * (1f - fy) + r1 * fy).roundToInt().coerceIn(0, 255)

        val g0 = ((p00 ushr 8) and 0xFF) * (1f - fx) + ((p10 ushr 8) and 0xFF) * fx
        val g1 = ((p01 ushr 8) and 0xFF) * (1f - fx) + ((p11 ushr 8) and 0xFF) * fx
        val g = (g0 * (1f - fy) + g1 * fy).roundToInt().coerceIn(0, 255)

        val b0 = (p00 and 0xFF) * (1f - fx) + (p10 and 0xFF) * fx
        val b1 = (p01 and 0xFF) * (1f - fx) + (p11 and 0xFF) * fx
        val b = (b0 * (1f - fy) + b1 * fy).roundToInt().coerceIn(0, 255)

        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun boxBlurHorizontal(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int) {
        val div = 2 * r + 1
        for (y in 0 until h) {
            val rowOffset = y * w
            var sumR = 0
            var sumG = 0
            var sumB = 0
            for (i in -r..r) {
                val px = i.coerceIn(0, w - 1)
                val p = src[rowOffset + px]
                sumR += (p ushr 16) and 0xFF
                sumG += (p ushr 8) and 0xFF
                sumB += p and 0xFF
            }
            for (x in 0 until w) {
                val avgR = sumR / div
                val avgG = sumG / div
                val avgB = sumB / div
                dst[rowOffset + x] = (0xFF shl 24) or (avgR shl 16) or (avgG shl 8) or avgB

                val leftPx = (x - r).coerceIn(0, w - 1)
                val rightPx = (x + r + 1).coerceIn(0, w - 1)
                val pLeft = src[rowOffset + leftPx]
                val pRight = src[rowOffset + rightPx]
                sumR += ((pRight ushr 16) and 0xFF) - ((pLeft ushr 16) and 0xFF)
                sumG += ((pRight ushr 8) and 0xFF) - ((pLeft ushr 8) and 0xFF)
                sumB += (pRight and 0xFF) - (pLeft and 0xFF)
            }
        }
    }

    private fun boxBlurVertical(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int) {
        val div = 2 * r + 1
        for (x in 0 until w) {
            var sumR = 0
            var sumG = 0
            var sumB = 0
            for (i in -r..r) {
                val py = i.coerceIn(0, h - 1)
                val p = src[py * w + x]
                sumR += (p ushr 16) and 0xFF
                sumG += (p ushr 8) and 0xFF
                sumB += p and 0xFF
            }
            for (y in 0 until h) {
                val avgR = sumR / div
                val avgG = sumG / div
                val avgB = sumB / div
                dst[y * w + x] = (0xFF shl 24) or (avgR shl 16) or (avgG shl 8) or avgB

                val topPy = (y - r).coerceIn(0, h - 1)
                val btmPy = (y + r + 1).coerceIn(0, h - 1)
                val pTop = src[topPy * w + x]
                val pBtm = src[btmPy * w + x]
                sumR += ((pBtm ushr 16) and 0xFF) - ((pTop ushr 16) and 0xFF)
                sumG += ((pBtm ushr 8) and 0xFF) - ((pTop ushr 8) and 0xFF)
                sumB += (pBtm and 0xFF) - (pTop and 0xFF)
            }
        }
    }
}
