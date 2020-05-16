package com.rycbar.holefiller

/**
 * Processor of each 2x2 points, detects border points
 */
abstract class VicinityProcessor {
    /**
     * Process a 2x2 pixel matrix. Designed to be cache friendly. All in place
     *
     * @return the size of data filled into resultPoints
     */
    fun process(topLeftIsHole: Boolean, topRightIsHole: Boolean, bottomLeftIsHole: Boolean, bottomRightIsHole: Boolean): Result {
        val bitMask = (if (topLeftIsHole) TOP_LEFT.asMask else 0) or
        (if (topRightIsHole) TOP_RIGHT.asMask else 0) or
        (if (bottomLeftIsHole) BOTTOM_LEFT.asMask else 0) or
        (if (bottomRightIsHole) BOTTOM_RIGHT.asMask else 0)

        return processQuartetMask(bitMask)
    }

    abstract fun processQuartetMask(mask: Int) : Result

    data class Result (
        val isTopLeftABorder: Boolean,
        val isTopRightABorder: Boolean,
        val isBottomLeftABorder: Boolean,
        val isBottomRightABorder: Boolean
    )

    protected inline val Int.asMask get() = 1 shl this

    companion object {
        /**
         * Mask from an int. Returns 1 << this
         *
         * On modern processors, mostly mobile, shift left costs 0 processor cycles
         */
        const val TOP_LEFT = 3
        const val TOP_RIGHT = 2
        const val BOTTOM_LEFT = 1
        const val BOTTOM_RIGHT = 0
    }
}
