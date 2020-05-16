package com.rycbar.holefiller

import kotlin.math.abs
import kotlin.math.min

/**
 * Weights calculator and cache. Given 2 points, the weights calculator can calculate
 *   the weights, and do this only once per every d where d = (|u.x-v.x| , |u.y-v.y|) for any u,v
 *   in the image coordinate space.
 *
 * @param epsilon e factor in the equation below.
 * @param power z factor in the equation below.
 *
 * Weights are calculated as w(u, v) = 1/(||u, v||^z + e)
 *
 */
class Weights(private val epsilon: Double, private val power: Float) {
    private val repository=HashMap<Int, Double>()
    private fun deltaHash(p1: Point, p2: Point) = abs(p1.x - p2.x) shl 16 or abs(p1.y - p2.y)
    operator fun get(p1: Point, p2: Point) : Double {
        val key = min(deltaHash(p1, p2), deltaHash(p2, p1)) // || v1 - v2 || = || v2 - v1 || so no need to store both
        val cached = repository[key]
        if (cached != null) return cached
        return (1.0/(p1.normByPower(p1, power) + epsilon)).also { repository[key] = it }
    }
}