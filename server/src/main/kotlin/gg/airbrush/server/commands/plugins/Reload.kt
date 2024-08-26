package gg.airbrush.server.commands.plugins

import gg.airbrush.server.commands.arguments.PluginArgument
import gg.airbrush.server.lib.mm
import gg.airbrush.server.pluginManager
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType

object Reload : Command("reload") {
    init {
        setCondition { sender, _ -> sender.hasPermission("airbrush.pluginManager") }
        addSyntax({ sender, context ->
            val plugin = context.get<Plugin>("plugin")

            sender.sendMessage("<s>Reloading plugin <p>${plugin.info.id}</p>...".mm())
            if (plugin.isSetup) plugin.teardown()
            plugin.setup()
            sender.sendMessage("<s>Plugin <p>${plugin.info.id}</p> has been reloaded.".mm())
        }, PluginArgument("plugin"))
    }
}