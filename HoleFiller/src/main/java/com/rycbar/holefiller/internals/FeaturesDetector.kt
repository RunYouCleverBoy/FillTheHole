package com.rycbar.holefiller.internals

import com.rycbar.holefiller.api.ConnectivityMode
import java.util.concurrent.ConcurrentLinkedQueue

internal class FeaturesDetector(connectivityMode: ConnectivityMode, private val damagedValueColour: Float) {
    lateinit var prevLine: FloatArray
    lateinit var currLine: FloatArray
    fun addLine(y: Int, row: FloatArray) {
        currLine = row

        row.forEachIndexed { x, pixel ->
            if (pixel == damagedValueColour) {
                damagedPoints.add(Point(x, y))
            }

            if (x > 0 && y > 0) {
                val (isTopLeft, isTopRight, isBottomLeft, isBottomRight) = edgeDetector.process(prevLine[x - 1] == damagedValueColour, prevLine[x] == damagedValueColour,
                    currLine[x - 1] == damagedValueColour, currLine[x] == damagedValueColour)

                if (isTopLeft) edges[Point(x - 1, y - 1)] = prevLine[x - 1]
                if (isTopRight) edges[Point(x, y - 1)] = prevLine[x]
                if (isBottomLeft) edges[Point(x - 1, y)] = currLine[x - 1]
                if (isBottomRight) edges[Point(x, y)] = currLine[x]
            }
        }

        prevLine = currLine
    }

    class EdgesInterface {
        private val repository = HashMap<Point, Float>()
        operator fun set(point: Point, value: Float) {
            repository[point] = value
        }

        val entries get() = repository.entries
        val coordinates get() = repository.keys
    }

    class DamagedPointsInterface {
        private val repository = ConcurrentLinkedQueue<Point>()
        val size get() = repository.size
        fun add(point: Point) = repository.add(point)
        fun consume() = repository.poll()
    }

    val edges = EdgesInterface()
    val damagedPoints = DamagedPointsInterface()
    private val edgeDetector = VicinityProcessorFactory.create(connectivityMode)
}