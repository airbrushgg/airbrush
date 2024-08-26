package gg.airbrush.core.commands.admin

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class StaffChat : Command("staffchat", "sc"), CommandExecutor {
	private val messageArg = ArgumentType.StringArray("message")

    init {
        setCondition { sender, _ -> sender.hasPermission("core.staff") }
        addSyntax(this::apply, messageArg)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val message = context.get(messageArg)
        val player = sender as Player

        Audiences.players { p -> p.hasPermission("core.staff") }
            .sendMessage(Translations.translate("core.commands.staff_chat", player.username, message.joinToString(" ") { it }).mm())
    }
}