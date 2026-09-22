package com.example.ime

import android.view.inputmethod.EditorInfo
import androidx.compose.ui.graphics.Color

object ImeConfig {
    const val DEFAULT_RECOGNITION_DELAY_MS = 600L
    const val MIN_RECOGNITION_DELAY_MS = 300L
    const val MAX_RECOGNITION_DELAY_MS = 1500L

    fun getActionLabel(editorInfo: EditorInfo?): String {
        if (editorInfo == null) return "Enter"
        val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
        return when (action) {
            EditorInfo.IME_ACTION_GO -> "Go"
            EditorInfo.IME_ACTION_SEARCH -> "Search"
            EditorInfo.IME_ACTION_SEND -> "Send"
            EditorInfo.IME_ACTION_NEXT -> "Next"
            EditorInfo.IME_ACTION_DONE -> "Done"
            else -> "Enter"
        }
    }
}

data class KeyboardPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val strokeColor: Color,
    val strokeGlow: Color,
    val primary: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val guideLines: Color
) {
    companion object {
        val Midnight = KeyboardPalette(
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            surfaceVariant = Color(0xFF334155),
            strokeColor = Color(0xFF38BDF8), // Cyan neon
            strokeGlow = Color(0x6638BDF8),
            primary = Color(0xFF0EA5E9),
            onPrimary = Color.White,
            onSurface = Color(0xFFF1F5F9),
            guideLines = Color(0x3394A3B8)
        )

        val Dark = KeyboardPalette(
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2D2D2D),
            strokeColor = Color(0xFFBB86FC), // Purple accent
            strokeGlow = Color(0x66BB86FC),
            primary = Color(0xFFBB86FC),
            onPrimary = Color.Black,
            onSurface = Color(0xFFE0E0E0),
            guideLines = Color(0x33888888)
        )

        val Light = KeyboardPalette(
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE2E8F0),
            strokeColor = Color(0xFF0284C7), // Deep cyan/blue
            strokeGlow = Color(0x440284C7),
            primary = Color(0xFF0284C7),
            onPrimary = Color.White,
            onSurface = Color(0xFF0F172A),
            guideLines = Color(0x3364748B)
        )

        fun fromName(theme: String): KeyboardPalette = when (theme.lowercase()) {
            "light" -> Light
            "dark" -> Dark
            else -> Midnight
        }
    }
}
