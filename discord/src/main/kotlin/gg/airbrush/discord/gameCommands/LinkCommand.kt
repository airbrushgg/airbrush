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