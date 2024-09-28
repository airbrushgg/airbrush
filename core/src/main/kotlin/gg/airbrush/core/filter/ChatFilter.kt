

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

package gg.airbrush.core.filter

import cc.ekblad.toml.decode
import cc.ekblad.toml.model.TomlValue
import cc.ekblad.toml.tomlMapper
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.core.filter.parsing.Tag
import gg.airbrush.core.filter.parsing.Tokenizer
import gg.airbrush.core.filter.ruleset.RegexWordList
import gg.airbrush.core.filter.ruleset.TextWordList
import gg.airbrush.core.filter.ruleset.WordList
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.notExists
import kotlin.math.log

val chatFilterInstance = ChatFilter()

class ChatFilter {
    private val logger = LoggerFactory.getLogger(ChatFilter::class.java)

    private val configPath = ConfigUtils.loadResource(FilterConfig::class.java, "filter.toml", "core/")
    private var config: FilterConfig
    private val mapper = tomlMapper {
        mapping<FilterConfig>(
            "ruleset" to "rulesets"
        )
        decoder { tomlString: TomlValue.String -> FilterAction.valueOf(tomlString.value.uppercase()) }
    }

    private val tokenizer = Tokenizer()
    private val wordLists = ArrayList<WordList>()

    val logChannel get() = config.root.logChannel

    init {
        config = loadConfiguration()
        if (config.rulesets == null) {
            logger.warn("The chat filter disabled! No rulesets were defined in the configuration file.")
        }

        loadRulesets()
    }

    fun validateMessage(player: Player, message: String): FilterResult {
        val tokens = tokenizer.tokenize(message)
        for (wordList in wordLists) {
            logger.info("Checking ${wordList.ruleset.path}")
            tokens.firstOrNull {
                logger.info("Checking token: $it")
                return@firstOrNull wordList.test(it)
            }?.let { failedToken ->
                return FilterResult(wordList.ruleset, listOf(failedToken))
            }
        }
        return FilterResult(null, emptyList())
    }

    fun reloadFilterConfiguration() {
        val newConfiguration = loadConfiguration()
        if (newConfiguration.rulesets == null) {
            logger.warn("No filter rulesets were defined. Using previous word list instead.")
            return
        }

        config = newConfiguration
        loadRulesets()
    }

    private fun loadConfiguration(): FilterConfig {
        val maybeConfig = runCatching { mapper.decode<FilterConfig>(configPath) }
        return maybeConfig.getOrElse {
            logger.error("Failed to load filter configuration. Loading default instead.", maybeConfig.exceptionOrNull())
            val stream = javaClass.getResourceAsStream("/filter.toml")
                ?: throw IllegalStateException("Resource 'filter.toml' is missing in the JAR file.")
            return mapper.decode<FilterConfig>(stream)
        }
    }

    private fun loadRulesets() {
        wordLists.clear()
        for (set in config.rulesets.orEmpty()) {
            val path = Path("plugins/core/", set.path)
            if (path.notExists()) {
                logger.warn("Filter ruleset specifies path '${path.absolute()}' that does not exist. Skipping...")
                continue
            }

            val wordList = if (set.regex) {
                RegexWordList(set, path)
            } else {
                TextWordList(set, path)
            }
            wordLists.add(wordList)
        }
        wordLists.sortByDescending { it.ruleset.priority }
        logger.info("Loaded ${wordLists.size} filter rulesets.")
    }
}