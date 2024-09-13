

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

package gg.airbrush.discord.gameCommands

import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.events.LinkAccountEvent
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import java.util.*

class LinkCommand : Command("link"), CommandExecutor {
	private val actionArgument = ArgumentType.Word("action")
		.from("accept", "deny")
	private val sessionIdArgument = ArgumentType.String("sessionId")

	init {
		defaultExecutor = this

		addSyntax({ sender: CommandSender, context: CommandContext ->
			run(sender, context)
		}, actionArgument, sessionIdArgument)
	}

	private fun run(sender: CommandSender, context: CommandContext) {
		val player = sender as Player

		val action = context.get(actionArgument)
		val sessionId = context.get(sessionIdArgument)
		val sessionUUID = UUID.fromString(sessionId)

		val session = SDK.linking.getSession(sessionUUID)

		if(session == null) {
			player.sendMessage("<error>Invalid session!".mm())
			return
		}

		when(action) {
			"accept" -> {
				// idea: maybe move the event call to the SDK?
				SDK.linking.verifySession(sessionUUID)
				EventDispatcher.call(LinkAccountEvent(player, session.discordId))
			}
			else -> return
		}
	}

	override fun apply(sender: CommandSender, context: CommandContext) {}
}