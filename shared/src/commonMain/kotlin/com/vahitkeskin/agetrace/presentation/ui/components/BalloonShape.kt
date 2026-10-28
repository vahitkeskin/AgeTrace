package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Balloon speech bubble shape: rounded rectangle + triangular pointer arrow pointing to the anchor.
 * [isBelow] defines whether arrow is at the top (balloon is below anchor, arrow points UP)
 * or at bottom (balloon is above anchor, arrow points DOWN).
 */
class BalloonShape(
    private val arrowWidth: Float,
    private val arrowHeight: Float,
    private val arrowX: Float,
    private val isBelow: Boolean,
    private val cornerRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f) {
            return Outline.Generic(path)
        }

        val cr = cornerRadius.coerceAtMost(h / 2f).coerceAtMost(w / 4f)
        val effectiveArrowWidth = arrowWidth.coerceAtMost((w - 2f * cr).coerceAtLeast(0f))
        val safeArrowX = arrowX.coerceIn(
            cr + effectiveArrowWidth / 2f,
            (w - cr - effectiveArrowWidth / 2f).coerceAtLeast(cr + effectiveArrowWidth / 2f)
        )

        if (isBelow) {
            // Arrow is at the top pointing UP to anchor
            val top = arrowHeight.coerceAtMost(h / 2f)
            val bottom = h

            path.moveTo(cr, top)
            if (effectiveArrowWidth > 0f) {
                path.lineTo((safeArrowX - effectiveArrowWidth / 2f).coerceAtLeast(cr), top)
                path.lineTo(safeArrowX, 0f)
                path.lineTo((safeArrowX + effectiveArrowWidth / 2f).coerceAtMost(w - cr), top)
            }
            path.lineTo(w - cr, top)
            path.arcTo(Rect(w - cr * 2, top, w, top + cr * 2), -90f, 90f, false)

            path.lineTo(w, bottom - cr)
            path.arcTo(Rect(w - cr * 2, bottom - cr * 2, w, bottom), 0f, 90f, false)

            path.lineTo(cr, bottom)
            path.arcTo(Rect(0f, bottom - cr * 2, cr * 2, bottom), 90f, 90f, false)

            path.lineTo(0f, top + cr)
            path.arcTo(Rect(0f, top, cr * 2, top + cr * 2), 180f, 90f, false)

            path.close()
        } else {
            // Balloon is ABOVE anchor: arrow is at the bottom pointing DOWN to anchor
            val top = 0f
            val bottom = (h - arrowHeight).coerceAtLeast(cr * 2f)

            path.moveTo(cr, top)
            path.lineTo(w - cr, top)
            path.arcTo(Rect(w - cr * 2, top, w, top + cr * 2), -90f, 90f, false)

            path.lineTo(w, bottom - cr)
            path.arcTo(Rect(w - cr * 2, bottom - cr * 2, w, bottom), 0f, 90f, false)

            if (effectiveArrowWidth > 0f) {
                path.lineTo((safeArrowX + effectiveArrowWidth / 2f).coerceAtMost(w - cr), bottom)
                path.lineTo(safeArrowX, h)
                path.lineTo((safeArrowX - effectiveArrowWidth / 2f).coerceAtLeast(cr), bottom)
            }

            path.lineTo(cr, bottom)
            path.arcTo(Rect(0f, bottom - cr * 2, cr * 2, bottom), 90f, 90f, false)

            path.lineTo(0f, top + cr)
            path.arcTo(Rect(0f, top, cr * 2, top + cr * 2), 180f, 90f, false)

            path.close()
        }

        return Outline.Generic(path)
    }
}
