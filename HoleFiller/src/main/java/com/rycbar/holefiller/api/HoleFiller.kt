package com.rycbar.holefiller.api

import com.rycbar.holefiller.internals.FeaturesDetector
import com.rycbar.holefiller.internals.FixedThreadPoolExecutor
import com.rycbar.holefiller.internals.InterpolationProcessor
import com.rycbar.holefiller.internals.Weights

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
 * 3. Reuse. Reload the image before each use. Assumption: No need to call a damaged image twice.
 *
 */
class HoleFiller(configuration: Configuration) {
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
    data class Configuration(val normAdditionCoefficient: Double, val normExponent: Float, val connectivityMode: ConnectivityMode, val damagedValueColour: Float = -1f)

    /**
     * Heal the image by interpolation
     *
     * @param width of the image to import
     * @param height of the image to import
     * @param pixelConverter Clause to provide the value of a pixel in coordinates [0 <= x < width], [0 <= y < height].
     *  Use the [Configuration.damagedValueColour] value to designate a damaged pixel.
     *
     */
    fun heal(width: Int, height: Int, pixelConverter: (x: Int, y: Int) -> Float, onDone:(Image) -> Unit) {
        println("Importing image")
        importImage(width, height, pixelConverter)

        val executor = advancedParamsInterface.executorManager.generator.invoke()
        interpolationProcessor.onFinished = {
            println("Finished healing, cleaning up")
            advancedParamsInterface.executorManager.destructor.invoke(executor)
            onDone.invoke(image)
        }

        println("Interpolating the hole")
        interpolationProcessor.interpolateHole(executor, 16)
    }

    /**
     * Advanced configuration:
     *   Executor: Thread pool to launch jobs, or GPU jobs distributor
     *
     */
    @Suppress("unused") class AdvancedParamsInterface {
        data class ExecutorBehaviour(val generator: () -> Executor, val destructor: (Executor) -> Unit)
        @Suppress("MemberVisibilityCanBePrivate")
        internal var executorManager = ExecutorBehaviour({ FixedThreadPoolExecutor(8) }, { executor -> executor.shutdown() })

        fun setThreadPoolExecutor(size: Int) {
            executorManager = ExecutorBehaviour({ FixedThreadPoolExecutor(size) }, { executor -> executor.shutdown() })
        }
    }

    /**
     * Import an image, detect the edges of each hole.
     *
     * @param width of the image to import
     * @param height of the image to import
     * @param pixelImporter Clause to provide the value of a pixel in coordinates [0 <= x < width], [0 <= y < height]
     *
     */
    private fun importImage(width: Int, height: Int, pixelImporter: (x: Int, y: Int) -> Float) {
        val importedImage = Image(width, height).also { image = it }

        for (y in 0 until height) {
            val row = FloatArray(width) { x -> pixelImporter.invoke(x, y) }
            importedImage[y] = row
            features.addLine(y, row)
        }

        image = importedImage
    }

    private val features = FeaturesDetector(configuration.connectivityMode, configuration.damagedValueColour)
    private val weights = Weights(configuration.normAdditionCoefficient, configuration.normExponent)
    private val interpolationProcessor by lazy {
        InterpolationProcessor(image, features, weights)
    }

    private val advancedParamsInterface by lazy { AdvancedParamsInterface() }
    private lateinit var image: Image
}
