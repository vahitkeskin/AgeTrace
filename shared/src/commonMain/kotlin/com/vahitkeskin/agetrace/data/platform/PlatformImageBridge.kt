package com.vahitkeskin.agetrace.data.platform

import androidx.compose.ui.graphics.ImageBitmap
import com.vahitkeskin.agetrace.domain.model.PixelBuffer

expect object PlatformImageBridge {
    fun decodeByteArrayToPixelBuffer(bytes: ByteArray): PixelBuffer?
    fun pixelBufferToImageBitmap(pixelBuffer: PixelBuffer): ImageBitmap
    fun openFilePicker(onFilePicked: (name: String, bytes: ByteArray) -> Unit)
}
