package com.rycbar.holefiller.internals

import com.rycbar.holefiller.api.Executor
import com.rycbar.holefiller.api.Image
import java.util.concurrent.atomic.AtomicInteger

internal class InterpolationProcessor(private val image: Image, private val features: FeaturesDetector, private val weights: Weights) {
    var onFinished: () -> Unit = {}
    internal fun interpolateHole(executor: Executor, bulkSize: Int = 16) {
        executor.dispatch(bulkSize)
    }

    private fun Executor.dispatch(bulkSize: Int) {
        val overallJobsCount = features.damagedPoints.size
        val refCounter = AtomicInteger()
        do {
            val bulk = Array<Point?>(bulkSize) { features.damagedPoints.consume() }
            submit {
                val count = processPixelsBulk(bulk)
                if (refCounter.addAndGet(count) >= overallJobsCount) {
                    onFinished.invoke()
                }
            }
        } while (bulk.last() != null)
    }

    private fun processPixelsBulk(points: Array<Point?>): Int {
        var count = 0
        points.forEach { p ->
            p ?: return@forEach
            val edges = features.edges
            val numerator = edges.entries.sumByDouble { (point, colour) -> weights[point, p] * colour }
            val nominator = edges.coordinates.sumByDouble { edgePoint -> weights[edgePoint, p] }
            image[p.x, p.y] = (numerator / nominator).toFloat()
            count++
        }

        return count
    }
}