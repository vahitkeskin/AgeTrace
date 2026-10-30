package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberGalleryPickerLauncher(
    onImagesPicked: (List<Pair<String, ByteArray>>) -> Unit
): () -> Unit
