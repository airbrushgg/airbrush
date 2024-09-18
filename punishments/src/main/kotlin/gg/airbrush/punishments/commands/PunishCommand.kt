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
import gg.airbrush.punishments.punishmentConfig
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.Placeholder
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.sdk.lib.parsePlaceholders
import gg.airbrush.server.lib.mm
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
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

fun getNumericValue(input: String): Pair<Int, String> {
	val regex = Regex("(\\d+)(\\D+)")
	val matchResult = regex.find(input)
	val (numericValue, timeUnit) = matchResult?.destructured ?: throw IllegalArgumentException("Invalid input format")
	return Pair(numericValue.toInt(), timeUnit)
}

// this might be messy, feel free to improve upon it
fun convertDate(input: String): Int {
	val (numericValue, timeUnit) = getNumericValue(input)

	val timeUnits: Map<String, Int> = mapOf(
		"h" to 60 * 60,
		"d" to 24 * 60 * 60,
		"w" to 7 * 24 * 60 * 60
	)

	val secondValue: Int =
		timeUnits[timeUnit] ?: throw IllegalArgumentException("Invalid time unit specified")

	return numericValue * secondValue
}

fun String.toPluralForm(): String {
	return when {
		endsWith("n") -> this + "ned"
		endsWith("e") -> this + "d"
		else -> this + "ed"
	}
}

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

	private fun canPunish(uuid: UUID): Boolean {
		val playerExists = SDK.players.exists(uuid)
		if(!playerExists) return true
		val offenderRank = SDK.players.get(uuid).getRank()
		return offenderRank.getData().permissions.find { it.key == "core.staff" } !== null
	}

	private fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
		val moderator = if(sender is Player) sender.uuid else nilUUID

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

		val punishment = SDK.punishments.create(
			moderator = moderator,
			player = offlinePlayer.uniqueId,
			reason = punishmentShort.uppercase(),
			type = punishmentType.ordinal,
			// TODO: Make duration non-null in the SDK.
			duration = if(punishmentInfo.duration !== null)
				convertDate(punishmentInfo.duration)
			else 999999999,
			notes = punishmentNotes,
		)

		sender.sendMessage("<p>Punishment issued. Type: <s>${punishmentType.name}</s>.".mm())

		val moderatorName = if(sender is Player) sender.username else "Console"
		val prettyType = punishmentType.name.lowercase().toPluralForm()
		var duration = "FOREVER"

		if(punishmentInfo.duration !== null) {
			val (numericValue, timeUnit) = getNumericValue(punishmentInfo.duration)

			var unit = when(timeUnit) {
				"h" -> "hour"
				"d" -> "day"
				"w" -> "week"
				else -> throw Exception("Invalid time unit specified")
			}
			if(numericValue > 1) unit += "s"

			duration = "$numericValue $unit"
		}

		val logPlaceholders = listOf(
			Placeholder("%moderator%", moderatorName),
			Placeholder("%player%", offlinePlayer.username),
			Placeholder("%action%", punishmentInfo.action),
			Placeholder("%type%", prettyType),
			Placeholder("%short_reason%", punishmentInfo.shortReason),
			Placeholder("%long_reason%", punishmentInfo.longReason),
			Placeholder("%duration%", duration),
		)
		logPlaceholders.forEach { MinecraftServer.LOGGER.info("[Placeholder] key = ${it.string}, value = ${it.replacement}") }

		val logString = Translations.getString("punishments.punishment").parsePlaceholders(logPlaceholders).trimIndent()
		Audiences.players { p -> p.hasPermission("core.staff") }.sendMessage(logString.mm())

		// Make this a variable in a config
		/*val discordLogChannel = bot.getTextChannelById("1162903708108071037")
			?: throw Exception("Failed to find #game-logs channel")

		val logEmbed = EmbedBuilder().setTitle("${offlinePlayer.username} was $prettyType")
			.setColor(Color.decode("#ff6e6e"))
			.setThumbnail("https://crafatar.com/renders/head/${offlinePlayer.uniqueId}")
			.addField(MessageEmbed.Field("Reason", type.name.lowercase(), true))
			.addField(MessageEmbed.Field("Moderator", moderatorName, true))
			.addField(MessageEmbed.Field("Punishment ID", punishment.id, false))
			.setFooter("Environment: ${if(SDK.isDev) "Development" else "Production"}")

		if(punishmentNotes.isNotEmpty()) {
			logEmbed.addField(MessageEmbed.Field("Notes", punishmentNotes, true))
		}

		discordLogChannel.sendMessageEmbeds(logEmbed.build()).queue()*/

		val player = MinecraftServer.getConnectionManager().onlinePlayers.firstOrNull {
			it.uuid == offlinePlayer.uniqueId
		} ?: return@runBlocking

		when(punishmentType) {
			PunishmentTypes.BAN,
			PunishmentTypes.KICK -> {
				player.kick(punishmentInfo.getDisconnectMessage())
			}
			PunishmentTypes.MUTE -> {
				val mutedMsg = Translations.getString("punishments.playerMuted").parsePlaceholders(logPlaceholders).trimIndent()
				player.sendMessage(mutedMsg.mm())
			}
		}
	}

	override fun addSyntax(
		executor: CommandExecutor,
		vararg args: Argument<*>
	): MutableCollection<CommandSyntax> {
		return super.addSyntax(executor, OfflinePlayerArgument("offline-player"), *args)
	}
}