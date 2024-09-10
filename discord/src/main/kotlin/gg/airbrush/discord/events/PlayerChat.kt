package gg.airbrush.discord.events

import gg.airbrush.discord.bot
import gg.airbrush.discord.discordConfig
import gg.airbrush.discord.lib.Placeholder
import gg.airbrush.discord.lib.pp
import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerChatEvent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object PlayerChat : ListenerAdapter() {
	init {
		val discordChatNode = EventNode.type("Discord", EventFilter.PLAYER)
		MinecraftServer.getGlobalEventHandler().addChild(discordChatNode)

		discordChatNode.addListener(PlayerChatEvent::class.java) { event ->
			chatMessage(event)
		}
	}

	private fun chatMessage(event: PlayerChatEvent) {
		if(event.isCancelled) return

		val configMsg = discordConfig.chat.content
		val channel = bot.getTextChannelById(discordConfig.channel.toLong())
			?: throw Exception("Failed to find guild!")

		val rank = PlainTextComponentSerializer.plainText().serialize(
			SDK.players.get(event.player.uuid).getRank().getData().prefix.mm()
		)

		val timestamp = DateTimeFormatter.ofPattern("HH:mm:ss z").format(ZonedDateTime.now())

		val parsedMsg = configMsg.pp(
			listOf(
				Placeholder("%timestamp%", timestamp),
				Placeholder("%rank%", rank),
				Placeholder("%name%", event.player.username),
				Placeholder("%message%", event.message)
			)
		)

		channel.sendMessage(parsedMsg).queue()
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if(event.channel.id != discordConfig.channel) return
		if(event.author.isBot) return

		val configMsg = discordConfig.ingame.content
		val msgContent = event.message.contentDisplay

		val parsedMsg = configMsg.pp(listOf(
			Placeholder("%message%", msgContent),
			Placeholder("%display-name%", event.author.effectiveName),
			Placeholder("%name%", event.author.name),
			Placeholder("%tag%", event.author.globalName ?: ""),
		)).mm()

		val plainText = PlainTextComponentSerializer.plainText().serialize(parsedMsg)

		// TODO(cal): Test.
		MinecraftServer.LOGGER.info(plainText)

		MinecraftServer.getConnectionManager().onlinePlayers.forEach {
			it.sendMessage(parsedMsg)
		}
	}
}