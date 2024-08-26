package gg.airbrush.discord.data

data class DiscordConfig(
    val botToken: String,
    val channel: String,
    val donor: String,
    val superdonor: String,
    val join: MessageContent,
    val leave: MessageContent,
    val chat: MessageContent,
    val ingame: MessageContent,
	val linkRequest: String
)

data class MessageContent(
    val content: String
)
