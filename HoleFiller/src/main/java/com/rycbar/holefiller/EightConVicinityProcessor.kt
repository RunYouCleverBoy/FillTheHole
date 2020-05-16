package com.rycbar.holefiller

class EightConVicinityProcessor : VicinityProcessor() {
    private val lookupTable = Array(16) {mask ->
        Result(
            mask.isNotAHoleWithAtLeastOneHoleNeighbour(TOP_LEFT),
            mask.isNotAHoleWithAtLeastOneHoleNeighbour(TOP_RIGHT),
            mask.isNotAHoleWithAtLeastOneHoleNeighbour(BOTTOM_LEFT),
            mask.isNotAHoleWithAtLeastOneHoleNeighbour(BOTTOM_RIGHT))
    }

    override fun processQuartetMask(mask: Int): Result = lookupTable[mask]

    // A hole   Any other is a hole     Is Border
    //  0             0                     0
    //  0             1                     1
    //  1             0                     0
    //  1             1                     0
    private fun Int.isNotAHoleWithAtLeastOneHoleNeighbour(pixelTag: Int) : Boolean {
        val pixelMask = pixelTag.asMask
        return (this and pixelMask == 0) && (this and pixelMask.inv() != 0)
    }
}