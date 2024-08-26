package gg.airbrush.server.commands.plugins

import gg.airbrush.server.commands.arguments.PluginArgument
import gg.airbrush.server.lib.mm
import gg.airbrush.server.pluginManager
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType

object Disable : Command("disable") {
    init {
        setCondition { sender, _ -> sender.hasPermission("airbrush.pluginManager") }
        addSyntax({ sender, context ->
            val plugin = context.get<Plugin>("plugin")

            if (!plugin.isSetup) {
                sender.sendMessage("<s>Plugin <p>${plugin.info.id}</p> is not enabled.".mm())
                return@addSyntax
            }

            sender.sendMessage("<s>Disabling plugin <p>${plugin.info.id}</p>...".mm())
            plugin.teardown()
            plugin.isSetup = false
            sender.sendMessage("<s>Plugin <p>${plugin.info.id}</p> has been disabled.".mm())
        }, PluginArgument("plugin"))
    }
}