package gg.airbrush.discord

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.airbrush.discord.data.DiscordConfig
import gg.airbrush.discord.events.HandleLink
import gg.airbrush.discord.events.PlayerChat
import gg.airbrush.discord.events.PlayerJoin
import gg.airbrush.discord.gameCommands.LinkCommand
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.MinecraftServer

lateinit var discordConfig: DiscordConfig

class DiscordPlugin : Plugin() {
    private val mapper = tomlMapper {}

    override fun setup() {
        val configPath = ConfigUtils.loadResource(
            clazz = DiscordPlugin::class.java,
            fileName = "config.toml",
            pluginInfo = this.info
        )
        discordConfig = mapper.decode(configPath)

	    registerCommands()

	    HandleLink
	    PlayerJoin
	    PlayerChat

	    Discord.load()
    }

    override fun teardown() {
        // On shutdown
	    bot.shutdownNow()
    }

	private fun registerCommands() {
		val manager = MinecraftServer.getCommandManager()
		manager.register(LinkCommand())
	}
}