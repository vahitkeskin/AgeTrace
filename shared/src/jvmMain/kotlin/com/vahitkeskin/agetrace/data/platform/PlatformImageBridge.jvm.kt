package com.vahitkeskin.agetrace.data.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

actual object PlatformImageBridge {

    actual fun decodeByteArrayToPixelBuffer(bytes: ByteArray): PixelBuffer? {
        return try {
            val stream = ByteArrayInputStream(bytes)
            val bufferedImage = ImageIO.read(stream) ?: return null
            val w = bufferedImage.width
            val h = bufferedImage.height
            val rgbArray = IntArray(w * h)
            bufferedImage.getRGB(0, 0, w, h, rgbArray, 0, w)
            PixelBuffer(w, h, rgbArray)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual fun pixelBufferToImageBitmap(pixelBuffer: PixelBuffer): ImageBitmap {
        val bi = BufferedImage(pixelBuffer.width, pixelBuffer.height, BufferedImage.TYPE_INT_ARGB)
        bi.setRGB(0, 0, pixelBuffer.width, pixelBuffer.height, pixelBuffer.pixels, 0, pixelBuffer.width)
        return bi.toComposeImageBitmap()
    }

    actual fun openFilePicker(onFilePicked: (name: String, bytes: ByteArray) -> Unit) {
        try {
            val fileDialog = FileDialog(null as Frame?, "AgeTrace - Görsel Seç", FileDialog.LOAD)
            fileDialog.setFilenameFilter { _, name ->
                val lower = name.lowercase()
                lower.endsWith(".png") || lower.endsWith(".jpg") ||
                    lower.endsWith(".jpeg") || lower.endsWith(".webp")
            }
            fileDialog.isVisible = true
            val file = fileDialog.file
            val dir = fileDialog.directory
            if (file != null && dir != null) {
                val pickedFile = File(dir, file)
                if (pickedFile.exists() && pickedFile.canRead()) {
                    onFilePicked(pickedFile.name, pickedFile.readBytes())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
