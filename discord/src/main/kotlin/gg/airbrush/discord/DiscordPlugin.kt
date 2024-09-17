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

package gg.airbrush.discord

import cc.ekblad.toml.decode
import cc.ekblad.toml.model.TomlException
import cc.ekblad.toml.tomlMapper
import gg.airbrush.discord.data.DiscordConfig
import gg.airbrush.discord.events.HandleLink
import gg.airbrush.discord.events.PlayerChat
import gg.airbrush.discord.events.PlayerJoin
import gg.airbrush.discord.gameCommands.LinkCommand
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.server.pluginManager
import gg.airbrush.server.plugins.Plugin
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventNode

lateinit var discordConfig: DiscordConfig
internal val eventNode = EventNode.all("Discord")

class DiscordPlugin : Plugin() {
    private val mapper = tomlMapper {}

    override fun setup() {
        val configPath = ConfigUtils.loadResource(
            clazz = DiscordPlugin::class.java,
            fileName = "config.toml",
            pluginInfo = this.info
        )

        try {
            discordConfig = mapper.decode<DiscordConfig>(configPath)
        } catch (e: TomlException) {
            MinecraftServer.LOGGER.error("[Discord] Failed to load discord config file.", e)
            pluginManager.disablePlugin(this)
            return
        }

        registerCommands()

        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
        HandleLink
        PlayerJoin
        PlayerChat

        try {
            Discord.load()
        } catch (e: IllegalArgumentException) {
            MinecraftServer.LOGGER.error("[Discord] No token was provided.", e)
            pluginManager.disablePlugin(this)
            return
        } catch (e: InvalidTokenException) {
            MinecraftServer.LOGGER.error("[Discord] Bot was provided with an invalid token.", e)
            pluginManager.disablePlugin(this)
            return
        }
    }

    override fun teardown() {
        // On shutdown
	    bot.shutdownNow()
        MinecraftServer.getGlobalEventHandler().removeChild(eventNode)
    }

	private fun registerCommands() {
		val manager = MinecraftServer.getCommandManager()
        if(!manager.commandExists("link")) {
            manager.register(LinkCommand())
        }
	}
}