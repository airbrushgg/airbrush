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