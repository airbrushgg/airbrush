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

package gg.airbrush.discord.discordCommands

import gg.airbrush.discord.lib.answer
import gg.airbrush.sdk.SDK
import me.santio.coffee.common.annotations.Command
import me.santio.coffee.jda.annotations.Description
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.*

@Command
@Description("Unlink your Minecraft account")
class UnlinkCommand {
	fun main(e: SlashCommandInteractionEvent) {
		e.deferReply(true).queue()

		val playerData = SDK.players.getByDiscordID(e.user.id)
		if(playerData == null) {
			e.answer("Your account is not currently linked!")
			return
		}

		val sdkPlayer = SDK.players.get(UUID.fromString(playerData.uuid))
		sdkPlayer.wipeDiscordId()

		e.answer("Successfully unlinked your Discord account")
	}
}

