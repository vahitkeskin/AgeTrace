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
            // 1. Measure raw dimensions without loading full pixels
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

            val rawW = boundsOptions.outWidth
            val rawH = boundsOptions.outHeight
            if (rawW <= 0 || rawH <= 0) return null

            var sampleSize = 1
            val maxTargetDimension = 1280
            while (rawW / sampleSize > maxTargetDimension || rawH / sampleSize > maxTargetDimension) {
                sampleSize *= 2
            }

            // 2. Decode safely with optimal inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = false
                inSampleSize = sampleSize
            }
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null

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
                    if (rotated != bitmap) {
                        bitmap.recycle()
                    }
                    bitmap = rotated
                }
            } catch (t: Throwable) {
                // Ignore EXIF parsing error and proceed with decoded bitmap
            }

            val w = bitmap.width
            val h = bitmap.height
            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
            bitmap.recycle()
            PixelBuffer(w, h, pixels)
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    actual fun pixelBufferToImageBitmap(pixelBuffer: PixelBuffer): ImageBitmap {
        return try {
            val bitmap = Bitmap.createBitmap(
                pixelBuffer.pixels,
                pixelBuffer.width,
                pixelBuffer.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.asImageBitmap()
        } catch (t: Throwable) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
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
