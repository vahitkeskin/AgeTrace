package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable

/**
 * Multiplatform launcher for capturing photos directly with the device camera.
 * Requests camera permissions on platforms that require runtime authorization (e.g. Android).
 */
@Composable
expect fun rememberCameraCaptureLauncher(
    onImageCaptured: (name: String, bytes: ByteArray) -> Unit
): () -> Unit
