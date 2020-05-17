package com.rycbar.holefiller.api

class Image(private val width: Int, height: Int) {
    private val rawData: Array<FloatArray> = Array(height) { FloatArray(width) }
    operator fun get(x: Int, y: Int) = rawData[y][x]
    operator fun set(y: Int, line: FloatArray) {
        rawData[y] = line
    }

    operator fun set(x: Int, y: Int, v: Float) {
        rawData[y][x] = v
    }
}