

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
import gg.airbrush.core.filter.rules.FileDefinedRule
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.server.plugins.PluginInfo
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.nio.file.Path

object ChatFilter {
    private lateinit var filterConfigPath: Path
    private lateinit var filterConfig: FilterConfig

    val logChannel = filterConfig.logChannel

    private val mapper = tomlMapper {
        mapping<FilterConfig> (
            "rule" to "rules"
        )
    }

    private val registeredRules = ArrayList<FilterRule>()

    fun initialize(info: PluginInfo) {
        filterConfigPath = ConfigUtils.loadResource(FilterConfig::class.java, "filter.toml", info)
        loadRules()
    }

    private fun loadRules() {
        filterConfig = mapper.decode(filterConfigPath)

        registerRule(FileDefinedRule(filterConfig))
        MinecraftServer.LOGGER.info("[Core] Loaded ${registeredRules.size} internal filter rules.")
    }

    fun reloadRules() {
        registeredRules.clear()
        loadRules()
    }

    fun shouldBlock(player: Player, message: String): Boolean {
        return registeredRules.any { rule -> rule.apply(player, message) >= FilterAction.BLOCK }
    }

    @Suppress("unused")
    // TODO(cal): Do we want to swap to this so we can log what filter rules were triggered? See events/PlayerChat.kt:36
    fun validateMessage(player: Player, message: String): FilterResult {
        val failedChecks = registeredRules
            .filter { rule -> rule.apply(player, message) >= FilterAction.BLOCK }

        return FilterResult(
            if(failedChecks.isNotEmpty()) FilterAction.BLOCK else FilterAction.ALLOW,
            failedChecks
        )
    }

    private fun registerRule(rule: FilterRule) {
        if (registeredRules.contains(rule))
            return
        registeredRules += rule
    }
}