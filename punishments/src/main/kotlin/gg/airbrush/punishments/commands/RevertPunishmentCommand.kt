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

import gg.airbrush.discord.discordConfig
import gg.airbrush.discord.useBot
import gg.airbrush.punishments.lib.getReasonInfo
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.punishments.AirbrushPunishment
import gg.airbrush.sdk.classes.punishments.RevertedData
import gg.airbrush.sdk.lib.Input
import gg.airbrush.sdk.lib.fetchInput
import gg.airbrush.sdk.lib.parsePlaceholders
import gg.airbrush.sdk.lib.Placeholder
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.utils.mojang.MojangUtils
import java.awt.Color
import java.util.*

class RevertPunishmentCommand : Command("revertpun") {
	init {
		setCondition { sender, _ ->
			sender.hasPermission("core.admin")
		}

		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<s>Invalid usage.".mm())
		}

		addSyntax(this::apply, ArgumentType.UUID("punishment"))
	}

	private fun getName(punishment: AirbrushPunishment): String {
		val nameData = MojangUtils.fromUuid(punishment.data.player)
		var playerName = "Not found"
		if(nameData !== null && nameData.isJsonObject) {
			playerName = nameData.get("name").asString
		}
		return playerName
	}

	private fun sendLog(punishment: AirbrushPunishment, moderator: String) {
		val player = getName(punishment)

		val translation = Translations.getString("punishments.reversion")
		val (shortReason) = getReasonInfo(punishment.data.reason)
		val placeholders = listOf(
			Placeholder("%moderator%", moderator),
			Placeholder("%player%", player),
			Placeholder("%reason%", shortReason),
			Placeholder("%id%", punishment.data.id)
		)
		val message = translation.parsePlaceholders(placeholders).mm()

		Audiences.players { p -> p.hasPermission("core.staff") }.sendMessage(message)
	}

	private fun handleRevert(punishment: AirbrushPunishment, sender: Player, reason: String) {
		punishment.setReverted(
			RevertedData(
				revertedBy = sender.uuid.toString(),
				revertedReason = reason
			)
		)

		sender.sendMessage("<success>Successfully reverted punishment.".mm())

		this.sendLog(punishment, sender.username)

		useBot {
			val discordLogChannel = it.getTextChannelById(discordConfig.channels.log.toLong())
				?: throw Exception("Failed to find logs channel")

			val victim = getName(punishment)

			val logEmbed = EmbedBuilder().setTitle("Punishment for $victim reverted")
				.setColor(Color.decode("#ff6e6e"))
				.setThumbnail("https://crafatar.com/renders/head/${punishment.getPlayer()}")
				.addField(MessageEmbed.Field("Reverted by", sender.username, false))
				.addField(MessageEmbed.Field("Punishment ID", punishment.data.id, false))
				.setFooter("Environment: ${if(SDK.isDev) "Development" else "Production"}")
				.build()

			discordLogChannel.sendMessageEmbeds(logEmbed).queue()
		}
	}


	private fun apply(sender: CommandSender, context: CommandContext) {
		if(sender !is Player) return

		val punishmentId = context.get<UUID>("punishment")

		val punishmentExists = SDK.punishments.exists(punishmentId)

		if(!punishmentExists) {
			sender.sendMessage("<error>A punishment with that ID does not exist!".mm())
			return
		}

		val punishment = SDK.punishments.get(punishmentId)

		if(!punishment.data.active) {
			sender.sendMessage("<error>That punishment is not active!".mm())
			return
		}

		sender.sendMessage("<s>Please enter a reason for the revert:".mm())

		val input: Input = fetchInput(sender) {
			handleRevert(punishment, sender, it)
		}

		input.prompt()
	}
}
