package gg.airbrush.core.commands.admin

import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class SetRadius : Command("setradius", "sr"), CommandExecutor {
	init {
		setCondition { sender, _ -> sender.hasPermission("core.staff") }
		addSyntax(this::apply, ArgumentType.Integer("radius"))
	}

	override fun apply(sender: CommandSender, context: CommandContext) {
		val radius = context.get<Int>("radius")
		val player = sender as Player

		val sdkPlayer = SDK.players.get(player.uuid)
		sdkPlayer.setRadius(radius, true)

		player.sendMessage(Translations.translate("core.commands.radius", radius).mm())
	}
}