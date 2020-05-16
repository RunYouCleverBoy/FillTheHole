package com.rycbar.holefiller

import kotlin.math.min

class WeightCache(private val epsilon: Double, private val power: Float) {
    private val repository=HashMap<Int, Double>()
    private fun deltaHash(p1: Point, p2: Point) = (p1.x - p2.x) shl 16 or (p1.y - p2.y)
    fun weight(p1: Point, p2: Point) : Double {
        val key = min(deltaHash(p1, p2), deltaHash(p2, p1))
        val cached = repository[key]
        if (cached != null) return cached
        return (1.0/(p1.normByPower(p1, power) + epsilon)).also { repository[key] = it }
    }
}