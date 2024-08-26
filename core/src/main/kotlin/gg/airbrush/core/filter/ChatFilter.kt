package gg.airbrush.core.filter

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.airbrush.core.filter.rules.FileDefinedRule
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.server.plugins.PluginInfo
import net.minestom.server.entity.Player
import java.nio.file.Path

object ChatFilter {
    private lateinit var filterConfigPath: Path
    private lateinit var filterConfig: FilterConfig

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
        println("[Core] Loaded ${registeredRules.size} internal filter rules")
    }

    fun reloadRules() {
        registeredRules.clear()
        loadRules()
    }

    fun shouldBlock(player: Player, message: String): Boolean {
        return registeredRules.any { rule -> rule.apply(player, message) >= FilterAction.BLOCK }
    }

    private fun registerRule(rule: FilterRule) {
        if (registeredRules.contains(rule))
            return
        registeredRules += rule
    }
}