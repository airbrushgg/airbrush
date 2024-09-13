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

import gg.airbrush.permissions.commands.arguments.OfflinePlayerArgument
import gg.airbrush.permissions.commands.arguments.RankArgument
import gg.airbrush.permissions.lib.OfflinePlayer
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.ranks.AirbrushRank
import gg.airbrush.server.lib.mm
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.CommandSyntax
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType.*

object PlayerSubcommand : Command("player") {
    init {
        val set = Literal("set")
        val get = Literal("get")

        val rank = Literal("rank")
        addSyntax(this::getRank, rank, get)
        addSyntax(this::setRank, rank, set, RankArgument("new-rank"))
    }

    private fun getRank(executor: CommandSender, context: CommandContext) = runBlocking {
        val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()

        if (!SDK.players.exists(offlinePlayer.uniqueId))
            SDK.players.create(offlinePlayer.uniqueId)

        val player = SDK.players.get(offlinePlayer.uniqueId)
        val rank = player.getRank()

        executor.sendMessage("<s><p>${offlinePlayer.username}</p> has the rank <p>${rank.getData().name}</p>.".mm())
    }

    private fun setRank(executor: CommandSender, context: CommandContext) = runBlocking {
        val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()

        if (!SDK.players.exists(offlinePlayer.uniqueId))
            SDK.players.create(offlinePlayer.uniqueId)

        val player = SDK.players.get(offlinePlayer.uniqueId)
        val rank = context.get<AirbrushRank>("new-rank")
        val name = rank.getData().name

        player.setRank(name)
        executor.sendMessage("<s>Set the rank of <p>${offlinePlayer.username}</p> to <p>$name</p>.".mm())
    }

    override fun addSyntax(
        executor: CommandExecutor,
        vararg args: Argument<*>
    ): MutableCollection<CommandSyntax> {
        return super.addSyntax(executor, OfflinePlayerArgument("offline-player"), *args)
    }
}