package gg.airbrush.permissions.commands.pm

import gg.airbrush.permissions.commands.arguments.rankCache
import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import java.util.UUID

object CreateRankSubcommand : Command("createRank") {
    init {
        addSyntax(this::execute, ArgumentType.String("name"))
    }

    private fun execute(sender: CommandSender, context: CommandContext) {
        val name = context.get<String>("name")

        if (SDK.ranks.exists(name)) {
            sender.sendMessage("<s>The rank <p>$name</p> already exists.".mm())
            return
        }

        val rankData = SDK.ranks.create(name)
        val rankId = UUID.fromString(rankData.id)
        rankCache.add(SDK.ranks.get(rankId))
        sender.sendMessage("<s>Created rank <p>$name</p>.".mm())
    }
}