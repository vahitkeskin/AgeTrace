package com.vahitkeskin.agetrace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.agetrace.data.cv.PlatformOnnxEstimator
import com.vahitkeskin.agetrace.data.platform.AppContextHolder
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppContextHolder.context = applicationContext

        // Initialize standalone on-device ONNX Runtime model (zero-Python deep learning)
        thread(start = true, isDaemon = true, name = "OnnxInitThread") {
            try {
                assets.open("genderage.onnx").use { input ->
                    PlatformOnnxEstimator.initModel(input.readBytes())
                }
            } catch (_: Throwable) {
                // Heuristic biometrics fallback is active if asset cannot be opened
            }
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}