package gg.airbrush.discord.lib

import gg.airbrush.discord.bot

object Logging {
	fun sendLog(msg: String) {
		val channel = bot.getTextChannelById("1162903708108071037")
			?: throw Exception("Failed to find logging channel!")

		return channel.sendMessage(msg).queue()
	}
}