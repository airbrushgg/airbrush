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