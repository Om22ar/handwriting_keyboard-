package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user preferences for the handwriting IME.
 * Persists settings via SharedPreferences and exposes them as reactive StateFlows.
 */
class SettingsRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<KeyboardSettings> = _settings.asStateFlow()

    private fun loadSettings(): KeyboardSettings {
        return KeyboardSettings(
            recognitionDelayMs = prefs.getLong(KEY_RECOGNITION_DELAY, 600L),
            strokeWidth = prefs.getFloat(KEY_STROKE_WIDTH, 7.0f),
            autoRecognize = prefs.getBoolean(KEY_AUTO_RECOGNIZE, true),
            // The prompt strictly requires: "Automatic Space After Character: This setting should default to: OFF.
            // The implementation should enforce the requested behavior even when the setting is OFF."
            // Thus autoSpace is strictly false by default!
            autoSpaceAfterCharacter = prefs.getBoolean(KEY_AUTO_SPACE, false),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            showGuides = prefs.getBoolean(KEY_SHOW_GUIDES, true),
            language = prefs.getString(KEY_LANGUAGE, "en") ?: "en",
            theme = prefs.getString(KEY_THEME, "midnight") ?: "midnight",
            debugMode = prefs.getBoolean(KEY_DEBUG_MODE, false),
            candidateCount = prefs.getInt(KEY_CANDIDATE_COUNT, 5),
            keyboardHeightDp = prefs.getInt(KEY_KEYBOARD_HEIGHT, 320)
        )
    }

    fun updateRecognitionDelay(delayMs: Long) {
        prefs.edit().putLong(KEY_RECOGNITION_DELAY, delayMs).apply()
        _settings.value = _settings.value.copy(recognitionDelayMs = delayMs)
    }

    fun updateStrokeWidth(width: Float) {
        prefs.edit().putFloat(KEY_STROKE_WIDTH, width).apply()
        _settings.value = _settings.value.copy(strokeWidth = width)
    }

    fun updateAutoRecognize(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RECOGNIZE, enabled).apply()
        _settings.value = _settings.value.copy(autoRecognize = enabled)
    }

    fun updateAutoSpace(enabled: Boolean) {
        // Enforce user choice if stored, but defaulting to false
        prefs.edit().putBoolean(KEY_AUTO_SPACE, enabled).apply()
        _settings.value = _settings.value.copy(autoSpaceAfterCharacter = enabled)
    }

    fun updateHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedback = enabled)
    }

    fun updateShowGuides(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_GUIDES, enabled).apply()
        _settings.value = _settings.value.copy(showGuides = enabled)
    }

    fun updateLanguage(langCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
        _settings.value = _settings.value.copy(language = langCode)
    }

    fun updateTheme(themeName: String) {
        prefs.edit().putString(KEY_THEME, themeName).apply()
        _settings.value = _settings.value.copy(theme = themeName)
    }

    fun updateDebugMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
        _settings.value = _settings.value.copy(debugMode = enabled)
    }

    fun updateCandidateCount(count: Int) {
        prefs.edit().putInt(KEY_CANDIDATE_COUNT, count).apply()
        _settings.value = _settings.value.copy(candidateCount = count)
    }

    fun updateKeyboardHeight(heightDp: Int) {
        prefs.edit().putInt(KEY_KEYBOARD_HEIGHT, heightDp).apply()
        _settings.value = _settings.value.copy(keyboardHeightDp = heightDp)
    }

    companion object {
        private const val PREFS_NAME = "handwriting_ime_prefs"
        private const val KEY_RECOGNITION_DELAY = "recognition_delay_ms"
        private const val KEY_STROKE_WIDTH = "stroke_width"
        private const val KEY_AUTO_RECOGNIZE = "auto_recognize"
        private const val KEY_AUTO_SPACE = "auto_space_after_char"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SHOW_GUIDES = "show_guides"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
        private const val KEY_DEBUG_MODE = "debug_mode"
        private const val KEY_CANDIDATE_COUNT = "candidate_count"
        private const val KEY_KEYBOARD_HEIGHT = "keyboard_height_dp"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

data class KeyboardSettings(
    val recognitionDelayMs: Long = 600L,
    val strokeWidth: Float = 7.0f,
    val autoRecognize: Boolean = true,
    val autoSpaceAfterCharacter: Boolean = false,
    val hapticFeedback: Boolean = true,
    val showGuides: Boolean = true,
    val language: String = "en",
    val theme: String = "midnight",
    val debugMode: Boolean = false,
    val candidateCount: Int = 5,
    val keyboardHeightDp: Int = 320
)
