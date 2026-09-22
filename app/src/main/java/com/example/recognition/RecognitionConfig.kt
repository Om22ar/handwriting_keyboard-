package com.example.recognition

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ARABIC("ar", "العربية (Arabic)");

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

enum class RecognitionMode(val displayName: String) {
    AUTO("Auto (ABC / 123)"),
    LETTERS("Letters (A-Z, a-z)"),
    NUMBERS("Numbers (0-9)"),
    SYMBOLS("Symbols (!@#)")
}
