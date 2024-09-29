package gg.airbrush.core.filter.ruleset

import gg.airbrush.core.filter.FilterConfig
import gg.airbrush.core.filter.parsing.Tag
import gg.airbrush.core.filter.parsing.TokenSpan
import java.nio.file.Files
import java.nio.file.Path

class TextWordList(override val ruleset: FilterConfig.Ruleset, path: Path) : WordList {
    private val words: List<String> = Files.readAllLines(path)

    override fun test(token: TokenSpan): Boolean {
        if (token.tag != Tag.WORD) {
            return false
        }
        return words.contains(token.value)
    }
}