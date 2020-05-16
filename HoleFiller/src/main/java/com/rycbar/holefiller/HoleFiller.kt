package com.rycbar.holefiller

/**
 * This is a hole filler engine. Given an image, it completes pixels marked damaged by omni-directional interpolation,
 *  when the stencil is computed by proximity weights
 *
 *
 * Things left out of scope: Currently, the engine doesn't handle more than one hole. It does not handle affiliation
 *  and interpolation by a specific hole.
 *
 */
class HoleFiller(configuration: Configuration) {
    data class Configuration(val normAdditionCoefficient: Double, val normExponent: Float, val connectivityMode: ConnectivityMode)

    /**
     * Import an image, detect the edges of each hole.
     *
     * @param width of the image to import
     * @param height of the image to import
     * @param damagedValueColour Value for a pixel that's considered a hole
     * @param pixelImporter Clause to provide the value of a pixel in coordinates [0 <= x < width], [0 <= y < height]
     *
     *
     */
    fun importImage(width: Int, height: Int, damagedValueColour:Float, pixelImporter:(x: Int, y: Int) -> Float) {
        val importedImage = Image(width, height)
        lateinit var prevLine: FloatArray
        lateinit var currLine: FloatArray
        for (y in 0 until height) {
            currLine = FloatArray(width)
            for (x in 0 until width) {
                currLine[x] = pixelImporter.invoke(x, y).also { if (it == damagedValueColour) {
                    holePoints.add(Point(x, y))
                } }

                // Avoid a second pass, find edges on the fly
                if (y > 1 && x > 1) {
                    val (isTopLeft, isTopRight, isBottomLeft, isBottomRight) = edgeDetector.process(
                        prevLine[x-1] == damagedValueColour,
                        prevLine[x] == damagedValueColour,
                        currLine[x-1] == damagedValueColour,
                        currLine[x] == damagedValueColour)

                    if (isTopLeft) edges.add(Point(x-1, y-1))
                    if (isTopRight) edges.add(Point(x, y-1))
                    if (isBottomLeft) edges.add(Point(x-1, y))
                    if (isBottomRight) edges.add(Point(x, y))
                }
            }
            importedImage[y] = currLine
            prevLine = currLine
        }
        image = importedImage
    }

    private val edges = HashSet<Point>()
    private val holePoints = HashSet<Point>()
    private val edgeDetector = VicinityProcessorFactory.create(configuration.connectivityMode)
    private var image: Image? = null
    private val normCache = WeightCache(configuration.normAdditionCoefficient, configuration.normExponent)
}
