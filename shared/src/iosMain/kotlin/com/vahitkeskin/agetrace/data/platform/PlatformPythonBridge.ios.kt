package com.vahitkeskin.agetrace.data.platform

actual object PlatformPythonBridge {
    actual fun isPythonSupported(): Boolean = false
    actual suspend fun analyzeWithPython(imageBytes: ByteArray): String? = null
}
