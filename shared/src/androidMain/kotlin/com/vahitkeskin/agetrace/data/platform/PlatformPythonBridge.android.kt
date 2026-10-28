package com.vahitkeskin.agetrace.data.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

actual object PlatformPythonBridge {

    actual fun isPythonSupported(): Boolean = true

    actual suspend fun analyzeWithPython(imageBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        // Connects to Python Server (supports Android physical device via ADB reverse and Android Emulator 10.0.2.2)
        val endpoints = listOf(
            "http://127.0.0.1:8080/detect",
            "http://10.0.2.2:8080/detect"
        )
        for (urlStr in endpoints) {
            val result = tryHttpDetect(imageBytes, urlStr)
            if (result != null) return@withContext result
        }
        null
    }

    private fun tryHttpDetect(bytes: ByteArray, urlStr: String): String? {
        return try {
            val url = URI(urlStr).toURL()
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 3000
            conn.readTimeout = 10000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.outputStream.use { it.write(bytes) }
            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                android.util.Log.e("AgeTrace", "InsightFace HTTP ${conn.responseCode} on $urlStr")
                null
            }
        } catch (e: Exception) {
            android.util.Log.d("AgeTrace", "Could not reach InsightFace on $urlStr: ${e.message}")
            null
        }
    }
}
