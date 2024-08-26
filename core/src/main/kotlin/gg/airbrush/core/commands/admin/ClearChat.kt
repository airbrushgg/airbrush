package gg.airbrush.core.commands.admin

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

class ClearChat : Command("clearchat", "clear"), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.staff") }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        Audiences.players().sendMessage("\n".repeat(100).mm())
        sender.sendMessage(Translations.translate("core.commands.clearchat").mm())
    }
}