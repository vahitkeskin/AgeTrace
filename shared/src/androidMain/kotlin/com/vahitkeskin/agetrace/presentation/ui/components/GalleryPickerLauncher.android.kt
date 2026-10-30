package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberGalleryPickerLauncher(
    onImagesPicked: (List<Pair<String, ByteArray>>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 30)
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            val list = mutableListOf<Pair<String, ByteArray>>()
            for ((index, uri) in uris.withIndex()) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.use { it.readBytes() }
                    if (bytes != null && bytes.isNotEmpty()) {
                        list.add(Pair("galeri_${index + 1}.jpg", bytes))
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            if (list.isNotEmpty()) {
                onImagesPicked(list)
            }
        }
    }

    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}
