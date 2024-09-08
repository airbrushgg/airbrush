package gg.airbrush.core.commands

import gg.airbrush.sdk.lib.Translations
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute

class MakeMeTiny : Command("tiny"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player)
            return

        val scale = sender.getAttribute(Attribute.fromNamespaceId("minecraft:generic.scale")!!)
        val enabled = scale.baseValue == 0.5

        scale.baseValue = if (enabled) 1.0 else 0.5
        sender.sendMessage(Translations.translate("core.commands.tiny", if (enabled) "no longer" else "now"))
    }
}