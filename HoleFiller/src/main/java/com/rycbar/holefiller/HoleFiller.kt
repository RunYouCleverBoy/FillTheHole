package com.rycbar.holefiller

import java.util.concurrent.*

/**
 * This is a hole filler engine. Given an image, it completes pixels marked damaged by omni-directional interpolation,
 *  when the stencil is computed by proximity weights
 *
 *
 * Things left out of scope:
 *
 * 1. Currently, the library doesn't handle more than connected element of hole. It does not handle affiliation
 *  and interpolation by a specific hole.
 *
 *  TODO: For that, define a Union-Find, add to it by geometric reachability,
 *  u and v are in the same union iff there is a path of adjacent damaged colour pixels between u and v.
 *  That is Ui..Un+1 = v where |Uj.x - Uj+1.x| <= 1 and |Uj.y - Uj+1.y| <= 1 and Colour(Uj) = damagedValueColour
 *
 * 2. A pixel colour is Float, and is monochromatic.
 *
 *
 */
class HoleFiller(private val configuration: Configuration) {
    /**
     * Configuration for the lib:
     *
     * Margin weights are calculated as 1/(|| u - v ||^z + e)
     *
     * @param normAdditionCoefficient the e factor above >= 0
     * @param normExponent the z factor above, > 0
     * @param connectivityMode 8 connectivity or 4 connectivity
     * @param damagedValueColour a value to be recognised as "damaged" pixel colour.
     */
    data class Configuration(
        val normAdditionCoefficient: Double,
        val normExponent: Float,
        val connectivityMode: ConnectivityMode,
        val damagedValueColour: Float = -1f,
        val threadPoolSize: Int = 8
    )

    /**
     * Heal the image
     *
     * @param width of the image to import
     * @param height of the image to import
     * @param pixelImporter Clause to provide the value of a pixel in coordinates [0 <= x < width], [0 <= y < height].
     *  Use the [Configuration.damagedValueColour] value to designate a damaged pixel.
     *
     */
    fun heal(width: Int, height: Int, pixelImporter: (x: Int, y: Int) -> Float) : Image {
        importImage(width, height, configuration.damagedValueColour, pixelImporter)
        interpolateHole()
        return image
    }

    /**
     * Import an image, detect the edges of each hole.
     *
     * @param width of the image to import
     * @param height of the image to import
     * @param damagedValueColour Value for a pixel that's considered a hole
     * @param pixelImporter Clause to provide the value of a pixel in coordinates [0 <= x < width], [0 <= y < height]
     *
     */
    private fun importImage(width: Int, height: Int, damagedValueColour:Float, pixelImporter:(x: Int, y: Int) -> Float) {
        val importedImage = Image(width, height).also { image = it }
        lateinit var prevLine: FloatArray
        lateinit var currLine: FloatArray
        for (y in 0 until height) {
            currLine = FloatArray(width)
            for (x in 0 until width) {
                currLine[x] = pixelImporter.invoke(x, y).also { if (it == damagedValueColour) {
                    damagedPoints.add(Point(x, y))
                } }

                // Avoid a second pass, find edges on the fly
                if (y > 1 && x > 1) {
                    val (isTopLeft, isTopRight, isBottomLeft, isBottomRight) = edgeDetector.process(
                        prevLine[x-1] == damagedValueColour,
                        prevLine[x] == damagedValueColour,
                        currLine[x-1] == damagedValueColour,
                        currLine[x] == damagedValueColour)

                    if (isTopLeft) edges[Point(x-1, y-1)] = prevLine[x-1]
                    if (isTopRight) edges[Point(x, y-1)] = prevLine[x]
                    if (isBottomLeft) edges[Point(x-1, y)] = currLine[x-1]
                    if (isBottomRight) edges[Point(x, y)] = currLine[x]
                }
            }
            importedImage[y] = currLine
            prevLine = currLine
        }
        image = importedImage
    }

    /**
     * Interpolate the damagedPixels based on the edges collected in [importImage] and the interpolation algorithm
     */
    private fun interpolateHole() {
        val threadPool: ExecutorService? = if (configuration.threadPoolSize > 0) Executors.newFixedThreadPool(configuration.threadPoolSize) else null
        val finishLatch = CountDownLatch(damagedPoints.size)
        do {
            val p = damagedPoints.poll()
            if (p != null) {
                if (threadPool != null) {
                    threadPool.submit {
                        processPixel(p)
                        finishLatch.countDown()
                    }
                } else {
                    processPixel(p)
                }
            }
        } while (p != null)

        if (threadPool != null) finishLatch.await()
        threadPool?.shutdown()
    }

    private fun processPixel(p: Point) {
        val numerator = edges.entries.sumByDouble { (point, colour) -> weights[point, p] * colour }
        val nominator = edges.keys.sumByDouble { edgePoint -> weights[edgePoint, p] }
        image[p.x, p.y] = (numerator / nominator).toFloat()
    }

    private val edges = HashMap<Point, Float>()
    private val damagedPoints = ConcurrentLinkedQueue<Point>()
    private val edgeDetector = VicinityProcessorFactory.create(configuration.connectivityMode)
    private lateinit var image: Image
    private val weights = Weights(configuration.normAdditionCoefficient, configuration.normExponent)
}
