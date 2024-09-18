package gg.airbrush.splatoon.commands

import gg.airbrush.server.lib.mm
import gg.airbrush.splatoon.GameManager
import gg.airbrush.splatoon.GameParameters
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.ArgumentWord

class SplatoonCommand : Command("splatoon") {
    init {
        setDefaultExecutor { sender, context ->  }
        addSubcommand(StartSubcommand())
        addSubcommand(StopSubcommand())
    }
}

internal class StartSubcommand : Command("start"), CommandExecutor {
    init {
        setCondition { sender, _ -> sender.hasPermission("splatoon.manage") }
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {

    }
}

internal class StopSubcommand : Command("stop"), CommandExecutor {

    init {
        setCondition { sender, _ -> sender.hasPermission("splatoon.manage") }
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {

    }
}

internal class ParameterSubcommand : Command("parameter", "param") {
    private val parameterName = ArgumentType.Enum("name", GameParameters.Parameter::class.java)
        .setFormat(ArgumentEnum.Format.LOWER_CASED)
    private val parameterValue = ArgumentType.Integer("value")

    init {
        setCondition { sender, _ -> sender.hasPermission("splatoon.manage") }
        setDefaultExecutor(this::listParameters)

        val setLiteral = ArgumentType.Literal("set")
        addSyntax(this::setParameter, setLiteral, parameterName, parameterValue)

        val getLiteral = ArgumentType.Literal("get")
        addSyntax(this::getParameter, getLiteral, parameterName)

        val resetLiteral = ArgumentType.Literal("reset")
        addSyntax(this::resetParameter, resetLiteral, parameterName)
    }

    private fun listParameters(sender: CommandSender, context: CommandContext) {
        val component = Component.text("<s>All parameters:</s>")
        for (entry in GameParameters.Parameter.entries) {
            val value = GameManager.parameters.get(entry)
            component.appendNewline().append("  <s>* '<p>${entry.name.lowercase()}</p>' has the value <p>$value</p>".mm())
        }
        sender.sendMessage(component)
    }

    private fun setParameter(sender: CommandSender, context: CommandContext) {
        val name = context.get(parameterName)
        val value = context.get(parameterValue)

        val previousValue = GameManager.parameters.get(name)
        GameManager.parameters.set(name, value)

        sender.sendMessage("<s>Parameter '<p>$name</p>' has been set to <p>$value</p> (prev: <p>$previousValue</p>).".mm())
    }

    private fun getParameter(sender: CommandSender, context: CommandContext) {
        val name = context.get(parameterName)
        val value = GameManager.parameters.get(name)
        sender.sendMessage("<s>Parameter '<p>$name</p>' has the value <p>$value</p>.".mm())
    }

    private fun resetParameter(sender: CommandSender, context: CommandContext) {
        val name = context.get(parameterName)

        val previousValue = GameManager.parameters.get(name)
        GameManager.parameters.set(name, name.defaultValue)

        sender.sendMessage("<s>Parameter '<p>$name</p>' has been reset to <p>${name.defaultValue}</p> (prev: <p>$previousValue</p>).".mm())
    }
}