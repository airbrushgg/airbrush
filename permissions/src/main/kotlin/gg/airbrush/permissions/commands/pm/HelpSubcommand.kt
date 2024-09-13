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

import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

object HelpSubcommand : Command("help"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("""<s>
            |<p>/pm</p> Help
            |
            |<p>/pm listRanks</p> List all ranks.
            |<p>/pm createRank (name)</p> Create a rank.
            |
            |<p>/pm rank (rank) delete</p> Delete a rank.
            |<p>/pm rank (rank) rename (new name)</p> Rename a rank.
            |
            |<p>/pm rank (rank) parent get</p> Get the parent of a rank.
            |<p>/pm rank (rank) parent set (parent name)</p> Set the parent of a rank.
            |
            |<p>/pm rank (rank) prefix get</p> Get the prefix of a rank.
            |<p>/pm rank (rank) prefix set (prefix)</p> Set the prefix of a rank.
            |
            |<p>/pm rank (rank) permission add (key) [value: NBT compound]</p> Add a permission (with optional NBT) to a rank.
            |<p>/pm rank (rank) permission remove (key)</p> Remove a permission from a rank.
            |<p>/pm rank (rank) permission get (key)</p> Get the permission of a rank.
            |<p>/pm rank (rank) permission list</p> List all permissions of a rank.
            |
            |<p>/pm player (name / uuid) rank get</p> Get the rank of a player.
            |<p>/pm player (name / uuid) rank set (rank)</p> Set the rank of a player.
        """.trimMargin().mm())
    }
}