package gg.airbrush.discord.discordCommands

import me.santio.coffee.common.annotations.Command
import me.santio.coffee.jda.annotations.Description
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Command
@Description("Get a free cookie")
class CookieCommand {
	fun main(e: SlashCommandInteractionEvent) {
		e.reply("Here you go! 🍪").queue()
	}
}

