/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2024 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.punishments.commands

import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.punishments.lib.*
import gg.airbrush.sdk.SDK
import gg.airbrush.server.arguments.OfflinePlayer
import gg.airbrush.server.arguments.OfflinePlayerArgument
import gg.airbrush.server.lib.mm
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.CommandSyntax
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class BanCommand : Command("ban") {
    private val reasonArg = ArgumentType.StringArray("reason")
    private val durationArg = ArgumentType.String("duration")

    init {
        setCondition { sender, _ ->
            sender.hasPermission("punishments.ban")
        }

        defaultExecutor = CommandExecutor { sender, _ ->
            sender.sendMessage("<error>/ban <player> [reason] [duration] [notes...]".mm())
        }

        addSyntax(this::apply, durationArg, reasonArg)
        addSyntax(this::apply, reasonArg)
        addSyntax(this::apply)
    }

    private fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
        val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
        val reason = context.get(reasonArg)
        val duration = context.get<String?>("duration") ?: "FOREVER"

        val punishable = canPunish(offlinePlayer.uniqueId)
        if(!punishable) {
            sender.sendMessage("<error>You cannot punish this person!".mm())
            return@runBlocking
        }

        val activePunishment = SDK.punishments.list(offlinePlayer.uniqueId).find {it.data.active}
        if(activePunishment !== null) {
            sender.sendMessage("<error>This player already has an active punishment!".mm())
            return@runBlocking
        }

        try {
            Punishment(
                moderator =  if(sender is Player) User(sender.uuid, sender.username) else User(nilUUID, "Console"),
                player = User(offlinePlayer.uniqueId, offlinePlayer.username),
                reason = reason.joinToString(" "),
                type = PunishmentTypes.BAN,
                duration = duration,
                notes = ""
            ).handle()
        } catch (e: Exception) {
            sender.sendMessage("<error>Failed to ban player.\n<error>${e.message}".mm())
            return@runBlocking
        }
    }

    override fun addSyntax(
        executor: CommandExecutor,
        vararg args: Argument<*>
    ): MutableCollection<CommandSyntax> {
        return super.addSyntax(executor, OfflinePlayerArgument("offline-player"), *args)
    }
}