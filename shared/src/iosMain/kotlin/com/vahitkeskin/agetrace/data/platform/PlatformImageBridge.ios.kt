package com.vahitkeskin.agetrace.data.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.create

actual object PlatformImageBridge {

    actual fun decodeByteArrayToPixelBuffer(bytes: ByteArray): PixelBuffer? {
        if (bytes.isEmpty()) return null
        return try {
            val skiaImg = Image.makeFromEncoded(bytes)
            val w = skiaImg.width
            val h = skiaImg.height
            if (w > 0 && h > 0) {
                val skiaBmp = Bitmap.makeFromImage(skiaImg)
                val pixels = IntArray(w * h)
                for (y in 0 until h) {
                    val rowOffset = y * w
                    for (x in 0 until w) {
                        pixels[rowOffset + x] = skiaBmp.getColor(x, y)
                    }
                }
                PixelBuffer(w, h, pixels)
            } else null
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    actual fun pixelBufferToImageBitmap(pixelBuffer: PixelBuffer): ImageBitmap {
        return try {
            val w = pixelBuffer.width
            val h = pixelBuffer.height
            val bitmap = Bitmap()
            val imageInfo = ImageInfo(w, h, ColorType.BGRA_8888, ColorAlphaType.PREMUL)
            bitmap.allocPixels(imageInfo)
            
            val byteArray = ByteArray(w * h * 4)
            for (i in 0 until w * h) {
                val argb = pixelBuffer.pixels[i]
                val a = (argb shr 24 and 0xFF).toByte()
                val r = (argb shr 16 and 0xFF).toByte()
                val g = (argb shr 8 and 0xFF).toByte()
                val b = (argb and 0xFF).toByte()
                val base = i * 4
                byteArray[base] = b
                byteArray[base + 1] = g
                byteArray[base + 2] = r
                byteArray[base + 3] = a
            }
            bitmap.installPixels(imageInfo, byteArray, w * 4)
            Image.makeFromBitmap(bitmap).toComposeImageBitmap()
        } catch (t: Throwable) {
            ImageBitmap(pixelBuffer.width.coerceAtLeast(1), pixelBuffer.height.coerceAtLeast(1))
        }
    }

    actual fun openFilePicker(onFilePicked: (name: String, bytes: ByteArray) -> Unit) {
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual fun readBytesFromPath(path: String): ByteArray? {
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(path)) return null
        val data = NSData.create(contentsOfFile = path) ?: return null
        val length = data.length.toInt()
        val bytes = ByteArray(length)
        if (length > 0) {
            bytes.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
        return bytes
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual fun saveImageToGallery(pixelBuffer: PixelBuffer, format: String): String? {
        return try {
            val w = pixelBuffer.width
            val h = pixelBuffer.height
            val bitmap = Bitmap()
            val imageInfo = ImageInfo(w, h, ColorType.BGRA_8888, ColorAlphaType.PREMUL)
            bitmap.allocPixels(imageInfo)
            val byteArray = ByteArray(w * h * 4)
            for (i in 0 until w * h) {
                val argb = pixelBuffer.pixels[i]
                val a = (argb shr 24 and 0xFF).toByte()
                val r = (argb shr 16 and 0xFF).toByte()
                val g = (argb shr 8 and 0xFF).toByte()
                val b = (argb and 0xFF).toByte()
                val base = i * 4
                byteArray[base] = b
                byteArray[base + 1] = g
                byteArray[base + 2] = r
                byteArray[base + 3] = a
            }
            bitmap.installPixels(imageInfo, byteArray, w * 4)
            val skiaImage = Image.makeFromBitmap(bitmap)
            val isPng = format.equals("PNG", ignoreCase = true)
            val encodedData = skiaImage.encodeToData(
                if (isPng) org.jetbrains.skia.EncodedImageFormat.PNG else org.jetbrains.skia.EncodedImageFormat.JPEG,
                95
            ) ?: return null
            val bytes = encodedData.bytes
            val nsData = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }
            val uiImage = platform.UIKit.UIImage.imageWithData(nsData) ?: return null
            platform.UIKit.UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)
            val ext = if (isPng) "png" else "jpg"
            val timestamp = platform.posix.time(null)
            "AgeTrace_$timestamp.$ext"
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
}
