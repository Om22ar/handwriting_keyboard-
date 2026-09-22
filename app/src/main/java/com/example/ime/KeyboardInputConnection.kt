package com.example.ime

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Manages all text modifications via Android's [InputConnection] API.
 *
 * CRITICAL ARCHITECTURAL CONTRACT:
 * Character recognition commits ONLY the raw character text via [commitCharacter].
 * An automatic space, newline, or hidden separator is NEVER appended.
 * Spaces are ONLY inserted when the user explicitly triggers [commitSpace].
 */
class KeyboardInputConnection(
    private val inputConnectionProvider: () -> InputConnection?,
    private val editorInfoProvider: () -> EditorInfo?
) {

    /**
     * Commits a recognized character to the current cursor position.
     *
     * ABSOLUTE RULE: Must commit EXACTLY the character string with NO trailing spaces.
     */
    fun commitCharacter(character: String) {
        if (character.isEmpty()) return
        val ic = inputConnectionProvider() ?: return

        // CRITICAL: commitText(character, 1) without adding ANY trailing space or separator!
        ic.commitText(character, 1)
    }

    /**
     * Commits an explicit space (" ") to the text field.
     * ONLY triggered when the user explicitly presses the Space key.
     */
    fun commitSpace() {
        val ic = inputConnectionProvider() ?: return
        ic.commitText(" ", 1)
    }

    /**
     * Commits punctuation or symbol without any automatic spaces.
     */
    fun commitPunctuation(symbol: String) {
        if (symbol.isEmpty()) return
        val ic = inputConnectionProvider() ?: return
        ic.commitText(symbol, 1)
    }

    /**
     * Deletes the character immediately before the cursor.
     * Handles text selections, normal characters, and Unicode surrogate pairs.
     */
    fun deleteBackward() {
        val ic = inputConnectionProvider() ?: return

        // 1. If text is currently selected, delete the selected text
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }

        // 2. Inspect characters before cursor to detect Unicode surrogate pairs (e.g. emojis)
        val textBefore = ic.getTextBeforeCursor(2, 0)
        if (!textBefore.isNullOrEmpty()) {
            val len = textBefore.length
            val lastChar = textBefore[len - 1]
            if (len >= 2 && Character.isSurrogate(lastChar)) {
                val prevChar = textBefore[len - 2]
                if (Character.isSurrogatePair(prevChar, lastChar)) {
                    ic.deleteSurroundingText(2, 0)
                    return
                }
            }
            ic.deleteSurroundingText(1, 0)
        } else {
            // Fallback for hardware key event if textBefore is unavailable
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        }
    }

    /**
     * Performs the default action for the current editor (Enter, Search, Send, Go, Next, Done).
     */
    fun performAction(): Boolean {
        val ic = inputConnectionProvider() ?: return false
        val info = editorInfoProvider()

        if (info != null) {
            val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
            if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                return ic.performEditorAction(action)
            }
        }

        // Default: send standard newline Enter key
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        return true
    }

    /**
     * Moves the cursor one step to the left.
     */
    fun moveCursorLeft() {
        val ic = inputConnectionProvider() ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
    }

    /**
     * Moves the cursor one step to the right.
     */
    fun moveCursorRight() {
        val ic = inputConnectionProvider() ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
    }
}
