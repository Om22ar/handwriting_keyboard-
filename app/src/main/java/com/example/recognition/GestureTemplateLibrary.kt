package com.example.recognition

import com.example.handwriting.Point
import com.example.handwriting.Stroke
import com.example.handwriting.StrokeNormalizer

/**
 * Standard gesture library providing reference templates for Latin uppercase A-Z,
 * lowercase a-z, digits 0-9, common punctuation, and Arabic script.
 */
object GestureTemplateLibrary {

    private val templates = mutableListOf<GestureTemplate>()

    fun getAllTemplates(): List<GestureTemplate> {
        if (templates.isEmpty()) {
            buildTemplates()
        }
        return templates
    }

    private fun buildTemplates() {
        // ==================== DIGITS ====================
        addT("0", Category.NUMBER, listOf(
            Point(0.5f, 0.05f), Point(0.2f, 0.2f), Point(0.1f, 0.5f),
            Point(0.2f, 0.8f), Point(0.5f, 0.95f), Point(0.8f, 0.8f),
            Point(0.9f, 0.5f), Point(0.8f, 0.2f), Point(0.5f, 0.05f)
        ), strokeCount = 1, closed = true)

        addT("1", Category.NUMBER, listOf(
            Point(0.35f, 0.2f), Point(0.5f, 0.05f), Point(0.5f, 0.95f)
        ), strokeCount = 1, tall = true)

        addT("2", Category.NUMBER, listOf(
            Point(0.2f, 0.25f), Point(0.5f, 0.05f), Point(0.8f, 0.25f),
            Point(0.8f, 0.45f), Point(0.2f, 0.95f), Point(0.85f, 0.95f)
        ), strokeCount = 1)

        addT("3", Category.NUMBER, listOf(
            Point(0.2f, 0.15f), Point(0.8f, 0.15f), Point(0.5f, 0.45f),
            Point(0.8f, 0.65f), Point(0.5f, 0.95f), Point(0.2f, 0.85f)
        ), strokeCount = 1)

        addT("4", Category.NUMBER, listOf(
            Point(0.7f, 0.95f), Point(0.7f, 0.05f), Point(0.15f, 0.65f), Point(0.9f, 0.65f)
        ), strokeCount = 2, hasCross = true)

        addT("5", Category.NUMBER, listOf(
            Point(0.8f, 0.1f), Point(0.25f, 0.1f), Point(0.25f, 0.45f),
            Point(0.75f, 0.45f), Point(0.8f, 0.75f), Point(0.5f, 0.95f), Point(0.2f, 0.85f)
        ), strokeCount = 1)

        addT("6", Category.NUMBER, listOf(
            Point(0.75f, 0.15f), Point(0.3f, 0.35f), Point(0.15f, 0.65f),
            Point(0.4f, 0.95f), Point(0.8f, 0.75f), Point(0.6f, 0.5f), Point(0.2f, 0.65f)
        ), strokeCount = 1, closed = true)

        addT("7", Category.NUMBER, listOf(
            Point(0.15f, 0.1f), Point(0.85f, 0.1f), Point(0.35f, 0.95f)
        ), strokeCount = 1)

        addT("8", Category.NUMBER, listOf(
            Point(0.5f, 0.5f), Point(0.25f, 0.25f), Point(0.5f, 0.05f), Point(0.75f, 0.25f),
            Point(0.5f, 0.5f), Point(0.2f, 0.75f), Point(0.5f, 0.95f), Point(0.8f, 0.75f), Point(0.5f, 0.5f)
        ), strokeCount = 1, closed = true)

        addT("9", Category.NUMBER, listOf(
            Point(0.5f, 0.5f), Point(0.25f, 0.25f), Point(0.5f, 0.05f), Point(0.75f, 0.25f),
            Point(0.5f, 0.5f), Point(0.75f, 0.5f), Point(0.65f, 0.95f), Point(0.35f, 0.95f)
        ), strokeCount = 1, closed = true)

        // ==================== UPPERCASE A-Z ====================
        addT("A", Category.LETTER, listOf(
            Point(0.15f, 0.95f), Point(0.5f, 0.05f), Point(0.85f, 0.95f),
            Point(0.3f, 0.55f), Point(0.7f, 0.55f)
        ), strokeCount = 2, hasCross = true)

        addT("B", Category.LETTER, listOf(
            Point(0.2f, 0.95f), Point(0.2f, 0.05f), Point(0.7f, 0.25f),
            Point(0.4f, 0.5f), Point(0.75f, 0.75f), Point(0.2f, 0.95f)
        ), strokeCount = 1, closed = true)

        addT("C", Category.LETTER, listOf(
            Point(0.85f, 0.2f), Point(0.5f, 0.05f), Point(0.15f, 0.5f),
            Point(0.5f, 0.95f), Point(0.85f, 0.8f)
        ), strokeCount = 1)

        addT("D", Category.LETTER, listOf(
            Point(0.2f, 0.95f), Point(0.2f, 0.05f), Point(0.75f, 0.2f),
            Point(0.8f, 0.5f), Point(0.75f, 0.8f), Point(0.2f, 0.95f)
        ), strokeCount = 1, closed = true)

        addT("E", Category.LETTER, listOf(
            Point(0.85f, 0.08f), Point(0.2f, 0.08f), Point(0.2f, 0.92f),
            Point(0.85f, 0.92f), Point(0.2f, 0.5f), Point(0.7f, 0.5f)
        ), strokeCount = 1)

        addT("F", Category.LETTER, listOf(
            Point(0.85f, 0.08f), Point(0.25f, 0.08f), Point(0.25f, 0.95f),
            Point(0.25f, 0.5f), Point(0.7f, 0.5f)
        ), strokeCount = 2)

        addT("G", Category.LETTER, listOf(
            Point(0.85f, 0.2f), Point(0.5f, 0.05f), Point(0.15f, 0.5f),
            Point(0.5f, 0.95f), Point(0.85f, 0.75f), Point(0.55f, 0.75f)
        ), strokeCount = 1)

        addT("H", Category.LETTER, listOf(
            Point(0.2f, 0.05f), Point(0.2f, 0.95f),
            Point(0.8f, 0.05f), Point(0.8f, 0.95f),
            Point(0.2f, 0.5f), Point(0.8f, 0.5f)
        ), strokeCount = 3, hasCross = true)

        addT("I", Category.LETTER, listOf(
            Point(0.5f, 0.05f), Point(0.5f, 0.95f)
        ), strokeCount = 1, tall = true)

        addT("J", Category.LETTER, listOf(
            Point(0.75f, 0.05f), Point(0.75f, 0.75f), Point(0.5f, 0.95f), Point(0.2f, 0.8f)
        ), strokeCount = 1)

        addT("K", Category.LETTER, listOf(
            Point(0.2f, 0.05f), Point(0.2f, 0.95f),
            Point(0.8f, 0.1f), Point(0.2f, 0.55f), Point(0.85f, 0.95f)
        ), strokeCount = 2)

        addT("L", Category.LETTER, listOf(
            Point(0.25f, 0.05f), Point(0.25f, 0.95f), Point(0.85f, 0.95f)
        ), strokeCount = 1)

        addT("M", Category.LETTER, listOf(
            Point(0.15f, 0.95f), Point(0.15f, 0.05f), Point(0.5f, 0.65f),
            Point(0.85f, 0.05f), Point(0.85f, 0.95f)
        ), strokeCount = 1, wide = true)

        addT("N", Category.LETTER, listOf(
            Point(0.2f, 0.95f), Point(0.2f, 0.05f), Point(0.8f, 0.95f), Point(0.8f, 0.05f)
        ), strokeCount = 1)

        addT("O", Category.LETTER, listOf(
            Point(0.5f, 0.05f), Point(0.15f, 0.35f), Point(0.15f, 0.65f),
            Point(0.5f, 0.95f), Point(0.85f, 0.65f), Point(0.85f, 0.35f), Point(0.5f, 0.05f)
        ), strokeCount = 1, closed = true)

        addT("P", Category.LETTER, listOf(
            Point(0.2f, 0.95f), Point(0.2f, 0.05f), Point(0.75f, 0.25f),
            Point(0.2f, 0.55f)
        ), strokeCount = 1)

        addT("Q", Category.LETTER, listOf(
            Point(0.5f, 0.05f), Point(0.15f, 0.5f), Point(0.5f, 0.95f),
            Point(0.85f, 0.5f), Point(0.5f, 0.05f), Point(0.55f, 0.65f), Point(0.9f, 0.95f)
        ), strokeCount = 2, closed = true)

        addT("R", Category.LETTER, listOf(
            Point(0.2f, 0.95f), Point(0.2f, 0.05f), Point(0.75f, 0.25f),
            Point(0.2f, 0.5f), Point(0.85f, 0.95f)
        ), strokeCount = 1)

        addT("S", Category.LETTER, listOf(
            Point(0.8f, 0.2f), Point(0.5f, 0.05f), Point(0.2f, 0.3f),
            Point(0.8f, 0.7f), Point(0.5f, 0.95f), Point(0.2f, 0.8f)
        ), strokeCount = 1)

        addT("T", Category.LETTER, listOf(
            Point(0.1f, 0.08f), Point(0.9f, 0.08f),
            Point(0.5f, 0.08f), Point(0.5f, 0.95f)
        ), strokeCount = 2, hasCross = true)

        addT("U", Category.LETTER, listOf(
            Point(0.2f, 0.05f), Point(0.2f, 0.75f), Point(0.5f, 0.95f),
            Point(0.8f, 0.75f), Point(0.8f, 0.05f)
        ), strokeCount = 1)

        addT("V", Category.LETTER, listOf(
            Point(0.15f, 0.05f), Point(0.5f, 0.95f), Point(0.85f, 0.05f)
        ), strokeCount = 1)

        addT("W", Category.LETTER, listOf(
            Point(0.1f, 0.05f), Point(0.3f, 0.95f), Point(0.5f, 0.45f),
            Point(0.7f, 0.95f), Point(0.9f, 0.05f)
        ), strokeCount = 1, wide = true)

        addT("X", Category.LETTER, listOf(
            Point(0.15f, 0.1f), Point(0.85f, 0.9f),
            Point(0.85f, 0.1f), Point(0.15f, 0.9f)
        ), strokeCount = 2, hasCross = true)

        addT("Y", Category.LETTER, listOf(
            Point(0.15f, 0.05f), Point(0.5f, 0.5f), Point(0.85f, 0.05f),
            Point(0.5f, 0.5f), Point(0.5f, 0.95f)
        ), strokeCount = 2)

        addT("Z", Category.LETTER, listOf(
            Point(0.15f, 0.1f), Point(0.85f, 0.1f), Point(0.15f, 0.9f), Point(0.85f, 0.9f)
        ), strokeCount = 1)

        // ==================== LOWERCASE a-z ====================
        addT("a", Category.LETTER, listOf(
            Point(0.75f, 0.4f), Point(0.45f, 0.35f), Point(0.25f, 0.65f),
            Point(0.55f, 0.95f), Point(0.75f, 0.75f), Point(0.75f, 0.35f), Point(0.75f, 0.95f)
        ), strokeCount = 1, closed = true)

        addT("b", Category.LETTER, listOf(
            Point(0.25f, 0.05f), Point(0.25f, 0.95f), Point(0.65f, 0.95f),
            Point(0.8f, 0.65f), Point(0.5f, 0.45f), Point(0.25f, 0.55f)
        ), strokeCount = 1)

        addT("c", Category.LETTER, listOf(
            Point(0.8f, 0.4f), Point(0.5f, 0.35f), Point(0.25f, 0.65f),
            Point(0.5f, 0.95f), Point(0.8f, 0.85f)
        ), strokeCount = 1)

        addT("d", Category.LETTER, listOf(
            Point(0.75f, 0.05f), Point(0.75f, 0.95f), Point(0.45f, 0.95f),
            Point(0.2f, 0.7f), Point(0.5f, 0.45f), Point(0.75f, 0.55f)
        ), strokeCount = 1)

        addT("e", Category.LETTER, listOf(
            Point(0.25f, 0.65f), Point(0.8f, 0.65f), Point(0.55f, 0.35f),
            Point(0.25f, 0.6f), Point(0.55f, 0.95f), Point(0.8f, 0.85f)
        ), strokeCount = 1)

        addT("h", Category.LETTER, listOf(
            Point(0.25f, 0.05f), Point(0.25f, 0.95f), Point(0.25f, 0.55f),
            Point(0.65f, 0.45f), Point(0.75f, 0.7f), Point(0.75f, 0.95f)
        ), strokeCount = 1)

        addT("i", Category.LETTER, listOf(
            Point(0.5f, 0.35f), Point(0.5f, 0.95f)
        ), strokeCount = 2, tall = true)

        addT("l", Category.LETTER, listOf(
            Point(0.5f, 0.05f), Point(0.5f, 0.95f)
        ), strokeCount = 1, tall = true)

        addT("o", Category.LETTER, listOf(
            Point(0.5f, 0.35f), Point(0.25f, 0.55f), Point(0.5f, 0.95f),
            Point(0.75f, 0.65f), Point(0.5f, 0.35f)
        ), strokeCount = 1, closed = true)

        addT("r", Category.LETTER, listOf(
            Point(0.3f, 0.95f), Point(0.3f, 0.4f), Point(0.6f, 0.35f), Point(0.8f, 0.5f)
        ), strokeCount = 1)

        addT("t", Category.LETTER, listOf(
            Point(0.5f, 0.1f), Point(0.5f, 0.95f), Point(0.2f, 0.35f), Point(0.8f, 0.35f)
        ), strokeCount = 2, hasCross = true)

        // ==================== PUNCTUATION & SYMBOLS ====================
        addT("!", Category.SYMBOL, listOf(
            Point(0.5f, 0.05f), Point(0.5f, 0.65f), Point(0.5f, 0.9f), Point(0.5f, 0.95f)
        ), strokeCount = 2, tall = true)

        addT("?", Category.SYMBOL, listOf(
            Point(0.25f, 0.25f), Point(0.5f, 0.05f), Point(0.75f, 0.25f),
            Point(0.5f, 0.55f), Point(0.5f, 0.65f), Point(0.5f, 0.95f)
        ), strokeCount = 2)

        addT("-", Category.SYMBOL, listOf(
            Point(0.15f, 0.5f), Point(0.85f, 0.5f)
        ), strokeCount = 1, wide = true)

        addT("+", Category.SYMBOL, listOf(
            Point(0.15f, 0.5f), Point(0.85f, 0.5f), Point(0.5f, 0.15f), Point(0.5f, 0.85f)
        ), strokeCount = 2, hasCross = true)

        addT("=", Category.SYMBOL, listOf(
            Point(0.2f, 0.35f), Point(0.8f, 0.35f), Point(0.2f, 0.65f), Point(0.8f, 0.65f)
        ), strokeCount = 2, wide = true)

        addT("/", Category.SYMBOL, listOf(
            Point(0.85f, 0.05f), Point(0.15f, 0.95f)
        ), strokeCount = 1)

        // ==================== ARABIC GLYPHS ====================
        addT("ا", Category.LETTER, listOf(
            Point(0.5f, 0.05f), Point(0.5f, 0.95f)
        ), strokeCount = 1, tall = true, lang = Language.ARABIC)

        addT("ب", Category.LETTER, listOf(
            Point(0.85f, 0.55f), Point(0.5f, 0.85f), Point(0.15f, 0.55f), Point(0.5f, 0.98f)
        ), strokeCount = 2, wide = true, lang = Language.ARABIC)

        addT("ت", Category.LETTER, listOf(
            Point(0.85f, 0.65f), Point(0.5f, 0.9f), Point(0.15f, 0.65f),
            Point(0.45f, 0.35f), Point(0.55f, 0.35f)
        ), strokeCount = 2, wide = true, lang = Language.ARABIC)

        addT("د", Category.LETTER, listOf(
            Point(0.75f, 0.35f), Point(0.4f, 0.65f), Point(0.75f, 0.85f)
        ), strokeCount = 1, lang = Language.ARABIC)

        addT("ر", Category.LETTER, listOf(
            Point(0.65f, 0.45f), Point(0.5f, 0.75f), Point(0.25f, 0.95f)
        ), strokeCount = 1, lang = Language.ARABIC)

        addT("س", Category.LETTER, listOf(
            Point(0.85f, 0.45f), Point(0.75f, 0.65f), Point(0.65f, 0.45f),
            Point(0.55f, 0.65f), Point(0.35f, 0.85f), Point(0.15f, 0.55f)
        ), strokeCount = 1, wide = true, lang = Language.ARABIC)

        addT("م", Category.LETTER, listOf(
            Point(0.65f, 0.45f), Point(0.45f, 0.35f), Point(0.55f, 0.55f),
            Point(0.75f, 0.55f), Point(0.75f, 0.95f)
        ), strokeCount = 1, closed = true, lang = Language.ARABIC)

        addT("و", Category.LETTER, listOf(
            Point(0.65f, 0.35f), Point(0.45f, 0.45f), Point(0.65f, 0.55f),
            Point(0.45f, 0.85f), Point(0.25f, 0.95f)
        ), strokeCount = 1, closed = true, lang = Language.ARABIC)
    }

    private fun addT(
        char: String,
        cat: Category,
        rawPoints: List<Point>,
        strokeCount: Int = 1,
        closed: Boolean = false,
        tall: Boolean = false,
        wide: Boolean = false,
        hasCross: Boolean = false,
        lang: Language = Language.ENGLISH
    ) {
        val resampled = StrokeNormalizer.resample(Stroke(rawPoints), LocalCharacterRecognizer.RESAMPLE_POINTS)
        val normalized = StrokeNormalizer.normalizeToUnitBox(listOf(resampled)).firstOrNull() ?: return
        templates.add(
            GestureTemplate(
                character = char,
                category = cat,
                language = lang,
                normalizedPoints = normalized.points,
                expectedStrokeCount = strokeCount,
                requiresClosedLoop = closed,
                isTall = tall,
                isWide = wide,
                hasCross = hasCross
            )
        )
    }
}
