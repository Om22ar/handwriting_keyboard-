package com.example.handwriting

import com.example.recognition.Language
import com.example.recognition.RecognitionMode
import com.example.recognition.RecognitionResult

/**
 * Modular interface for handwriting character recognition engines.
 * Allows replacing or extending the recognition engine (e.g. ML models, TFLite,
 * platform APIs) without rewriting the IME or touch pipeline.
 */
interface CharacterRecognizer {
    val name: String

    suspend fun recognize(
        strokes: List<Stroke>,
        language: Language = Language.ENGLISH,
        mode: RecognitionMode = RecognitionMode.AUTO,
        maxCandidates: Int = 5
    ): RecognitionResult
}
