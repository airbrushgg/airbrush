package gg.airbrush.core.filter.parsing

class Tokenizer {
    private var source: String = ""
    private var position: Int = 0
    private val current get() = source.getOrElse(position) { return@getOrElse '\u0000' }

    fun tokenize(source: String): List<TokenSpan> {
        // Reset the state of the tokenizer so that we can reuse it.
        this.source = source
        this.position = 0

        val tokens = ArrayList<TokenSpan>()

        while (position < source.length) {
            // Skip whitespace and punctuation between tokens.
            while (current.isWhitespace()) {
                position++
            }

            val start = position

            // Handle punctuation case.
            if (current.isPunctuation()) {
                val first = current
                position++
                // Merge similar punctuation together.
                while (current.isPunctuation() && current == first) {
                    position++
                }
                tokens.add(TokenSpan(source.substring(start, position), Tag.PUNCTUATION, position))
                continue
            }

            if (current.isLetter()) {
                position++
                val tag = scanWord()
                tokens.add(TokenSpan(source.substring(start, position), tag, position))
                continue
            }

            if (current.isDigit()) {
                position++
                val tag = scanNumber()
                tokens.add(TokenSpan(source.substring(start, position), tag, position))
                continue
            }

            if (current.isLetterOrDigit()) {
                position++
                val tag = scanMixedWord()
                tokens.add(TokenSpan(source.substring(start, position), tag, position))
                continue
            }

            // Handle unknown case.
            position++
            tokens.add(TokenSpan(source.substring(start..position), Tag.UNKNOWN, position))
        }

        return tokens
    }

    private fun scanWord(): Tag {
        // TODO: We should handle mixed scripts here. For example, "ab汉字c" should be {"ab", "汉字", "c"}.
        while (current.isLetter()) {
            position++
        }
        // We have encountered a digit, parse it as a mixed word instead.
        if (current.isDigit()) return scanMixedWord()
        return Tag.WORD
    }

    private fun scanNumber(): Tag {
        while (current.isDigit()) {
            position++
        }
        // If we encounter a letter, parse it as a mixed word instead.
        if (current.isLetter()) return scanMixedWord()

        // Else, if we encounter a decimal point, parse the rest of the number.
        if (current == '.' && peek(1).isDigit()) {
            position++
            while (current.isDigit()) {
                position++
            }
        }
        return Tag.NUMBER
    }

    private fun scanMixedWord(): Tag {
        while (current.isLetterOrDigit()) {
            position++
        }
        return Tag.MIXED_WORD
    }

    private fun peek(offset: Int): Char {
        return source.getOrElse(position + offset) { return@getOrElse '\u0000' }
    }

    private fun Char.isASCIILetter(): Boolean {
        return this.code in 65..90 || this.code in 97..122
    }

    private fun Char.isPunctuation(): Boolean {
        return when (this.code) {
            in 33..47 -> true
            in 58..64 -> true
            in 91..96 -> true
            in 123..126 -> true
            else -> false
        }
    }
}