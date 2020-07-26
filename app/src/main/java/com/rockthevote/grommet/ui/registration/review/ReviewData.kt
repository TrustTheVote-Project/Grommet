package com.rockthevote.grommet.ui.registration.review

data class ReviewData(
    val hasReviewedAndConfirmedInfo: Boolean,
    val signature: ByteArray,
    val formLanguage: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReviewData

        if (hasReviewedAndConfirmedInfo != other.hasReviewedAndConfirmedInfo) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hasReviewedAndConfirmedInfo.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}