package com.vahitkeskin.agetrace.data.cv

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.vahitkeskin.agetrace.data.platform.AppContextHolder
import java.nio.FloatBuffer

actual object PlatformOnnxEstimator {
    private var session: OrtSession? = null
    private val env by lazy { OrtEnvironment.getEnvironment() }
    private var isInitialized = false

    private fun ensureInitialized() {
        if (session != null) return
        try {
            val ctx = AppContextHolder.context
            if (ctx != null) {
                ctx.assets.open("genderage.onnx").use { input ->
                    initModel(input.readBytes())
                }
            }
        } catch (_: Throwable) {}
    }

    actual fun isSupported(): Boolean {
        if (!isInitialized || session == null) {
            ensureInitialized()
        }
        return isInitialized && session != null
    }

    actual fun initModel(modelBytes: ByteArray): Boolean {
        return try {
            if (session == null) {
                val opts = OrtSession.SessionOptions().apply {
                    setIntraOpNumThreads(2)
                }
                session = env.createSession(modelBytes, opts)
            }
            isInitialized = true
            true
        } catch (t: Throwable) {
            println("[PlatformOnnxEstimator] Init failed: ${t.message}")
            false
        }
    }

    actual fun estimate(nchwFloats: FloatArray): Pair<Int, Float>? {
        val sess = session ?: return null
        return try {
            val buffer = FloatBuffer.wrap(nchwFloats)
            val tensor = OnnxTensor.createTensor(env, buffer, longArrayOf(1, 3, 96, 96))
            val result = sess.run(mapOf("data" to tensor))
            val output = result[0].value
            tensor.close()
            result.close()

            val row = when (output) {
                is Array<*> -> {
                    when (val first = output[0]) {
                        is FloatArray -> first
                        is Array<*> -> FloatArray(first.size) { (first[it] as Number).toFloat() }
                        else -> null
                    }
                }
                is FloatArray -> output
                else -> null
            } ?: return null

            if (row.size < 3) return null
            val femaleScore = row[0]
            val maleScore = row[1]
            val gender = if (maleScore > femaleScore) 1 else 0
            val rawAge = row[2] * 100f
            Pair(gender, rawAge)
        } catch (t: Throwable) {
            println("[PlatformOnnxEstimator] Inference error: ${t.message}")
            null
        }
    }
}

