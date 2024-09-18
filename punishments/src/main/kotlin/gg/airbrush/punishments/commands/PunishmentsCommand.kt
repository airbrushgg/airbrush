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
import gg.airbrush.punishments.lib.OfflinePlayer
import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.CommandSyntax
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.utils.mojang.MojangUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun timestampToRelativeTime(timestamp: Long): String {
	val currentInstant = Instant.now()
	val targetInstant = Instant.ofEpochMilli(timestamp)

	val zoneId = ZoneId.systemDefault()
	val currentDateTime = LocalDateTime.ofInstant(currentInstant, zoneId)
	val targetDateTime = LocalDateTime.ofInstant(targetInstant, zoneId)

	val diff = ChronoUnit.SECONDS.between(targetDateTime, currentDateTime)

	fun formatAgo(diff: Long, secondsInUnit: Long, unit: String): String {
		val time = diff / secondsInUnit
		return "$time ${if (time > 1) unit + "s" else unit} ago"
	}

	return when {
		diff < 60 -> "Just now"
		diff < 3600 -> formatAgo(diff, 60, "minute")
		diff < 86400 -> formatAgo(diff, 3600, "hour")
		diff < 604800 -> formatAgo(diff, 86400, "day")
		else -> formatAgo(diff, 604800, "week")
	}
}


class PunishmentsCommand : Command("punishments") {
	init {
		setCondition { sender, _ ->
			sender.hasPermission("core.staff")
		}

		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<s>Invalid usage.".mm())
		}

		addSyntax(this::apply)
	}

	private fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
		val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
		val playerPunishments = SDK.punishments.list(offlinePlayer.uniqueId)

		val pages = mutableListOf<Component>()
		val book = Book
			.builder()
			.author("".mm())
			.title("".mm())

		if(playerPunishments.isEmpty()) {
			sender.sendMessage("<error>${offlinePlayer.username} does not have any punishments!".mm())
			return@runBlocking
		}

		playerPunishments.forEach {
			val type = PunishmentTypes.entries.find { t ->
				t.ordinal == it.data.type
			}

			if(type == null) {
				sender.sendMessage("punishment type was null. (${it.data.type})")
				return@forEach
			}

			val action = if(it.data.active) "<red>${type.name}</red>" else "<p>${type.name}</p>"
			val occurred = timestampToRelativeTime(it.getCreatedAt().toEpochMilli())

			val nameData = MojangUtils.fromUuid(it.data.moderator)
			var moderatorName = "Console"

			if(nameData !== null && nameData.isJsonObject) {
				moderatorName = nameData.get("name").asString
			}

			val string = """
				Action: $action
				Occurred: <p>$occurred</p>
				From: <p>$moderatorName</p>
				Reason: <hover:show_text:'${it.data.reason}'><p>(hover)</hover></p>
				${if(it.data.notes !== null) "Notes: <p>${it.data.notes}</p>" else ""}
			
				<p>   ${if(it.data.active) ">>> <b> <click:run_command:/revertpun ${it.data.id}>REVERT</click> </b> <<<" else ""}
			""".trimIndent()

			pages.add(string.mm())
		}

		sender.openBook(book.pages(pages))
	}

	override fun addSyntax(
		executor: CommandExecutor,
		vararg args: Argument<*>
	): MutableCollection<CommandSyntax> {
		return super.addSyntax(executor, OfflinePlayerArgument("offline-player"), *args)
	}
}