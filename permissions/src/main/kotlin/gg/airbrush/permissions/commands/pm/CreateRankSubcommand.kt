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