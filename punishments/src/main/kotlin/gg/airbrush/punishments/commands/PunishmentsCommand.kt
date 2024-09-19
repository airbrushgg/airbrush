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

import gg.airbrush.punishments.arguments.OfflinePlayerArgument
import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.punishments.lib.OfflinePlayer
import gg.airbrush.punishments.punishmentConfig
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.punishments.AirbrushPunishment
import gg.airbrush.sdk.lib.replaceTabs
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

fun AirbrushPunishment.getReasonString(): String {
	var text = ""
	var reason = this.data.reason

	if(reason.lowercase() in punishmentConfig.punishments.keys) {
		val punishmentInfo = punishmentConfig.punishments[reason.lowercase()]!!
		reason = punishmentInfo.shortReason
	}

	if(this.data.active) {
		text = if(this.data.type == PunishmentTypes.BAN.ordinal) "<red>$reason</red>"
		else "<blue>$reason</blue>"
	}

	if (this.data.reverted != null) {
		text = "(R) $reason"
	}

	if(text.isEmpty()) text = reason

	return text
}

class PunishmentsCommand : Command("punishments") {
	init {
		setCondition { sender, _ ->
			sender.hasPermission("core.staff")
		}

		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<error>Invalid usage.".mm())
		}

		addSyntax(this::apply)
	}

	private fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
		val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
		val playerPunishments = SDK.punishments.list(offlinePlayer.uniqueId)

		val pages = mutableListOf<Component>()
		val book = Book.builder().author(Component.empty()).title(Component.empty())

		if(playerPunishments.isEmpty()) {
			sender.sendMessage("<error>${offlinePlayer.username} does not have any punishments!".mm())
			return@runBlocking
		}

		val allPunishments = playerPunishments
			.sortedWith(compareByDescending<AirbrushPunishment> { it.data.active }
			.thenBy { it.data.type == PunishmentTypes.BAN.ordinal })

		val punishments = allPunishments.joinToString("\n") {
			val type = PunishmentTypes.entries[it.data.type]
			val text = it.getReasonString()
			"<hover:show_text:'<p>View ${type.name.lowercase()} information</p>'><click:run_command:/punishment ${it.data.id}>$text</click></hover>"
		}

		val page = """
			<p>Username:</p>
			${offlinePlayer.username}
			
			<p>Punishments:</p>
			$punishments
		""".replaceTabs()

		pages.add(page.mm())

		sender.openBook(book.pages(pages))
	}

	override fun addSyntax(
		executor: CommandExecutor,
		vararg args: Argument<*>
	): MutableCollection<CommandSyntax> {
		return super.addSyntax(executor, OfflinePlayerArgument("offline-player"), *args)
	}
}