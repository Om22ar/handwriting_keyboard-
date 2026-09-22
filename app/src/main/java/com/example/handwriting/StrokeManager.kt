package com.example.handwriting

import java.util.Stack

/**
 * Manages the collection of strokes for the active character being drawn.
 * Supports multi-stroke characters (e.g. 'E', 'A', 'H', 't', 'k', 'X', 'i', Arabic characters),
 * undo, redo, and canvas clearing.
 */
class StrokeManager {
    private val _strokes = mutableListOf<Stroke>()
    private val undoStack = Stack<Stroke>()
    private val redoStack = Stack<Stroke>()

    val strokes: List<Stroke> get() = _strokes.toList()

    val strokeCount: Int get() = _strokes.size

    val hasStrokes: Boolean get() = _strokes.isNotEmpty()

    val canUndo: Boolean get() = _strokes.isNotEmpty()

    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun addStroke(stroke: Stroke) {
        if (stroke.points.isNotEmpty()) {
            _strokes.add(stroke)
            redoStack.clear()
        }
    }

    fun undo(): Boolean {
        if (_strokes.isNotEmpty()) {
            val removed = _strokes.removeAt(_strokes.size - 1)
            redoStack.push(removed)
            return true
        }
        return false
    }

    fun redo(): Boolean {
        if (redoStack.isNotEmpty()) {
            val restored = redoStack.pop()
            _strokes.add(restored)
            return true
        }
        return false
    }

    fun clear() {
        _strokes.clear()
        undoStack.clear()
        redoStack.clear()
    }

    fun getAllPoints(): List<Point> {
        return _strokes.flatMap { it.points }
    }
}
