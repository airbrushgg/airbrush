package gg.airbrush.core.commands.admin

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player

class Vanish : Command("vanish", "v"), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.staff") }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player)
            return

        sender.isInvisible = !sender.isInvisible
	    sender.sendMessage(
		    Translations.translate("core.commands.vanish", if(sender.isInvisible) "invisible" else "visible").mm()
	    )
    }
}