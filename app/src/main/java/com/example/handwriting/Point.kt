package com.example.handwriting

/**
 * Represents a single touch point recorded during handwriting.
 * Includes coordinates, timestamp, and optional touch pressure.
 */
data class Point(
    val x: Float,
    val y: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val pressure: Float = 1.0f
) {
    fun distanceTo(other: Point): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
