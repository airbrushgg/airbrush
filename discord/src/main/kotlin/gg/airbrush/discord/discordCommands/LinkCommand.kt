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

package gg.airbrush.discord.discordCommands

import gg.airbrush.discord.discordConfig
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.linking.Linking
import gg.airbrush.sdk.lib.Placeholder
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.sdk.lib.parsePlaceholders
import gg.airbrush.server.lib.mm
import me.santio.coffee.common.annotations.Command
import me.santio.coffee.jda.annotations.Description
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.minestom.server.MinecraftServer

@Command
@Description("Link your Discord account to your Minecraft account")
class LinkCommand {
	private fun SlashCommandInteractionEvent.answer(msg: String) {
		return this.hook.sendMessage(msg).queue()
	}

	fun main(e: SlashCommandInteractionEvent, username: String) {
		val playerUUID = PlayerUtils.getUUID(username)
		e.deferReply(true).queue()

		val linkSession: Linking.LinkData
		try {
			linkSession = SDK.linking.createSession(playerUUID, e.user.idLong)
		} catch (ex: Exception) {
			MinecraftServer.LOGGER.info(ex.message)
			throw ex
		}

		val msg = discordConfig.linkRequest.parsePlaceholders(
			listOf(
				Placeholder("%username%", e.user.name),
				Placeholder("%sessionId%", linkSession.code)
			)
		)

		val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerUUID)
		if(player == null) {
			e.answer("$username is not online!")
			return
		}

		player.sendMessage(msg.mm())

		e.answer("Check in game!")
	}
}

