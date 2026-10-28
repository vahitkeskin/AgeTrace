package com.vahitkeskin.agetrace.data.cv

import com.vahitkeskin.agetrace.domain.model.PixelBuffer

/**
 * Pure Kotlin encoder for standard 32-bit uncompressed Windows Bitmap (BMP) format.
 * Produces images readable natively by ImageIO, Android BitmapFactory, and Skia.
 */
object BmpEncoder {

    fun encode(buffer: PixelBuffer): ByteArray {
        val procBuffer = if (buffer.width > 1280 || buffer.height > 1280) {
            val scale = 1280f / maxOf(buffer.width, buffer.height)
            buffer.downsample((buffer.width * scale).toInt(), (buffer.height * scale).toInt())
        } else buffer

        val width = procBuffer.width
        val height = procBuffer.height
        val rowSize = width * 4
        val pixelDataSize = rowSize * height
        val fileSize = 54 + pixelDataSize

        val bytes = ByteArray(fileSize)

        // 1. BMP Header (14 bytes)
        bytes[0] = 'B'.code.toByte()
        bytes[1] = 'M'.code.toByte()
        writeInt32LE(bytes, 2, fileSize)
        writeInt16LE(bytes, 6, 0) // Reserved 1
        writeInt16LE(bytes, 8, 0) // Reserved 2
        writeInt32LE(bytes, 10, 54) // Offset to pixel array

        // 2. DIB Header (BITMAPINFOHEADER - 40 bytes)
        writeInt32LE(bytes, 14, 40) // Header size
        writeInt32LE(bytes, 18, width)
        writeInt32LE(bytes, 22, -height) // Negative for top-down raster order
        writeInt16LE(bytes, 26, 1) // Color planes
        writeInt16LE(bytes, 28, 32) // Bits per pixel (32-bit BGRA)
        writeInt32LE(bytes, 30, 0) // Compression (BI_RGB = uncompressed)
        writeInt32LE(bytes, 34, pixelDataSize)
        writeInt32LE(bytes, 38, 2835) // Horizontal resolution (72 DPI)
        writeInt32LE(bytes, 42, 2835) // Vertical resolution (72 DPI)
        writeInt32LE(bytes, 46, 0) // Colors in color palette
        writeInt32LE(bytes, 50, 0) // Important colors

        // 3. Pixel Data (BGRA format)
        var byteOffset = 54
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = buffer.getPixel(x, y)
                val a = (p ushr 24) and 0xFF
                val r = (p ushr 16) and 0xFF
                val g = (p ushr 8) and 0xFF
                val b = p and 0xFF

                bytes[byteOffset++] = b.toByte()
                bytes[byteOffset++] = g.toByte()
                bytes[byteOffset++] = r.toByte()
                bytes[byteOffset++] = a.toByte()
            }
        }

        return bytes
    }

    private fun writeInt16LE(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    }

    private fun writeInt32LE(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }
}
