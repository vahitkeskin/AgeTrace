package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable
import com.vahitkeskin.agetrace.data.platform.PlatformImageBridge

@Composable
actual fun rememberCameraCaptureLauncher(
    onImageCaptured: (name: String, bytes: ByteArray) -> Unit
): () -> Unit {
    // On Desktop JVM, fallback to file picker for testing or capturing camera images
    return {
        PlatformImageBridge.openFilePicker(onImageCaptured)
    }
}
