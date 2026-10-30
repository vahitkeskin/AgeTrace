package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable
import com.vahitkeskin.agetrace.data.platform.PlatformImageBridge

@Composable
actual fun rememberGalleryPickerLauncher(
    onImagesPicked: (List<Pair<String, ByteArray>>) -> Unit
): () -> Unit {
    return {
        PlatformImageBridge.openFilePicker { name, bytes ->
            onImagesPicked(listOf(Pair(name, bytes)))
        }
    }
}
