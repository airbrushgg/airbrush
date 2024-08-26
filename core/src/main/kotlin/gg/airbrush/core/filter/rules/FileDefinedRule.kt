package gg.airbrush.core.filter.rules

import gg.airbrush.core.filter.FilterAction
import gg.airbrush.core.filter.FilterConfig
import gg.airbrush.core.filter.FilterRule
import gg.airbrush.server.lib.mm
import net.minestom.server.entity.Player
import java.nio.file.Path
import kotlin.io.path.notExists
import kotlin.io.path.readLines

class FileDefinedRule(private val filterConfig: FilterConfig) : FilterRule {
    private val parsedPatternLists = HashMap<FilterConfig.RuleDefinition, PatternList>()

    private var validRules: List<FilterConfig.RuleDefinition> = filterConfig.rules
        .orEmpty()
        .filter { rule ->
            if (rule.file == null && rule.pattern == null) {
                println("[Core] Rule defined in filter.toml does not contain 'pattern' or 'file'")
                return@filter false
            }
            if (rule.file != null && rule.pattern != null) {
                println("[Core] Rule defined in filter.toml has both 'pattern' and 'file'")
                return@filter false
            }
            if (rule.file != null && Path.of(rule.file).notExists()) {
                print("[Core] Rule specifies 'file' that does not exist")
                return@filter false
            }
            true
        }
        .sortedBy { rule -> rule.action }

    init {
        validRules
            .filter { rule -> rule.file != null }
            .forEach { rule -> parsedPatternLists[rule] = parseFile(Path.of(rule.file!!)) }
        println("[Core] Loaded ${validRules.size} user-defined filter rules (out of ${filterConfig.rules.orEmpty().size})")
    }

    override fun apply(player: Player, message: String): FilterAction {
        val matchedRules = validRules.filter { rule -> hasMatch(rule, message) }
        if (matchedRules.isEmpty())
            return FilterAction.ALLOW

        val first = validRules.first()
        val blockMessage = first.blockMessage ?: filterConfig.root.blockMessage

        player.sendMessage(blockMessage.mm())
        return first.action ?: FilterAction.BLOCK
    }

    private fun hasMatch(rule: FilterConfig.RuleDefinition, message: String): Boolean {
        return when {
            rule.pattern != null -> {
                val regex = Regex(rule.pattern)
                regex.containsMatchIn(message)
            }
            rule.file != null -> {
                val list = parsedPatternLists[rule]?.patterns
                list.orEmpty().any { pattern -> pattern.containsMatchIn(message) }
            }
            else -> false
        }
    }

    private fun parseFile(filePath: Path): PatternList {
        val list = filePath.readLines().map { line -> Regex(line) }
        return PatternList(list)
    }

    private data class PatternList(val patterns: List<Regex>)
}