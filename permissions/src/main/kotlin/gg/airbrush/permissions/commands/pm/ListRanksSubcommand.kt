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