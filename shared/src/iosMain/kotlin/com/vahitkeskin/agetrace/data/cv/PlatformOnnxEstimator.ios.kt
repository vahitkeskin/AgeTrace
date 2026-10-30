package com.vahitkeskin.agetrace.data.cv

actual object PlatformOnnxEstimator {
    actual fun isSupported(): Boolean = false
    actual fun initModel(modelBytes: ByteArray): Boolean = false
    actual fun estimate(nchwFloats: FloatArray): Pair<Int, Float>? = null
}

