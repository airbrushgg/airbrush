package gg.airbrush.core.filter.ruleset

import gg.airbrush.core.filter.FilterConfig
import gg.airbrush.core.filter.parsing.TokenSpan

interface WordList {
    val ruleset: FilterConfig.Ruleset
    fun test(token: TokenSpan): Boolean
}