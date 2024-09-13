

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

package gg.airbrush.discord

import gg.airbrush.discord.events.PlayerChat
import gg.airbrush.discord.events.RoleEvent
import me.santio.coffee.common.Coffee
import me.santio.coffee.jda.CoffeeJDA
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy

lateinit var bot: JDA

object Discord {
	fun load() {
		bot = JDABuilder.createDefault(
			discordConfig.botToken
		).enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
			.addEventListeners(RoleEvent, PlayerChat)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.build().awaitReady()

		// Attach command handler
		bot.updateCommands().complete()

		// Load all members
		bot.guilds.forEach {
			it.loadMembers()
		}

		Coffee.import(CoffeeJDA(bot))
		Coffee.brew("gg.airbrush.discord.discordCommands")
	}
}