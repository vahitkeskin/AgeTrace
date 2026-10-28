package com.vahitkeskin.agetrace.data.platform

expect object PlatformPythonBridge {
    suspend fun analyzeWithPython(imageBytes: ByteArray): String?
    fun isPythonSupported(): Boolean
}
