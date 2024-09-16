package gg.airbrush.core.filter.parsing

data class TokenSpan(val value: String, val tag: Tag)

enum class Tag {
    WORD,
    MIXED_WORD,
    NUMBER,
    PUNCTUATION,
    UNKNOWN,
}