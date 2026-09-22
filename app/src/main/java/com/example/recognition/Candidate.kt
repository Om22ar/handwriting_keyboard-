package com.example.recognition

/**
 * Represents a single character recognition candidate with a confidence score.
 *
 * @property text The candidate string (typically a single character, punctuation, or symbol).
 * @property confidence A value between 0.0 and 1.0 indicating recognizer certainty.
 * @property isExact Whether this candidate was an exact or priority match.
 */
data class Candidate(
    val text: String,
    val confidence: Float,
    val isExact: Boolean = false
) {
    init {
        // Enforce the core invariant: candidate text must NEVER contain trailing or leading spaces
        require(!text.contains(" ")) {
            "Candidate text must never contain spaces! Automatic spaces are strictly forbidden."
        }
    }
}
