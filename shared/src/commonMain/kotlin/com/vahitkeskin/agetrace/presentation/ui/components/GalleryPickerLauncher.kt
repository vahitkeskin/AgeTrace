package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberGalleryPickerLauncher(
    onImagePicked: (name: String, bytes: ByteArray) -> Unit
): () -> Unit
