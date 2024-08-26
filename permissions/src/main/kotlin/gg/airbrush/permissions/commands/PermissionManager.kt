package gg.airbrush.permissions.commands

import gg.airbrush.permissions.commands.pm.*
import gg.airbrush.server.lib.mm
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor

class PermissionManager : Command("permissionmanager", "pm") {
    init {
        setCondition { sender, _ ->
            sender.hasPermission("permissions.pm")
        }

        defaultExecutor = CommandExecutor { sender, _ ->
            sender.sendMessage("<s>Invalid usage. Please use <p>/pm help</p>.".mm())
        }

        addSubcommand(RankSubcommand)
        addSubcommand(ListRanksSubcommand)
        addSubcommand(CreateRankSubcommand)
        addSubcommand(PlayerSubcommand)
        addSubcommand(HelpSubcommand)
    }
}