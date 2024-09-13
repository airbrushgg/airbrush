

/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2023 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.core.filter.rules

import gg.airbrush.core.filter.FilterAction
import gg.airbrush.core.filter.FilterConfig
import gg.airbrush.core.filter.FilterRule
import gg.airbrush.server.lib.mm
import net.minestom.server.MinecraftServer
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
                MinecraftServer.LOGGER.info("[Core] Rule defined in filter.toml does not contain 'pattern' or 'file'")
                return@filter false
            }
            if (rule.file != null && rule.pattern != null) {
                MinecraftServer.LOGGER.info("[Core] Rule defined in filter.toml has both 'pattern' and 'file'")
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

        MinecraftServer.LOGGER.info("[Core] Loaded ${validRules.size} user-defined filter rules (out of ${filterConfig.rules.orEmpty().size})")
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