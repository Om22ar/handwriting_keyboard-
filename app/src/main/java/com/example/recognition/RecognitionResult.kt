package com.example.recognition

/**
 * Result returned by a [com.example.handwriting.CharacterRecognizer].
 * Contains the ranked list of candidates and recognition performance telemetry.
 */
data class RecognitionResult(
    val candidates: List<Candidate>,
    val latencyMs: Long = 0L,
    val debugInfo: String = ""
) {
    val primary: Candidate? get() = candidates.firstOrNull()

    val hasCandidates: Boolean get() = candidates.isNotEmpty()

    companion object {
        val EMPTY = RecognitionResult(emptyList())
    }
}
