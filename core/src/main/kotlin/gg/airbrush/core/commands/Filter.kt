package gg.airbrush.core.commands

import gg.airbrush.core.filter.ChatFilter
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType

class Filter : Command("filter"), CommandExecutor {
    private val action = ArgumentType.Enum("action", Action::class.java)
        .setFormat(ArgumentEnum.Format.LOWER_CASED)

    init {
        defaultExecutor = this

	    setCondition { sender, _ -> sender.hasPermission("core.staff") }

        addSyntax({ sender: CommandSender, context: CommandContext ->
            runReload(sender, context)
        }, action)
    }

    private fun runReload(sender: CommandSender, context: CommandContext) {
        when (context.get(action)) {
            Action.RELOAD -> {
                ChatFilter.reloadRules()
                sender.sendMessage("<success>Reloaded filter configuration!".mm())
            }
            else -> {
                sender.sendMessage("<error>Invalid action for /filter".mm())
            }
        }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("<error>Invalid action for /filter".mm())
    }

    private enum class Action {
        RELOAD
    }
}