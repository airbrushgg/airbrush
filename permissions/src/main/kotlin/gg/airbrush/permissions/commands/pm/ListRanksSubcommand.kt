package gg.airbrush.permissions.commands.pm

import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

object ListRanksSubcommand : Command("listRanks"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val ranks = SDK.ranks.list()
        var component = "<s>Ranks (<p>${ranks.size}</p>): ".mm()

        for ((index, rank) in ranks.withIndex()) {
            val name = rank.getData().name
            val rankComponent = "<p>$name</p>"
                .mm()
                .clickEvent(ClickEvent.suggestCommand("/pm rank $name "))

            component = component.append(rankComponent)

            if (index < ranks.size - 1)
                component = component.append(", ".mm())
        }

        sender.sendMessage(component)
    }
}