package com.vahitkeskin.agetrace.domain.model

/**
 * Normalized bounding box coordinates (values between 0.0 and 1.0)
 * relative to the source image dimensions.
 */
data class BoundingBox(
    val normalizedX: Float,
    val normalizedY: Float,
    val normalizedWidth: Float,
    val normalizedHeight: Float
) {
    val left: Float get() = normalizedX
    val top: Float get() = normalizedY
    val right: Float get() = (normalizedX + normalizedWidth).coerceAtMost(1.0f)
    val bottom: Float get() = (normalizedY + normalizedHeight).coerceAtMost(1.0f)
    val centerX: Float get() = normalizedX + (normalizedWidth / 2f)
    val centerY: Float get() = normalizedY + (normalizedHeight / 2f)
    val aspectRatio: Float get() = if (normalizedWidth > 0f) normalizedHeight / normalizedWidth else 1.0f

    /**
     * Converts normalized coordinates to absolute pixel rect for a given canvas size.
     */
    fun toAbsoluteRect(canvasWidth: Float, canvasHeight: Float): AbsoluteRect {
        return AbsoluteRect(
            left = normalizedX * canvasWidth,
            top = normalizedY * canvasHeight,
            width = normalizedWidth * canvasWidth,
            height = normalizedHeight * canvasHeight
        )
    }

    data class AbsoluteRect(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float
    ) {
        val right: Float get() = left + width
        val bottom: Float get() = top + height
    }
}
