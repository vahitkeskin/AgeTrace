package com.vahitkeskin.agetrace.data.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.vahitkeskin.agetrace.domain.model.PixelBuffer

actual object PlatformImageBridge {

    actual fun decodeByteArrayToPixelBuffer(bytes: ByteArray): PixelBuffer? {
        if (bytes.isEmpty()) return null
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = false
            }
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null

            // Read EXIF Orientation and rotate bitmap so portrait photos are always upright
            try {
                val exif = android.media.ExifInterface(java.io.ByteArrayInputStream(bytes))
                val orientation = exif.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL
                )
                val rotationDegrees = when (orientation) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                if (rotationDegrees != 0f) {
                    val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
                    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    bitmap = rotated
                }
            } catch (ex: Exception) {
                // Ignore EXIF parsing error and proceed with decoded bitmap
            }

            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            PixelBuffer(w, h, pixels)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual fun pixelBufferToImageBitmap(pixelBuffer: PixelBuffer): ImageBitmap {
        val bitmap = Bitmap.createBitmap(
            pixelBuffer.pixels,
            pixelBuffer.width,
            pixelBuffer.height,
            Bitmap.Config.ARGB_8888
        )
        return bitmap.asImageBitmap()
    }

    actual fun openFilePicker(onFilePicked: (name: String, bytes: ByteArray) -> Unit) {
        // Handled via rememberGalleryPickerLauncher in Android UI
    }

    actual fun readBytesFromPath(path: String): ByteArray? {
        return try {
            val file = java.io.File(path)
            if (file.exists() && file.canRead()) {
                file.readBytes()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
