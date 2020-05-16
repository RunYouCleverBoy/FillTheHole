package com.rycbar.holefiller

import kotlin.math.pow

class Point(val x: Int, val y: Int) {
    private fun sqr(x: Int) = x.toDouble().let { it * it }
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
    fun squareDistanceTo(other: Point) = sqr(x - other.x) + sqr(y - other.y)
    fun normByPower(other: Point, power: Float) = squareDistanceTo(other).pow(power / 2.0)
    override fun equals(other: Any?): Boolean =
        (other as? Point)?.let{ it.x == x && it.y == y} == true

    override fun hashCode(): Int = x shl 16 + y
}