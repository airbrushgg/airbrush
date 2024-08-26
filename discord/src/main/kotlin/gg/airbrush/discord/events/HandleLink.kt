package gg.airbrush.discord.events

import gg.airbrush.discord.bot
import gg.airbrush.discord.discordConfig
import gg.airbrush.sdk.events.LinkAccountEvent
import gg.airbrush.server.lib.mm
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.minestom.server.MinecraftServer

object HandleLink {
	init {
		val eventHandler = MinecraftServer.getGlobalEventHandler()

		eventHandler.addListener(LinkAccountEvent::class.java) { event ->
			linkEvent(event)
		}
	}

	private fun linkEvent(event: LinkAccountEvent) {
		// Fetch the chat channel, this allows us to get the guild.
		val channel = bot.getGuildChannelById(ChannelType.TEXT, discordConfig.channel.toLong()) ?: throw Exception("Failed to find guild!")

		val paintersRole = channel.guild.roles.find {
			it.name.lowercase() == "painters"
		} ?: throw Exception("Failed to find role.")

		val member = channel.guild.findMembers {
			it.idLong == event.getDiscordID()
		}.get().firstOrNull() ?: throw Exception("Failed to find member.")

		val userSnowflake = UserSnowflake.fromId(member.user.idLong)
		channel.guild.addRoleToMember(userSnowflake, paintersRole).complete()

		event.player.sendMessage("<success>Successfully linked your Discord to your Minecraft account!".mm())
	}
}