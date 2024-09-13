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

import gg.airbrush.discord.bot
import gg.airbrush.punishments.Punishment
import gg.airbrush.punishments.enums.PunishmentShorts
import gg.airbrush.punishments.PunishmentsConfig
import gg.airbrush.punishments.arguments.OfflinePlayerArgument
import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.punishments.lib.OfflinePlayer
import gg.airbrush.punishments.punishmentConfig
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.CommandSyntax
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import java.util.*
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import java.awt.Color

var nilUUID = UUID(0, 0)

// this might be messy, feel free to improve upon it
fun convertDate(input: String): Int {
	val regex = Regex("(\\d+)(\\D+)")
	val matchResult = regex.find(input)
	val (numericValue, timeUnit) = matchResult?.destructured ?: throw IllegalArgumentException("Invalid input format")

	val timeUnits: Map<String, Int> = mapOf(
		"h" to 60 * 60,
		"d" to 24 * 60 * 60,
		"w" to 7 * 24 * 60 * 60
	)

	val secondValue: Int =
		timeUnits[timeUnit] ?: throw IllegalArgumentException("Invalid time unit specified")

	return numericValue.toInt() * secondValue
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
	private val typeArg = ArgumentType.Enum("type", PunishmentShorts::class.java)
		.setFormat(ArgumentEnum.Format.LOWER_CASED)
    
	init {
		setCondition { sender, _ ->
			sender.hasPermission("core.punish")
		}

		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<error>/punish <player> <reason>".mm())
		}

		addSyntax(this::apply, typeArg, notesArg)
		addSyntax(this::apply, typeArg)
	}

	private fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
		val moderator = if(sender is Player) sender.uuid else nilUUID

		val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
		val notes = context.get(notesArg)

		val offenderRank = SDK.players.get(offlinePlayer.uniqueId).getRank()

		when(offenderRank.getData().name.lowercase()) {
			"mod",
			"admin" -> {
				sender.sendMessage("<error>You cannot punish this person!".mm())
				return@runBlocking
			}
		}

		val activePunishment = SDK.punishments.list(offlinePlayer.uniqueId).find {it.data.active}
		if(activePunishment !== null) {
			sender.sendMessage("<error>This player already has an active punishment!".mm())
			return@runBlocking
		}

		val type = context.get<PunishmentShorts>("type")

		val punishmentInfo = (PunishmentsConfig::class.memberProperties
			.firstOrNull {
				it.name == type.name.lowercase()
			}
			?.takeIf { it.returnType.isSubtypeOf(Punishment::class.starProjectedType) }
			?.get(punishmentConfig) as? Punishment)

		if(punishmentInfo == null) {
			sender.sendMessage("<error>Invalid punishment type".mm())
			return@runBlocking
		}

		val punishmentType = PunishmentTypes.valueOf(punishmentInfo.action.uppercase())
		val punishmentNotes = if(notes !== null) notes.joinToString(" ") { it } else ""

		val punishment = SDK.punishments.create(
			moderator = moderator,
			player = offlinePlayer.uniqueId,
			reason = punishmentInfo.reason,
			type = punishmentType.ordinal,
			// TODO: Make duration non-null in the SDK.
			duration = if(punishmentInfo.duration !== null)
				convertDate(punishmentInfo.duration)
			else 999999999,
			notes = punishmentNotes,
		)


		sender.sendMessage("<p>Issued punishment. Type: <s>${punishmentType.name}</s>.".mm())

		val moderatorName = if(sender is Player) sender.username else "Console"
		Audiences.players { p -> p.hasPermission("core.staff") }
			.sendMessage(Translations.translate("core.punish.punishment", moderatorName, offlinePlayer.username, punishmentInfo.action).mm())

		// Make this a variable in a config
		val discordLogChannel = bot.getTextChannelById("1162903708108071037")
			?: throw Exception("Failed to find #game-logs channel")

		val logEmbed = EmbedBuilder().setTitle("${offlinePlayer.username} was ${punishmentType.name.lowercase().toPluralForm()}")
			.setColor(Color.decode("#ff6e6e"))
			.setThumbnail("https://crafatar.com/renders/head/${offlinePlayer.uniqueId}")
			.addField(MessageEmbed.Field("Reason", type.name.lowercase(), true))
			.addField(MessageEmbed.Field("Moderator", moderatorName, true))
			.addField(MessageEmbed.Field("Punishment ID", punishment.id, false))
			.setFooter("Environment: ${if(SDK.isDev) "Development" else "Production"}")

		if(punishmentNotes.isNotEmpty()) {
			logEmbed.addField(MessageEmbed.Field("Notes", punishmentNotes, true))
		}

		discordLogChannel.sendMessageEmbeds(logEmbed.build()).queue()

		val player = MinecraftServer.getConnectionManager().onlinePlayers.firstOrNull {
			it.uuid == offlinePlayer.uniqueId
		} ?: return@runBlocking

		when(punishmentType) {
			PunishmentTypes.BAN,
			PunishmentTypes.KICK -> {
				player.kick(punishmentInfo.reason)
			}
			else -> return@runBlocking
		}
	}

	override fun addSyntax(
		executor: CommandExecutor,
		vararg args: Argument<*>
	): MutableCollection<CommandSyntax> {
		return super.addSyntax(executor, OfflinePlayerArgument("offline-player"), *args)
	}
}