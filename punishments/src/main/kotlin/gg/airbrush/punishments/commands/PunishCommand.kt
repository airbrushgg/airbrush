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

package gg.airbrush.punishments.commands

import gg.airbrush.punishments.arguments.OfflinePlayerArgument
import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.punishments.lib.*
import gg.airbrush.punishments.punishmentConfig
import gg.airbrush.sdk.SDK
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
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import java.util.*

var nilUUID = UUID(0, 0)

class PunishCommand : Command("punish") {
	private val notesArg = ArgumentType.StringArray("notes")
	private val typeArg = ArgumentType.String("type").setSuggestionCallback { _, context, suggestions ->
		punishmentConfig.punishments.keys.forEach { suggestions.addEntry(SuggestionEntry(it)) }
	}
    
	init {
		setCondition { sender, _ ->
			sender.hasPermission("core.staff")
		}

		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<error>/punish <player> <reason>".mm())
		}

		addSyntax(this::apply, typeArg, notesArg)
		addSyntax(this::apply, typeArg)
	}

	private fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
		val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
		val notes = context.get(notesArg)

		val punishable = canPunish(offlinePlayer.uniqueId)
/*		if(punishable) {
			sender.sendMessage("<error>You cannot punish this person!".mm())
			return@runBlocking
		}*/

		val activePunishment = SDK.punishments.list(offlinePlayer.uniqueId).find {it.data.active}
		if(activePunishment !== null) {
			sender.sendMessage("<error>This player already has an active punishment!".mm())
			return@runBlocking
		}

		val punishmentShort = context.get<String>("type")
		val punishmentInfo = punishmentConfig.punishments[punishmentShort.lowercase()]

		if(punishmentInfo == null) {
			sender.sendMessage("<error>Invalid punishment type".mm())
			return@runBlocking
		}

		val punishmentType = PunishmentTypes.valueOf(punishmentInfo.action.uppercase())
		val punishmentNotes = if(notes !== null) notes.joinToString(" ") { it } else ""

		Punishment(
			moderator =  if(sender is Player) User(sender.uuid, sender.username) else User(nilUUID, "Console"),
			player = User(offlinePlayer.uniqueId, offlinePlayer.username),
			reason = punishmentShort.uppercase(),
			type = punishmentType,
			duration = punishmentInfo.duration ?: "FOREVER",
			notes = punishmentNotes
		).handle()
	}

	override fun addSyntax(
		executor: CommandExecutor,
		vararg args: Argument<*>
	): MutableCollection<CommandSyntax> {
		return super.addSyntax(executor, OfflinePlayerArgument("offline-player"), *args)
	}
}