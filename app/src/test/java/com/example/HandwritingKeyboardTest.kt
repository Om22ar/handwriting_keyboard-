package com.example

import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import com.example.handwriting.Point
import com.example.handwriting.Stroke
import com.example.handwriting.StrokeManager
import com.example.ime.KeyboardInputConnection
import com.example.recognition.Candidate
import com.example.recognition.Language
import com.example.recognition.LocalCharacterRecognizer
import com.example.recognition.RecognitionMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HandwritingKeyboardTest {

    private lateinit var fakeInputConnection: TestInputConnection
    private lateinit var keyboardInputConnection: KeyboardInputConnection

    @Before
    fun setUp() {
        fakeInputConnection = TestInputConnection()
        keyboardInputConnection = KeyboardInputConnection(
            inputConnectionProvider = { fakeInputConnection },
            editorInfoProvider = { null }
        )
    }

    @Test
    fun `test sequential characters commit WITHOUT spaces - ABCD`() {
        keyboardInputConnection.commitCharacter("A")
        keyboardInputConnection.commitCharacter("B")
        keyboardInputConnection.commitCharacter("C")
        keyboardInputConnection.commitCharacter("D")

        assertEquals("ABCD", fakeInputConnection.currentText)
        // Verify individual commits
        assertEquals(listOf("A", "B", "C", "D"), fakeInputConnection.committedTexts)
    }

    @Test
    fun `test space button ONLY creates space - AB space C`() {
        keyboardInputConnection.commitCharacter("A")
        keyboardInputConnection.commitCharacter("B")
        keyboardInputConnection.commitSpace()
        keyboardInputConnection.commitCharacter("C")

        assertEquals("AB C", fakeInputConnection.currentText)
        assertEquals(listOf("A", "B", " ", "C"), fakeInputConnection.committedTexts)
    }

    @Test
    fun `test Hello World sequence with explicit space`() {
        val word1 = listOf("H", "e", "l", "l", "o")
        word1.forEach { keyboardInputConnection.commitCharacter(it) }

        // Explicit space
        keyboardInputConnection.commitSpace()

        val word2 = listOf("W", "o", "r", "l", "d")
        word2.forEach { keyboardInputConnection.commitCharacter(it) }

        assertEquals("Hello World", fakeInputConnection.currentText)
    }

    @Test
    fun `test recognition NEVER commits text with trailing space`() {
        val testChars = listOf("A", "B", "Z", "1", "9", "a", "k", "!", "?")
        for (char in testChars) {
            fakeInputConnection.clear()
            keyboardInputConnection.commitCharacter(char)

            // Must NOT have trailing space
            assertFalse(
                "Character '$char' must NOT be committed with trailing space!",
                fakeInputConnection.currentText.endsWith(" ")
            )
            assertEquals(char, fakeInputConnection.currentText)
        }
    }

    @Test
    fun `test punctuation does not add spaces`() {
        keyboardInputConnection.commitCharacter("H")
        keyboardInputConnection.commitCharacter("i")
        keyboardInputConnection.commitPunctuation("!")

        assertEquals("Hi!", fakeInputConnection.currentText)
    }

    @Test
    fun `test backspace deletes single character before cursor`() {
        keyboardInputConnection.commitCharacter("A")
        keyboardInputConnection.commitCharacter("B")
        keyboardInputConnection.commitCharacter("C")
        assertEquals("ABC", fakeInputConnection.currentText)

        keyboardInputConnection.deleteBackward()
        assertEquals("AB", fakeInputConnection.currentText)

        keyboardInputConnection.deleteBackward()
        assertEquals("A", fakeInputConnection.currentText)
    }

    @Test
    fun `test backspace deletes unicode surrogate pairs correctly`() {
        // E.g. Emoji 😃 is a surrogate pair (2 chars in Java string)
        val emoji = "\uD83D\uDE03"
        keyboardInputConnection.commitCharacter("A")
        keyboardInputConnection.commitCharacter(emoji)
        assertEquals("A$emoji", fakeInputConnection.currentText)

        keyboardInputConnection.deleteBackward()
        // Should have deleted the entire surrogate pair, leaving only "A"
        assertEquals("A", fakeInputConnection.currentText)
    }

    @Test
    fun `test multiple consecutive spaces preserved`() {
        keyboardInputConnection.commitCharacter("A")
        keyboardInputConnection.commitSpace()
        keyboardInputConnection.commitSpace()
        keyboardInputConnection.commitCharacter("B")

        assertEquals("A  B", fakeInputConnection.currentText)
    }

    @Test
    fun `test stroke manager multi-stroke undo redo and clear`() {
        val manager = StrokeManager()
        assertFalse(manager.hasStrokes)

        val stroke1 = Stroke(listOf(Point(10f, 10f), Point(20f, 20f)))
        val stroke2 = Stroke(listOf(Point(30f, 10f), Point(40f, 20f)))

        manager.addStroke(stroke1)
        manager.addStroke(stroke2)
        assertEquals(2, manager.strokeCount)
        assertTrue(manager.canUndo)

        // Undo
        assertTrue(manager.undo())
        assertEquals(1, manager.strokeCount)
        assertTrue(manager.canRedo)

        // Redo
        assertTrue(manager.redo())
        assertEquals(2, manager.strokeCount)

        // Clear
        manager.clear()
        assertEquals(0, manager.strokeCount)
        assertFalse(manager.hasStrokes)
    }

    @Test
    fun `test candidate creation rejects spaces`() {
        // Enforce candidate invariant: candidates must NEVER contain spaces
        var caughtException = false
        try {
            Candidate("A ", 0.9f)
        } catch (e: IllegalArgumentException) {
            caughtException = true
        }
        assertTrue("Candidate should throw exception if space is included", caughtException)
    }

    @Test
    fun `test local character recognizer returns valid candidates`() = runBlocking {
        val recognizer = LocalCharacterRecognizer()
        // Create vertical stroke (like '1' or 'I')
        val stroke = Stroke(
            listOf(
                Point(0.5f, 0.1f),
                Point(0.5f, 0.5f),
                Point(0.5f, 0.9f)
            )
        )

        val result = recognizer.recognize(
            strokes = listOf(stroke),
            language = Language.ENGLISH,
            mode = RecognitionMode.AUTO,
            maxCandidates = 5
        )

        assertTrue("Recognizer should produce candidates", result.hasCandidates)
        for (candidate in result.candidates) {
            assertFalse(
                "Candidate text must NOT contain spaces",
                candidate.text.contains(" ")
            )
        }
    }

    /**
     * In-memory test implementation of InputConnection to record commits and cursor mutations.
     */
    private class TestInputConnection : InputConnection {
        val committedTexts = mutableListOf<String>()
        val buffer = StringBuilder()
        var cursorPosition = 0

        val currentText: String get() = buffer.toString()

        fun clear() {
            buffer.setLength(0)
            cursorPosition = 0
            committedTexts.clear()
        }

        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
            if (text == null) return false
            val str = text.toString()
            committedTexts.add(str)
            buffer.insert(cursorPosition, str)
            cursorPosition += str.length
            return true
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            val start = (cursorPosition - beforeLength).coerceAtLeast(0)
            val end = (cursorPosition + afterLength).coerceAtMost(buffer.length)
            if (start < cursorPosition) {
                buffer.delete(start, cursorPosition)
                cursorPosition = start
            }
            if (cursorPosition < end) {
                buffer.delete(cursorPosition, end)
            }
            return true
        }

        override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
            val start = (cursorPosition - n).coerceAtLeast(0)
            return buffer.substring(start, cursorPosition)
        }

        override fun getTextAfterCursor(n: Int, flags: Int): CharSequence {
            val end = (cursorPosition + n).coerceAtMost(buffer.length)
            return buffer.substring(cursorPosition, end)
        }

        override fun getSelectedText(flags: Int): CharSequence? = null

        override fun getCursorCapsMode(reqModes: Int): Int = 0
        override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? = null
        override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean = false
        override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean = false
        override fun setComposingRegion(start: Int, end: Int): Boolean = false
        override fun finishComposingText(): Boolean = false
        override fun commitCompletion(text: CompletionInfo?): Boolean = false
        override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean = false
        override fun setSelection(start: Int, end: Int): Boolean {
            cursorPosition = start.coerceIn(0, buffer.length)
            return true
        }
        override fun performEditorAction(editorAction: Int): Boolean = true
        override fun performContextMenuAction(id: Int): Boolean = false
        override fun beginBatchEdit(): Boolean = true
        override fun endBatchEdit(): Boolean = true
        override fun sendKeyEvent(event: KeyEvent?): Boolean {
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DEL -> deleteSurroundingText(1, 0)
                    KeyEvent.KEYCODE_ENTER -> commitText("\n", 1)
                    KeyEvent.KEYCODE_DPAD_LEFT -> cursorPosition = (cursorPosition - 1).coerceAtLeast(0)
                    KeyEvent.KEYCODE_DPAD_RIGHT -> cursorPosition = (cursorPosition + 1).coerceAtMost(buffer.length)
                }
            }
            return true
        }
        override fun clearMetaKeyStates(states: Int): Boolean = false
        override fun performPrivateCommand(action: String?, data: Bundle?): Boolean = false
        override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean = false
        override fun getHandler(): Handler? = null
        override fun closeConnection() {}
        override fun reportFullscreenMode(enabled: Boolean): Boolean = false
        override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean = false
    }
}
