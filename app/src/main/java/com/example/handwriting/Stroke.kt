package com.example.handwriting

import android.graphics.RectF

/**
 * Represents a single continuous stroke drawn on the canvas
 * from finger touchdown to finger lift.
 */
data class Stroke(
    val points: List<Point> = emptyList()
) {
    val isEmpty: Boolean get() = points.isEmpty()
    val size: Int get() = points.size

    val startPoint: Point? get() = points.firstOrNull()
    val endPoint: Point? get() = points.lastOrNull()

    val durationMs: Long
        get() {
            if (points.size < 2) return 0L
            return points.last().timestamp - points.first().timestamp
        }

    val totalLength: Float
        get() {
            if (points.size < 2) return 0f
            var len = 0f
            for (i in 0 until points.size - 1) {
                len += points[i].distanceTo(points[i + 1])
            }
            return len
        }

    val boundingBox: RectF
        get() {
            if (points.isEmpty()) return RectF(0f, 0f, 0f, 0f)
            var minX = points[0].x
            var maxX = points[0].x
            var minY = points[0].y
            var maxY = points[0].y
            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
            return RectF(minX, minY, maxX, maxY)
        }
}
