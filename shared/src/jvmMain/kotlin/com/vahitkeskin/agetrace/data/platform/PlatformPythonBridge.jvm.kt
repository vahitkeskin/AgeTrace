package com.vahitkeskin.agetrace.data.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

actual object PlatformPythonBridge {

    actual fun isPythonSupported(): Boolean = true

    actual suspend fun analyzeWithPython(imageBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        // 1. Try local HTTP Python server if active at port 8080
        val httpResult = tryHttpDetect(imageBytes, "http://127.0.0.1:8080/detect")
        if (httpResult != null) return@withContext httpResult

        // 2. Otherwise invoke python3 CLI script directly via ProcessBuilder
        try {
            val tempFile = File.createTempFile("agetrace_input_", ".png")
            tempFile.writeBytes(imageBytes)

            val candidatePaths = listOf(
                File("python/age_trace_detector.py"),
                File("../python/age_trace_detector.py"),
                File("AgeTrace/python/age_trace_detector.py")
            )
            val scriptFile = candidatePaths.find { it.exists() } ?: File("python/age_trace_detector.py")

            val process = ProcessBuilder("python3", scriptFile.absolutePath, tempFile.absolutePath)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            tempFile.delete()

            if (exitCode == 0 && output.contains("\"faces\"")) {
                output
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun tryHttpDetect(bytes: ByteArray, urlStr: String): String? {
        return try {
            val url = URI(urlStr).toURL()
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 1200
            conn.readTimeout = 3000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/octet-stream")
            conn.outputStream.use { it.write(bytes) }
            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
