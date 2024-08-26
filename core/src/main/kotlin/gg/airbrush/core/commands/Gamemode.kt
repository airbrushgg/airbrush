package gg.airbrush.core.commands

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

class Gamemode : Command("gamemode", "gm"), CommandExecutor {
    init {
	    setCondition { sender, _ -> sender.hasPermission("core.gamemode") }

        addSyntax(this::apply, ArgumentType
            .Enum("gamemode", GameMode::class.java)
            .setFormat(ArgumentEnum.Format.LOWER_CASED))
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val gamemode = context.get<GameMode>("gamemode")
        val player = sender as Player

        player.gameMode = gamemode
        player.isAllowFlying = true

        player.sendMessage(Translations.translate("core.commands.gamemode", gamemode.name.lowercase()).mm())
    }
}