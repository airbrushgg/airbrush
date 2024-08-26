package gg.airbrush.core.commands

import gg.airbrush.core.lib.Constants
import gg.airbrush.core.lib.teleportToCanvas
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player


// TODO: Add support for fetching World by UUID.
//  Then we can check private status, fetch name, etc.
class Canvas : Command("canvas"), CommandExecutor {
    private val canvasId: ArgumentString = ArgumentType.String("canvas-id")

    init {
        defaultExecutor = this
        Constants.schematicFolder.mkdir()
        Constants.worldFolder.mkdir()

        addSyntax({ sender: CommandSender, context: CommandContext ->
            run(sender, context)
        }, canvasId)
    }

    private fun run(sender: CommandSender, context: CommandContext) {
        val player = sender as Player

        val canvasUUID = context.get(canvasId)

        player.teleportToCanvas(canvasUUID)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {}
}