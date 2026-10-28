package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable
import com.vahitkeskin.agetrace.data.platform.PlatformImageBridge

@Composable
actual fun rememberGalleryPickerLauncher(
    onImagePicked: (name: String, bytes: ByteArray) -> Unit
): () -> Unit {
    return {
        PlatformImageBridge.openFilePicker(onImagePicked)
    }
}
