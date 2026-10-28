package com.vahitkeskin.agetrace.domain.model

/**
 * Standardized biometric age bracket classification.
 */
enum class AgeClassification(
    val title: String,
    val rangeLabel: String,
    val isAdult: Boolean
) {
    MINOR(title = "18- Minor", rangeLabel = "0 - 17 Years", isAdult = false),
    YOUNG_ADULT(title = "18+ Young Adult", rangeLabel = "18 - 29 Years", isAdult = true),
    ADULT(title = "18+ Adult", rangeLabel = "30 - 49 Years", isAdult = true),
    MATURE(title = "18+ Mature", rangeLabel = "50 - 64 Years", isAdult = true),
    SENIOR(title = "18+ Senior", rangeLabel = "65+ Years", isAdult = true);

    companion object {
        fun fromAge(age: Int): AgeClassification = when {
            age < 18 -> MINOR
            age in 18..29 -> YOUNG_ADULT
            age in 30..49 -> ADULT
            age in 50..64 -> MATURE
            else -> SENIOR
        }
    }
}
