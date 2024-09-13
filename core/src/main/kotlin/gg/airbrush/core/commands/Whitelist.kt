

/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2023 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.core.commands

import gg.airbrush.core.commands.history.timestampToRelativeTime
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.server.lib.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import java.util.*

class Whitelist : Command("whitelist") {
    init {
        defaultExecutor = CommandExecutor { sender, _ ->
            sender.sendMessage("<error>Usage: /whitelist <add/remove/list>".mm())
        }
        setCondition { sender, _ -> sender.hasPermission("core.whitelist") }

        addSubcommand(ListSubcommand())
        addSubcommand(AddSubcommand())
        addSubcommand(RemoveSubcommand())
    }

    private class ListSubcommand : Command("list"), CommandExecutor {
        init {
            defaultExecutor = this
        }

        override fun apply(sender: CommandSender, context: CommandContext) {
            val entries = SDK.whitelist.list()
            val message = Component.join(
                JoinConfiguration.newlines(),
                entries.map { entry ->
                    val time = timestampToRelativeTime(entry.addedAt)
                    val playerUUID = UUID.fromString(entry.uuid)
                    val player = PlayerUtils.getName(playerUUID)
                    "<p><s>$player</s> added <s>$time".mm()
                }.toList()
            )
            sender.sendMessage(message)
        }
    }

    private class AddSubcommand : Command("add"), CommandExecutor {
        private val playerArgument = ArgumentType.String("player")
        init {
            defaultExecutor = this
            addSyntax(this, playerArgument)
        }

        override fun apply(sender: CommandSender, context: CommandContext) {
            if (!context.has(playerArgument)) {
                sender.sendMessage("<error>Usage: /whitelist add <player>".mm())
                return
            }

            val selectedPlayer = context.get(playerArgument)
            SDK.whitelist.add(PlayerUtils.getUUID(selectedPlayer))
            sender.sendMessage("<success>Added $selectedPlayer to the whitelist".mm())
        }
    }

    private class RemoveSubcommand : Command("remove"), CommandExecutor {
        private val playerArgument = ArgumentType.String("player")

        init {
            defaultExecutor = this
            addSyntax(this, playerArgument)
        }

        override fun apply(sender: CommandSender, context: CommandContext) {
            if (!context.has(playerArgument)) {
                sender.sendMessage("<error>Usage: /whitelist remove <player>".mm())
                return
            }

            val selectedPlayer = context.get(playerArgument)
            SDK.whitelist.remove(PlayerUtils.getUUID(selectedPlayer))
            sender.sendMessage("<success>Removed $selectedPlayer from the whitelist".mm())
        }
    }
}