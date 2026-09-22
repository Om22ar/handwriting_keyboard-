package com.example.handwriting

import android.graphics.RectF
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

/**
 * Normalizes handwritten strokes into a standard coordinate space and extracts
 * geometric and topological features for recognition.
 */
object StrokeNormalizer {

    /**
     * Resamples a stroke into [targetPointCount] equidistant points along its path.
     */
    fun resample(stroke: Stroke, targetPointCount: Int = 32): Stroke {
        val points = stroke.points
        if (points.size <= 1) return stroke

        val totalLength = stroke.totalLength
        if (totalLength <= 0f) return stroke

        val segmentInterval = totalLength / (targetPointCount - 1)
        val resampled = mutableListOf<Point>()
        resampled.add(points.first())

        var accumulatedDist = 0f
        var currentIdx = 0
        var currentPoint = points[0]

        while (currentIdx < points.size - 1 && resampled.size < targetPointCount) {
            val nextPoint = points[currentIdx + 1]
            val d = currentPoint.distanceTo(nextPoint)

            if (accumulatedDist + d >= segmentInterval) {
                val t = (segmentInterval - accumulatedDist) / d
                val interpX = currentPoint.x + t * (nextPoint.x - currentPoint.x)
                val interpY = currentPoint.y + t * (nextPoint.y - currentPoint.y)
                val interpPoint = Point(interpX, interpY)
                resampled.add(interpPoint)
                currentPoint = interpPoint
                accumulatedDist = 0f
            } else {
                accumulatedDist += d
                currentIdx++
                currentPoint = nextPoint
            }
        }

        while (resampled.size < targetPointCount) {
            resampled.add(points.last())
        }

        return Stroke(resampled)
    }

    /**
     * Normalizes a collection of strokes into a unit bounding box [0..1, 0..1],
     * preserving aspect ratio and centering within the box.
     */
    fun normalizeToUnitBox(strokes: List<Stroke>): List<Stroke> {
        if (strokes.isEmpty()) return emptyList()

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        var hasPoints = false
        for (stroke in strokes) {
            for (p in stroke.points) {
                hasPoints = true
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
        }

        if (!hasPoints) return emptyList()

        val width = maxX - minX
        val height = maxY - minY
        val maxDim = max(width, height)

        if (maxDim <= 0.001f) {
            // Degenerate single point
            return strokes.map { s ->
                Stroke(s.points.map { Point(0.5f, 0.5f, it.timestamp, it.pressure) })
            }
        }

        // Center within [0, 1]
        val offsetX = (maxDim - width) / 2f
        val offsetY = (maxDim - height) / 2f

        return strokes.map { stroke ->
            Stroke(
                stroke.points.map { p ->
                    val normX = ((p.x - minX) + offsetX) / maxDim
                    val normY = ((p.y - minY) + offsetY) / maxDim
                    Point(normX, normY, p.timestamp, p.pressure)
                }
            )
        }
    }

    /**
     * Extracts normalized high-level geometric features from strokes.
     */
    fun extractFeatures(strokes: List<Stroke>): StrokeFeatures {
        if (strokes.isEmpty()) {
            return StrokeFeatures(
                strokeCount = 0,
                aspectRatio = 1f,
                isClosedLoop = false,
                hasCross = false,
                isDotOnly = false,
                dominantDirection = Direction.NONE,
                normalizedStrokes = emptyList()
            )
        }

        val normalized = normalizeToUnitBox(strokes)
        val allPoints = normalized.flatMap { it.points }
        if (allPoints.isEmpty()) {
            return StrokeFeatures(
                strokeCount = strokes.size,
                aspectRatio = 1f,
                isClosedLoop = false,
                hasCross = false,
                isDotOnly = false,
                dominantDirection = Direction.NONE,
                normalizedStrokes = normalized
            )
        }

        // Calculate original bounding box aspect ratio
        var origMinX = Float.MAX_VALUE
        var origMaxX = Float.MIN_VALUE
        var origMinY = Float.MAX_VALUE
        var origMaxY = Float.MIN_VALUE
        for (stroke in strokes) {
            for (p in stroke.points) {
                if (p.x < origMinX) origMinX = p.x
                if (p.x > origMaxX) origMaxX = p.x
                if (p.y < origMinY) origMinY = p.y
                if (p.y > origMaxY) origMaxY = p.y
            }
        }
        val origW = max(1f, origMaxX - origMinX)
        val origH = max(1f, origMaxY - origMinY)
        val aspectRatio = origW / origH

        // Check if single small dot
        val totalLength = strokes.sumOf { it.totalLength.toDouble() }.toFloat()
        val isDotOnly = strokes.size == 1 && (totalLength < 40f || (origW < 25f && origH < 25f))

        // Check closed loop in primary stroke
        var isClosedLoop = false
        val primary = normalized.firstOrNull()
        if (primary != null && primary.points.size >= 8) {
            val start = primary.points.first()
            val end = primary.points.last()
            val endDist = start.distanceTo(end)
            if (endDist < 0.28f && primary.totalLength > 1.2f) {
                isClosedLoop = true
            }
        }

        // Check for cross (two intersecting strokes)
        var hasCross = false
        if (normalized.size >= 2) {
            val s1 = normalized[0]
            val s2 = normalized[1]
            if (intersects(s1, s2)) {
                hasCross = true
            }
        }

        // Dominant direction of first stroke
        val dominantDirection = if (primary != null && primary.points.size >= 2) {
            val start = primary.points.first()
            val end = primary.points.last()
            val dx = end.x - start.x
            val dy = end.y - start.y
            when {
                kotlin.math.abs(dy) > kotlin.math.abs(dx) * 1.5f -> if (dy > 0) Direction.DOWN else Direction.UP
                kotlin.math.abs(dx) > kotlin.math.abs(dy) * 1.5f -> if (dx > 0) Direction.RIGHT else Direction.LEFT
                dx > 0 && dy > 0 -> Direction.DOWN_RIGHT
                dx < 0 && dy > 0 -> Direction.DOWN_LEFT
                dx > 0 && dy < 0 -> Direction.UP_RIGHT
                else -> Direction.UP_LEFT
            }
        } else {
            Direction.NONE
        }

        return StrokeFeatures(
            strokeCount = strokes.size,
            aspectRatio = aspectRatio,
            isClosedLoop = isClosedLoop,
            hasCross = hasCross,
            isDotOnly = isDotOnly,
            dominantDirection = dominantDirection,
            normalizedStrokes = normalized
        )
    }

    private fun intersects(s1: Stroke, s2: Stroke): Boolean {
        for (i in 0 until s1.points.size - 1 step 2) {
            val p1 = s1.points[i]
            val p2 = s1.points[i + 1]
            for (j in 0 until s2.points.size - 1 step 2) {
                val p3 = s2.points[j]
                val p4 = s2.points[j + 1]
                if (lineSegmentsIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y)) {
                    return true
                }
            }
        }
        return false
    }

    private fun lineSegmentsIntersect(
        p0x: Float, p0y: Float, p1x: Float, p1y: Float,
        p2x: Float, p2y: Float, p3x: Float, p3y: Float
    ): Boolean {
        val s1x = p1x - p0x
        val s1y = p1y - p0y
        val s2x = p3x - p2x
        val s2y = p3y - p2y

        val s = (-s1y * (p0x - p2x) + s1x * (p0y - p2y)) / (-s2x * s1y + s1x * s2y + 0.00001f)
        val t = (s2x * (p0y - p2y) - s2y * (p0x - p2x)) / (-s2x * s1y + s1x * s2y + 0.00001f)

        return s in 0f..1f && t in 0f..1f
    }
}

enum class Direction {
    NONE, UP, DOWN, LEFT, RIGHT, DOWN_RIGHT, DOWN_LEFT, UP_RIGHT, UP_LEFT
}

data class StrokeFeatures(
    val strokeCount: Int,
    val aspectRatio: Float,
    val isClosedLoop: Boolean,
    val hasCross: Boolean,
    val isDotOnly: Boolean,
    val dominantDirection: Direction,
    val normalizedStrokes: List<Stroke>
)
