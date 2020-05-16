package com.rycbar.holefiller

class FourConVicinityProcessor : VicinityProcessor() {
    private val lookupTable = Array(16) {mask ->
        Result(
            !mask.isOn(TOP_LEFT) && (mask.isOn(TOP_RIGHT) || mask.isOn(BOTTOM_LEFT)),
            !mask.isOn(TOP_RIGHT) && (mask.isOn(TOP_LEFT) || mask.isOn(BOTTOM_RIGHT)),
            !mask.isOn(BOTTOM_LEFT) && (mask.isOn(BOTTOM_RIGHT) || mask.isOn(TOP_LEFT)),
            !mask.isOn(BOTTOM_RIGHT) && (mask.isOn(BOTTOM_LEFT) || mask.isOn(TOP_RIGHT)))
    }

    override fun processQuartetMask(mask: Int): Result = lookupTable[mask]

    private fun Int.isOn(pixelTag: Int) = (this and pixelTag.asMask != 0)
}