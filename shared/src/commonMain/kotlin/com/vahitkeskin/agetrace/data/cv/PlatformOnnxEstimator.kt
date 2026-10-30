package com.vahitkeskin.agetrace.data.cv

/**
 * Cross-platform interface for standalone on-device ONNX Runtime deep learning inference.
 * Runs InsightFace GenderAge ONNX model directly on mobile CPU/NPU with ZERO Python dependency.
 */
expect object PlatformOnnxEstimator {
    fun isSupported(): Boolean
    fun initModel(modelBytes: ByteArray): Boolean
    fun estimate(nchwFloats: FloatArray): Pair<Int, Float>?
}

