package com.vahitkeskin.agetrace

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
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