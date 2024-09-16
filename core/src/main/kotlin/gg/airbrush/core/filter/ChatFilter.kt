

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
import cc.ekblad.toml.tomlMapper
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.core.filter.parsing.Tag
import gg.airbrush.core.filter.parsing.Tokenizer
import net.minestom.server.entity.Player
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val chatFilterInstance = ChatFilter()

class ChatFilter {
    private val logger = LoggerFactory.getLogger(ChatFilter::class.java)
    private val mapper = tomlMapper { }

    private val config: FilterConfig
    private val tokenizer = Tokenizer()
    private val blockedWords: MutableList<String> = fetchWordList().toMutableList()

    val logChannel get() = config.logChannel

    init {
        val configPath = ConfigUtils.loadResource(FilterConfig::class.java, "filter.toml", "core/")
        config = mapper.decode(configPath)

        if (blockedWords.isEmpty()) {
            logger.warn("The chat filter disabled! Failed to load the filter word list (or list returned empty).")
        }
    }

    // TODO(cal): Do we want to swap to this so we can log what filter rules were triggered? See events/PlayerChat.kt:36
    fun validateMessage(player: Player, message: String): FilterResult {
        //
//        val failedChecks = registeredRules
//            .filter { rule -> rule.apply(player, message) >= FilterAction.BLOCK }

        val tokens = tokenizer.tokenize(message)

        val failedTokens = tokens.filter {
            it.tag == Tag.WORD && blockedWords.contains(it.value)
        }

        return FilterResult(
            if (failedTokens.isNotEmpty()) FilterAction.BLOCK else FilterAction.ALLOW,
            failedTokens
        )
    }

    fun clearAndFetchWordList() {
        val newList = fetchWordList()
        if (newList.isEmpty()) {
            logger.warn("Failed to fetch the filter word list (or list returned empty). Using previous word list instead.")
            return
        }

        blockedWords.clear()
        blockedWords.addAll(newList)
    }

    companion object {
        // This is hard-coded for now. Ideally, we would want to fetch these from an API endpoint.
        private const val WORDLIST_URL: String = "https://gist.githubusercontent.com/AppleFlavored/b04bc246d0dc7361c53c27228f1592b4/raw/0d01f7b51e5d6530b58ad4b604c58dac6126943e/wordlist.txt"
        private val client = HttpClient.newHttpClient()

        private fun fetchWordList(): List<String> {
            val request = HttpRequest.newBuilder(URI.create(WORDLIST_URL)).build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofLines())

            return when (response.statusCode()) {
                200, 304 -> response.body().toList()
                else -> emptyList()
            }
        }
    }
}