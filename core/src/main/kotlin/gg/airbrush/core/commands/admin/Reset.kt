package gg.airbrush.core.commands.admin

import dev.flavored.bamboo.SchematicReader
import gg.airbrush.core.lib.Constants
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player

class Reset : Command("resetworld"), CommandExecutor {
    private val schematicReader = SchematicReader()

    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.admin") }

        val confirmFlag = ArgumentLiteral("confirm")
        addSyntax(this::executeConfirm, confirmFlag)
    }

    private fun executeConfirm(sender: CommandSender, context: CommandContext) {
        if (sender !is Player)
            return

        val schematicFile = Constants.schematicFolder.resolve("spawn.schem")
        if (!schematicFile.exists()) {
            sender.sendMessage("<error>Could not resolve 'spawn.schem' file.".mm())
            return
        }

        val instance = sender.instance
        if (sender.instance != WorldManager.defaultInstance) {
            sender.sendMessage("<error>You must be in the spawn world to reset it.".mm())
            return
        }

        val center = Pos(0.0, 4.0, 0.0)
        val schematic = schematicReader.fromPath(schematicFile.toPath())
        schematic.paste(instance, center, true)

        instance.saveChunksToStorage().join()
        sender.sendMessage("<success>The spawn world was reset.".mm())

        instance.players.forEach { player -> player.teleport(center.add(0.0, 2.0, 0.0)) }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("<p><em>This is an irreversible action!</em> <s>To reset the world, do <p>/resetworld confirm<s>.".mm())
    }
}