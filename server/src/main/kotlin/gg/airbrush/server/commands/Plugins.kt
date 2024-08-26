package gg.airbrush.server.commands

import gg.airbrush.server.commands.plugins.Disable
import gg.airbrush.server.commands.plugins.Enable
import gg.airbrush.server.commands.plugins.Reload
import gg.airbrush.server.lib.mm
import gg.airbrush.server.pluginManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

class Plugins : Command("plugins", "pl"), CommandExecutor {
    init {
        defaultExecutor = this

        addSubcommand(Reload)
        addSubcommand(Enable)
        addSubcommand(Disable)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val plugins = pluginManager.plugins.values
        val list = plugins.joinToString("</p>, <p>") { it.info.name }
        sender.sendMessage("<s>Plugins (<p>${plugins.size}</p>): <p>$list".mm())
    }
}