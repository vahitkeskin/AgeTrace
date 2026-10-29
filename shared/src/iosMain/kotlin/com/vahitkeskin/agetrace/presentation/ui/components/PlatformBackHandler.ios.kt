package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS does not have a system hardware back button
}

