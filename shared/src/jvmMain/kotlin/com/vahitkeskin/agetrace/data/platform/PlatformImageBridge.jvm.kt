package com.vahitkeskin.agetrace.data.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

actual object PlatformImageBridge {

    /**
     * Decodes raw image bytes supporting WebP, PNG, JPEG, and BMP.
     * Uses Skia native C++ rasterizer with ImageIO fallback.
     */
    actual fun decodeByteArrayToPixelBuffer(bytes: ByteArray): PixelBuffer? {
        if (bytes.isEmpty()) return null

        // 1. Primary High-Performance Decoder: Skia (Natively decodes WebP, PNG, JPG, BMP)
        try {
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
                return PixelBuffer(w, h, pixels)
            }
        } catch (e: Throwable) {
            // Skia decoding exception, fallback to ImageIO
        }

        // 2. Secondary Decoder: Standard ImageIO
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
        SwingUtilities.invokeLater {
            try {
                // Ensure macOS native properties
                System.setProperty("apple.awt.fileDialogForDirectories", "false")

                val fileDialog = FileDialog(null as Frame?, "AgeTrace - Görsel Seç (PNG, JPG, WEBP)", FileDialog.LOAD)
                // Note: Do NOT call setFilenameFilter on macOS as it disables file selection
                fileDialog.setFile("*.png;*.jpg;*.jpeg;*.webp;*.bmp")
                fileDialog.isVisible = true

                val fileName = fileDialog.file
                val directory = fileDialog.directory

                if (fileName != null && directory != null) {
                    val pickedFile = File(directory, fileName)
                    if (pickedFile.exists() && pickedFile.canRead()) {
                        val bytes = pickedFile.readBytes()
                        if (bytes.isNotEmpty()) {
                            onFilePicked(pickedFile.name, bytes)
                            return@invokeLater
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fallback: Swing JFileChooser
            try {
                val chooser = JFileChooser()
                chooser.dialogTitle = "AgeTrace - Görsel Seç"
                chooser.fileFilter = FileNameExtensionFilter(
                    "Görseller (*.png, *.jpg, *.jpeg, *.webp, *.bmp)",
                    "png", "jpg", "jpeg", "webp", "bmp"
                )
                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val file = chooser.selectedFile
                    if (file != null && file.exists() && file.canRead()) {
                        val bytes = file.readBytes()
                        if (bytes.isNotEmpty()) {
                            onFilePicked(file.name, bytes)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    actual fun readBytesFromPath(path: String): ByteArray? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.readBytes()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
