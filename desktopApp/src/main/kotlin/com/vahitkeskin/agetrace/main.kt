package com.vahitkeskin.agetrace

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vahitkeskin.agetrace.data.cv.PlatformOnnxEstimator
import java.awt.Dimension
import kotlin.concurrent.thread

fun main() {
    thread(start = true, isDaemon = true, name = "OnnxDesktopInit") {
        try {
            val resourcePath = "composeResources/agetrace.shared.generated.resources/files/genderage.onnx"
            val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
            if (stream != null) {
                val bytes = stream.use { it.readBytes() }
                PlatformOnnxEstimator.initModel(bytes)
            }
        } catch (_: Throwable) {}
    }

    application {
        val windowState = rememberWindowState(
            size = DpSize(1180.dp, 840.dp),
            position = WindowPosition.PlatformDefault
        )

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "AgeTrace • Edge AI Biyometrik Yüz & Yaş Analiz Sistemi",
            icon = painterResource("icons/icon.png")
        ) {
            window.minimumSize = Dimension(720, 600)
            App()
        }
    }
}