package com.vahitkeskin.agetrace.domain.model

/**
 * Biometric features extracted directly from facial image pixels.
 * Uses real anthropometry, gradient analysis, and texture roughness metrics.
 */
data class FacialBiometrics(
    /**
     * Craniofacial ratio: proportion of upper face (forehead/eyes) to lower face (nose/jaw/chin).
     * Typically 0.50 - 0.65 in young individuals, expanding to 0.75 - 0.95 in mature adults.
     */
    val craniofacialRatio: Float,

    /**
     * High-frequency edge gradient density across forehead and periorbital regions.
     * Normalized between 0.0 and 1.0.
     */
    val wrinkleDensityScore: Float,

    /**
     * Local intensity variance across cheek skin patches.
     * Smooth in youth (< 0.25), rougher in mature skin (> 0.50).
     */
    val skinTextureVariance: Float,

    /**
     * Bilateral facial symmetry index (0.0 to 1.0).
     */
    val bilateralSymmetryScore: Float,

    /**
     * Centroid skin chrominance (Cb, Cr) values in YCbCr color space.
     */
    val chrominanceCentroid: Pair<Float, Float>
)
