package gg.airbrush.core.commands

import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType

class TestColors : Command("testcolors"), CommandExecutor {
    private val messageArg = ArgumentType.StringArray("message")

    init {
        defaultExecutor = this

	    setCondition { sender, _ -> sender.hasPermission("core.staff") }

        addSyntax({ sender: CommandSender, context: CommandContext ->
            run(sender, context)
        }, messageArg)
    }

    private fun run(sender: CommandSender, context: CommandContext) {
        val message = context.get(messageArg)
        sender.sendMessage(message.joinToString(" ") { it }.mm())
    }

    override fun apply(sender: CommandSender, context: CommandContext) {}
}