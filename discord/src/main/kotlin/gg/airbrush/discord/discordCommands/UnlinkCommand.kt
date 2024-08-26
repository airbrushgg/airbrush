package gg.airbrush.discord.discordCommands

import gg.airbrush.sdk.SDK
import me.santio.coffee.common.annotations.Command
import me.santio.coffee.jda.annotations.Description
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.*

@Command
@Description("Unlink your Minecraft account")
class UnlinkCommand {
	private fun SlashCommandInteractionEvent.answer(msg: String) {
		return this.hook.sendMessage(msg).queue()
	}

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

