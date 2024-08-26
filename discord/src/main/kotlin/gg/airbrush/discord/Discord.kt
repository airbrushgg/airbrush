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