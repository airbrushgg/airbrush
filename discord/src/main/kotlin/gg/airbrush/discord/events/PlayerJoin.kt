package gg.airbrush.discord.events

import gg.airbrush.discord.bot
import gg.airbrush.discord.discordConfig
import gg.airbrush.discord.lib.Placeholder
import gg.airbrush.discord.lib.pp
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent

object PlayerJoin {
	init {
		val eventHandler = MinecraftServer.getGlobalEventHandler()

        eventHandler.addListener(PlayerSpawnEvent::class.java) { event ->
            handle(event.player, type = "join")
        }

        eventHandler.addListener(PlayerDisconnectEvent::class.java) { event ->
            handle(event.player, type = "leave")
        }
	}

	private fun handle(player: Player, type: String) {
		val configMsg = when (type) {
			"join" -> discordConfig.join.content
			"leave" -> discordConfig.leave.content
			else -> throw Exception("$type is an invalid event to handle")
		}

		val parsedMsg = configMsg.pp(
			listOf(
				Placeholder("%name%", player.username),
			)
		)

		val channel = bot.getTextChannelById(discordConfig.channel.toLong())
			?: throw Exception("Failed to find chat channel!")
		channel.sendMessage(parsedMsg)
	}
}