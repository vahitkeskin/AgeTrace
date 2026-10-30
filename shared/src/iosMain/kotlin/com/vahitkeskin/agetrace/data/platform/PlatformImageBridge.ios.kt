package com.vahitkeskin.agetrace.data.platform

import androidx.compose.ui.graphics.ImageBitmap
import com.vahitkeskin.agetrace.domain.model.PixelBuffer

actual object PlatformImageBridge {

    actual fun decodeByteArrayToPixelBuffer(bytes: ByteArray): PixelBuffer? {
        // Fallback or CoreGraphics decoding for iOS target
        val size = 64
        val dummy = IntArray(size * size) { 0xFF333333.toInt() }
        return PixelBuffer(size, size, dummy)
    }

    actual fun pixelBufferToImageBitmap(pixelBuffer: PixelBuffer): ImageBitmap {
        return ImageBitmap(pixelBuffer.width, pixelBuffer.height)
    }

    actual fun openFilePicker(onFilePicked: (name: String, bytes: ByteArray) -> Unit) {
        // iOS UIImagePickerController
    }

    actual fun readBytesFromPath(path: String): ByteArray? = null

    actual fun saveImageToGallery(pixelBuffer: PixelBuffer, format: String): String? = null
}
