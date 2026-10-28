package com.vahitkeskin.agetrace.data.cv

import kotlin.math.abs

/**
 * High performance color space transformations and biological skin chrominance segmentation.
 */
object ColorSpaceConverter {

    /**
     * Converts RGB to YCbCr and determines if the pixel falls within
     * the human skin chrominance cluster (Chai & Ngan, Kovac et al.).
     */
    fun isSkinPixel(r: Int, g: Int, b: Int): Boolean {
        // YCbCr Conversion (ITU-R BT.601)
        val cb = (-0.168736f * r - 0.331264f * g + 0.500000f * b + 128f)
        val cr = (0.500000f * r - 0.418688f * g - 0.081312f * b + 128f)

        // Chrominance boundaries covering diverse human skin tones
        val chrominanceMatch = (cr in 132.0f..176.0f) && (cb in 76.0f..128.0f)

        // RGB color balance validation
        val rgbMatch = (r > g) && (g > b) && ((r - g) >= 12) && (abs(r - g) > 10)

        return chrominanceMatch && rgbMatch
    }

    /**
     * Returns the normalized luminance (0.0 to 1.0) of an RGB pixel.
     */
    fun luminance(r: Int, g: Int, b: Int): Float {
        return (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f
    }
}
