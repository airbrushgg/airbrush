package gg.airbrush.core.commands.admin

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

class Lockdown : Command("lockdown"), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.staff") }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        locked = !locked

        sender.sendMessage(
	        Translations.translate("core.commands.lockdown", if(locked) "disabled" else "enabled").mm()
        )
    }

    companion object {
        private var locked = false
        fun isLockedDown(): Boolean = locked
    }
}