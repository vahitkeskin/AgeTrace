package com.vahitkeskin.agetrace.domain.model

data class CustomBlurRegion(
    val id: String,
    val normalizedX: Float,
    val normalizedY: Float,
    val normalizedWidth: Float,
    val normalizedHeight: Float
) {
    fun toBoundingBox(): BoundingBox {
        val safeX = normalizedX.coerceIn(0f, 0.95f)
        val safeY = normalizedY.coerceIn(0f, 0.95f)
        val safeW = normalizedWidth.coerceIn(0.04f, (1f - safeX).coerceAtLeast(0.04f))
        val safeH = normalizedHeight.coerceIn(0.04f, (1f - safeY).coerceAtLeast(0.04f))
        return BoundingBox(
            normalizedX = safeX,
            normalizedY = safeY,
            normalizedWidth = safeW,
            normalizedHeight = safeH
        )
    }
}

