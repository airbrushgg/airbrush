package gg.airbrush.core.commands

import gg.airbrush.core.lib.CanvasManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player

class Create : Command("create"), CommandExecutor {
    init {
        defaultExecutor = this

	    setCondition { sender, _ -> sender.hasPermission("core.superdonor") }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val player = sender as Player

        CanvasManager.create(player)
    }
}