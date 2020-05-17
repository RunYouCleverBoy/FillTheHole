package com.rycbar.holefiller.internals

/**
 * Processor of each 2x2 points, detects border points
 */
internal abstract class VicinityProcessor {
    /**
     * Process a 2x2 pixel matrix. Designed to be cache friendly. All in place
     *
     * @return the size of data filled into resultPoints
     */
    abstract fun process(topLeftIsHole: Boolean, topRightIsHole: Boolean, bottomLeftIsHole: Boolean, bottomRightIsHole: Boolean): Result

    data class Result (
        val isTopLeftABorder: Boolean,
        val isTopRightABorder: Boolean,
        val isBottomLeftABorder: Boolean,
        val isBottomRightABorder: Boolean
    )
}
