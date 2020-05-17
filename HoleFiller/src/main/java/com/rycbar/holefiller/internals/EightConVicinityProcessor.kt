package com.rycbar.holefiller.internals

/**
 * Eight connectivity edge detector by 4 pixels
 *
 * A pixel is an edge iff it is not a hole and any of its neighbours is a hole
 *
 * In order to implement it cache-friendly, vicinity is detected by 4 points.
 *   for every 2x2, check connectivity and add border points for each point found to be a border.
 *
 * NOTE: This is a more readable implementation, but less efficient. For faster implementation
 *   convert the 4 points to a bitmask and use a lookup table
 */
internal class EightConVicinityProcessor : VicinityProcessor() {
    override fun process(topLeftIsHole: Boolean, topRightIsHole: Boolean, bottomLeftIsHole: Boolean, bottomRightIsHole: Boolean) =
        Result(isTopLeftABorder = !topLeftIsHole && (topRightIsHole || bottomLeftIsHole || bottomRightIsHole),
            isTopRightABorder = !topRightIsHole && (topLeftIsHole || bottomRightIsHole || bottomLeftIsHole),
            isBottomLeftABorder = !bottomLeftIsHole && (bottomRightIsHole || topLeftIsHole || topRightIsHole),
            isBottomRightABorder = !bottomRightIsHole && (bottomLeftIsHole || topRightIsHole || topLeftIsHole))
}