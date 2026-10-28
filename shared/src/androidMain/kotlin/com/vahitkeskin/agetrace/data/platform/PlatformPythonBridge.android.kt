package com.vahitkeskin.agetrace.data.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI

actual object PlatformPythonBridge {

    actual fun isPythonSupported(): Boolean = true

    actual suspend fun analyzeWithPython(imageBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        val optimizedBytes = prepareOptimizedBytes(imageBytes)

        // Connects to Python Server (supports Android physical device via ADB reverse, local Wi-Fi LAN, and Android Emulator)
        val endpoints = listOf(
            "http://127.0.0.1:8080/detect",
            "http://192.168.1.100:8080/detect",
            "http://10.0.2.2:8080/detect"
        )
        for (urlStr in endpoints) {
            val result = tryHttpDetect(optimizedBytes, urlStr)
            if (result != null) return@withContext result
        }
        null
    }

    private fun prepareOptimizedBytes(originalBytes: ByteArray): ByteArray {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size, boundsOptions)
            val origW = boundsOptions.outWidth
            val origH = boundsOptions.outHeight

            if (origW <= 0 || origH <= 0 || (origW <= 1280 && origH <= 1280 && originalBytes.size < 1_500_000)) {
                return originalBytes
            }

            var inSampleSize = 1
            val maxDim = maxOf(origW, origH)
            while (maxDim / inSampleSize > 1600) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val bmp = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size, decodeOptions)
                ?: return originalBytes

            val outStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
            bmp.recycle()
            outStream.toByteArray()
        } catch (e: Exception) {
            originalBytes
        }
    }

    private fun tryHttpDetect(bytes: ByteArray, urlStr: String): String? {
        return try {
            val url = URI(urlStr).toURL()
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 4000
            conn.readTimeout = 30000 // Generous timeout for deep learning inference
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.outputStream.use { it.write(bytes) }
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                android.util.Log.i("AgeTrace", "InsightFace response length: ${response.length}")
                response
            } else {
                android.util.Log.e("AgeTrace", "InsightFace HTTP ${conn.responseCode} on $urlStr")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AgeTrace", "Could not reach InsightFace on $urlStr: ${e.message}")
            null
        }
    }
}
