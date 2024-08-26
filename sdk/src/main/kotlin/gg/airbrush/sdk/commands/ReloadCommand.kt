package gg.airbrush.sdk.commands

import gg.airbrush.sdk.lib.Translations
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext

class ReloadCommand : Command("reloadmsgs") {
	init {
		setCondition { sender, _ ->
			sender.hasPermission("core.admin")
		}

		addSyntax(this::apply)
	}


	private fun apply(sender: CommandSender, context: CommandContext) {
		Translations.reload()
		sender.sendMessage("Reloaded message configurations.")
	}
}