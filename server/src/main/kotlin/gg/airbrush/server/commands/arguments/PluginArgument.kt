package gg.airbrush.server.commands.arguments

import gg.airbrush.server.pluginManager
import gg.airbrush.server.plugins.PLUGIN_REGEX
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.binary.BinaryWriter

class PluginArgument(id: String) : Argument<Plugin>(id) {
    init {
        setSuggestionCallback { _, context, suggestions ->
            val name = context.getRaw(id)

            for (plugin in pluginManager.plugins.values)
                if (name in plugin.info.id)
                    suggestions.addEntry(SuggestionEntry(plugin.info.id))
        }
    }

    override fun parse(sender: CommandSender, input: String): Plugin {
        if (!input.matches(PLUGIN_REGEX))
            throw ArgumentSyntaxException("Invalid plugin ID.", input, 1)

        return pluginManager.plugins.values.find { it.info.id.lowercase() == input.lowercase() }
            ?: throw ArgumentSyntaxException("Plugin not found.", input, 2)
    }

    override fun parser(): String {
        return "brigadier:string"
    }

    override fun nodeProperties(): ByteArray? {
        return BinaryWriter.makeArray { writer -> writer.writeVarInt(0) }
    }
}