package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop platform does not have a system back button
}
