package com.example.handwriting

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt

/**
 * High-performance hardware-accelerated touch canvas for drawing handwriting characters.
 * Supports multi-stroke characters, Bézier smoothing, baseline guides, undo/redo,
 * and configurable recognition inactivity timer.
 */
class HandwritingCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val strokeManager = StrokeManager()

    private val handler = Handler(Looper.getMainLooper())
    private var recognitionRunnable: Runnable? = null

    // Drawing paints
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = 0xFF38BDF8.toInt() // Default cyan neon
        strokeWidth = 14f
    }

    private val strokeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = 0x5538BDF8.toInt()
        strokeWidth = 24f
    }

    private val guideLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0x2AFFFFFF.toInt()
        strokeWidth = 2f
    }

    private val dashedGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0x20FFFFFF.toInt()
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(12f, 12f), 0f)
    }

    private val hintTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x3DFFFFFF.toInt()
        textSize = 38f
        textAlign = Paint.Align.CENTER
    }

    // Active stroke tracking
    private var activePath = Path()
    private val activePoints = mutableListOf<Point>()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDrawing = false

    // Paths cache for completed strokes
    private val cachedPaths = mutableListOf<Path>()

    // Configurable settings
    var recognitionDelayMs: Long = 600L
    var autoRecognizeEnabled: Boolean = true
    var showGuides: Boolean = true
    var hintText: String = "Draw character here"
    var onStrokesChanged: ((strokeCount: Int) -> Unit)? = null
    var onRecognitionRequested: ((strokes: List<Stroke>) -> Unit)? = null

    init {
        // Enable hardware acceleration
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setStrokeColor(@ColorInt color: Int, @ColorInt glowColor: Int) {
        strokePaint.color = color
        strokeGlowPaint.color = glowColor
        invalidate()
    }

    fun setStrokeWidthDip(dp: Float) {
        val px = dp * resources.displayMetrics.density
        strokePaint.strokeWidth = px
        strokeGlowPaint.strokeWidth = px * 1.6f
        invalidate()
    }

    fun setGuideColor(@ColorInt color: Int) {
        guideLinePaint.color = color
        dashedGuidePaint.color = (color and 0x00FFFFFF) or 0x22000000
        hintTextPaint.color = (color and 0x00FFFFFF) or 0x44000000
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val pressure = event.pressure

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Cancel pending recognition timer when touching down for next stroke
                cancelRecognitionTimer()
                isDrawing = true
                activePath.reset()
                activePath.moveTo(x, y)
                activePoints.clear()
                activePoints.add(Point(x, y, System.currentTimeMillis(), pressure))
                lastTouchX = x
                lastTouchY = y
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isDrawing) return false
                val historySize = event.historySize
                for (i in 0 until historySize) {
                    val hx = event.getHistoricalX(i)
                    val hy = event.getHistoricalY(i)
                    val hp = event.getHistoricalPressure(i)
                    addInterpolatedPoint(hx, hy, hp)
                }
                addInterpolatedPoint(x, y, pressure)
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDrawing) {
                    isDrawing = false
                    activePoints.add(Point(x, y, System.currentTimeMillis(), pressure))
                    val completedStroke = Stroke(activePoints.toList())
                    strokeManager.addStroke(completedStroke)

                    // Cache path
                    val finalizedPath = Path(activePath)
                    cachedPaths.add(finalizedPath)
                    activePath.reset()
                    activePoints.clear()

                    onStrokesChanged?.invoke(strokeManager.strokeCount)
                    invalidate()

                    // Start inactivity timer for multi-stroke recognition
                    if (autoRecognizeEnabled && strokeManager.hasStrokes) {
                        scheduleRecognition()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun addInterpolatedPoint(x: Float, y: Float, pressure: Float) {
        activePoints.add(Point(x, y, System.currentTimeMillis(), pressure))
        val midX = (lastTouchX + x) / 2f
        val midY = (lastTouchY + y) / 2f
        activePath.quadTo(lastTouchX, lastTouchY, midX, midY)
        lastTouchX = x
        lastTouchY = y
    }

    private fun cancelRecognitionTimer() {
        recognitionRunnable?.let { handler.removeCallbacks(it) }
        recognitionRunnable = null
    }

    fun scheduleRecognition() {
        cancelRecognitionTimer()
        recognitionRunnable = Runnable {
            if (strokeManager.hasStrokes) {
                val currentStrokes = strokeManager.strokes
                onRecognitionRequested?.invoke(currentStrokes)
            }
        }.also { handler.postDelayed(it, recognitionDelayMs) }
    }

    /**
     * Immediately triggers recognition without waiting for the inactivity timer.
     */
    fun triggerImmediateRecognition() {
        cancelRecognitionTimer()
        if (strokeManager.hasStrokes) {
            val currentStrokes = strokeManager.strokes
            onRecognitionRequested?.invoke(currentStrokes)
        }
    }

    fun undo(): Boolean {
        cancelRecognitionTimer()
        val undone = strokeManager.undo()
        if (undone) {
            rebuildCachedPaths()
            onStrokesChanged?.invoke(strokeManager.strokeCount)
            invalidate()
            if (strokeManager.hasStrokes && autoRecognizeEnabled) {
                scheduleRecognition()
            }
        }
        return undone
    }

    fun redo(): Boolean {
        cancelRecognitionTimer()
        val redone = strokeManager.redo()
        if (redone) {
            rebuildCachedPaths()
            onStrokesChanged?.invoke(strokeManager.strokeCount)
            invalidate()
            if (strokeManager.hasStrokes && autoRecognizeEnabled) {
                scheduleRecognition()
            }
        }
        return redone
    }

    fun clearCanvas() {
        cancelRecognitionTimer()
        strokeManager.clear()
        cachedPaths.clear()
        activePath.reset()
        activePoints.clear()
        isDrawing = false
        onStrokesChanged?.invoke(0)
        invalidate()
    }

    private fun rebuildCachedPaths() {
        cachedPaths.clear()
        for (stroke in strokeManager.strokes) {
            if (stroke.points.size < 2) continue
            val path = Path()
            path.moveTo(stroke.points[0].x, stroke.points[0].y)
            for (i in 1 until stroke.points.size) {
                val prev = stroke.points[i - 1]
                val curr = stroke.points[i]
                val midX = (prev.x + curr.x) / 2f
                val midY = (prev.y + curr.y) / 2f
                path.quadTo(prev.x, prev.y, midX, midY)
            }
            cachedPaths.add(path)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // 1. Draw handwriting guidelines (ascender, midline, baseline, descender)
        if (showGuides) {
            val ascenderY = h * 0.22f
            val midlineY = h * 0.48f
            val baselineY = h * 0.76f

            // Ascender guide
            canvas.drawLine(w * 0.05f, ascenderY, w * 0.95f, ascenderY, guideLinePaint)
            // Midline (dashed)
            canvas.drawLine(w * 0.05f, midlineY, w * 0.95f, midlineY, dashedGuidePaint)
            // Baseline (solid)
            canvas.drawLine(w * 0.05f, baselineY, w * 0.95f, baselineY, guideLinePaint)
        }

        // 2. Draw watermark hint if empty
        if (!strokeManager.hasStrokes && !isDrawing) {
            canvas.drawText(hintText, w / 2f, h * 0.54f, hintTextPaint)
        }

        // 3. Draw completed strokes with glow + solid ink
        for (path in cachedPaths) {
            canvas.drawPath(path, strokeGlowPaint)
            canvas.drawPath(path, strokePaint)
        }

        // 4. Draw active stroke currently under finger
        if (isDrawing) {
            canvas.drawPath(activePath, strokeGlowPaint)
            canvas.drawPath(activePath, strokePaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelRecognitionTimer()
    }
}
