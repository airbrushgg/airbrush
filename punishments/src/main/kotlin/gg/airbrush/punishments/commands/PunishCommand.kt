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
import gg.airbrush.sdk.lib.Placeholder
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.sdk.lib.parsePlaceholders
import gg.airbrush.server.lib.mm
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
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
	private val confirmArg = ArgumentType.String("confirm")
	private val typeArg = ArgumentType.String("type").setSuggestionCallback { _, context, suggestions ->
		punishmentConfig.punishments.keys.forEach { suggestions.addEntry(SuggestionEntry(it)) }
	}
    
	init {
		setCondition { sender, _ ->
			sender.hasPermission("core.staff")
		}

		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<error>/punish <player>".mm())
		}

		addSyntax(this::punish, typeArg, confirmArg)
		addSyntax(this::punish, typeArg)
		addSyntax(this::baseCommand)
	}

	private fun getActivePunishmentWarning(player: OfflinePlayer): Component? {
		val activePunishment = SDK.punishments.list(player.uniqueId).find {it.data.active}
		if(activePunishment === null) return null

		val translation = Translations.getString("punishments.hasActivePunishment")
		val placeholders = listOf(
			Placeholder("%id%", activePunishment.data.id),
			Placeholder("%player%", player.username),
			Placeholder("%reason%", activePunishment.getReasonString()),
		)

		return translation.parsePlaceholders(placeholders).mm()
	}
	private fun punish(sender: CommandSender, context: CommandContext) = runBlocking {
		val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
		val confirmArg = context.get(confirmArg)
		val punishmentShort = context.get(typeArg)

		val punishable = canPunish(offlinePlayer.uniqueId)
		if(!punishable) {
			sender.sendMessage("<error>You cannot punish this person!".mm())
			return@runBlocking
		}

		val activePunishmentWarning = getActivePunishmentWarning(offlinePlayer)
		if(activePunishmentWarning !== null) {
			sender.sendMessage(activePunishmentWarning)
			return@runBlocking
		}

		val punishmentInfo = punishmentConfig.punishments[punishmentShort.lowercase()]

		if(punishmentInfo == null) {
			sender.sendMessage("<error>Invalid punishment type".mm())
			return@runBlocking
		}

		val punishmentType = PunishmentTypes.valueOf(punishmentInfo.action.uppercase())

		fun getPunishmentDuration(): String {
			if(punishmentInfo.duration === null) return "FOREVER"

			val punishmentInstances = SDK.punishments.list(offlinePlayer.uniqueId)
				.filter { punishmentShort.equals(it.data.reason, true) }

			val duration = punishmentInfo.duration[punishmentInstances.size]
			if(duration.equals("FOREVER", true)) return "FOREVER"

			return duration
		}

		if(confirmArg !== null) {
			if (confirmArg != "confirm") {
			    sender.sendMessage("<error>Cancelled punishment.".mm())
				if(sender is Player) sender.closeInventory()
			    return@runBlocking
			}

			try {
				Punishment(
					moderator =  if(sender is Player) User(sender.uuid, sender.username) else User(nilUUID, "Console"),
					player = User(offlinePlayer.uniqueId, offlinePlayer.username),
					reason = punishmentShort.uppercase(),
					type = punishmentType,
					duration = getPunishmentDuration()
				).handle()
			} catch (e: Exception) {
				sender.sendMessage("<error>Failed to punish player.\n<error>${e.message}".mm())
			}

			return@runBlocking
		}

		val pages = mutableListOf<Component>()
		val book = Book.builder().author(Component.empty()).title(Component.empty())

		val placeholders = listOf(
			Placeholder("%player%", offlinePlayer.username),
			Placeholder("%reason%", punishmentInfo.getReasonString()),
			Placeholder("%type%", punishmentType.name),
			Placeholder("%short%", punishmentShort),
		)

		val pageText = Translations.getString("punishments.confirm")
			.parsePlaceholders(placeholders)
			.trimIndent()
		pages.add(pageText.mm())

		sender.openBook(book.pages(pages))
	}
	private fun baseCommand(sender: CommandSender, context: CommandContext) = runBlocking {
		val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
		val punishable = canPunish(offlinePlayer.uniqueId)

		if (!punishable) {
			sender.sendMessage("<error>You cannot punish this person!".mm())
			return@runBlocking
		}

		val activePunishmentWarning = getActivePunishmentWarning(offlinePlayer)
		if(activePunishmentWarning !== null) {
			sender.sendMessage(activePunishmentWarning)
			return@runBlocking
		}

		val punishmentsList = punishmentConfig.punishments.map { (key, value) ->
			"- <click:run_command:/punish ${offlinePlayer.username} $key>${value.shortReason}</click>"
		}

		val pages = mutableListOf<Component>()
		val book = Book.builder().author(Component.empty()).title(Component.empty())

		val placeholders = listOf(
			Placeholder("%player%", offlinePlayer.username)
		)

		val punishmentChunks = punishmentsList.chunked(8)
		punishmentChunks.forEachIndexed { index, chunk ->
			val punishments = chunk.joinToString("<br>")
			val newPlaceholders = listOf(
				*placeholders.toTypedArray(),
				Placeholder("%current_page%", (index + 1).toString()),
				Placeholder("%punishments%", punishments)
			)
			val pageText = Translations.getString("punishments.punishing")
				.parsePlaceholders(newPlaceholders)
				.trimIndent()
			pages.add(pageText.mm())
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