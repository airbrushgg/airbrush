package gg.airbrush.core.events

import gg.airbrush.core.commands.admin.Lockdown
import gg.airbrush.core.filter.ChatFilter
import gg.airbrush.core.lib.ColorUtil
import gg.airbrush.discord.bot
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.ranks.RankData
import gg.airbrush.server.lib.mm
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.color.Color
import net.minestom.server.event.player.PlayerChatEvent

class PlayerChat {
    init {
        val eventHandler = MinecraftServer.getGlobalEventHandler()

        eventHandler.addListener(
            PlayerChatEvent::class.java
        ) { event: PlayerChatEvent -> execute(event) }
    }

    private fun execute(event: PlayerChatEvent) {
        if (ChatFilter.shouldBlock(event.player, event.message)) {
            event.isCancelled = true
	        // TODO: Make a global configuration of all the log channel IDs
	        val filterLogs = bot.getTextChannelById("1178153995588612226")
		        ?: throw Exception("Failed to find #filter-logs channel")

	        // TODO: Eventually get the filter rule they triggered? idk, that's an @apple thing
	        val logEmbed = EmbedBuilder().setTitle("${event.player.username} triggered the filter")
		        .setColor(java.awt.Color.decode("#ff6e6e"))
		        .addField(MessageEmbed.Field("Message", event.message, false))
		        .build()

	        filterLogs.sendMessageEmbeds(logEmbed).queue()
            return
        }

        if (Lockdown.isLockedDown()) {
            event.isCancelled = true
            return
        }

	    val punishments = SDK.punishments.list(event.player)
	    val activeMute = punishments.find { it.data.active }
	    if(activeMute !== null) {
		    event.player.sendMessage("<error>${activeMute.data.reason}".mm())
		    event.isCancelled = true
		    return
	    }

        val player = event.player
        val sdkPlayer = SDK.players.get(player.uuid)

        event.setChatFormat { _ ->
            val level = sdkPlayer.getLevel()
            val levelColor = ColorUtil.oscillateHSV(Color(0xff0000), Color(0xff00ff), level)
            val rankData = sdkPlayer.getRank().getData()

	        val pronounData = sdkPlayer.getData().pronouns
	        val pronouns = if(!pronounData.isNullOrEmpty()) pronounData else "None set"
	        val message = PlainTextComponentSerializer.plainText().serialize(event.message.mm())

            val hoverComponent = Component.text { builder ->
                if (player.hasPermission("airbrush.staff")) {
                    builder.append("<s>☆ <p>${player.username} <s>is a staff member.\n\n".mm())
                } else if (player.hasPermission("core.donor")) {
                    builder.append("<donator>☆ ${player.username}</donator> <s>is a donator.\n\n".mm())
                }

                val fullPrefix = getFullPrefix(rankData)
                if (fullPrefix.isNotEmpty()) {
                    builder.append(fullPrefix.mm()).appendSpace()
                }

                builder.append("<p>${player.username}\n".mm())
                builder.append("<s>Level: <${TextColor.color(levelColor).asHexString()}>[$level]\n".mm())
                builder.append("<s>Pronouns: $pronouns".mm())
            }

            val messageComponent = Component.text { builder ->
                builder.append("<${TextColor.color(levelColor).asHexString()}>[$level]".mm())
                if (rankData.prefix.isNotEmpty()) {
                    builder.appendSpace().append("<s>${rankData.prefix}".mm()
                        .appendSpace()
                        .append(Component.text(player.username)))
                } else builder.append("<p>${player.username}".mm())
                builder.appendSpace().append("<s>- <white>$message".mm())
            }

            messageComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent))
        }
    }

    private fun getFullPrefix(rank: RankData): String {
        // If the prefix belongs to a donator rank, just return the prefix as-is.
        if (rank.prefix.contains("☆"))
            return rank.prefix

        // If there is no prefix, we probably have a default rank.
        if (rank.prefix.isBlank())
            return ""

        // Otherwise, return the transformed prefix.
        return rank.prefix.replaceFirst(rank.name.first().toString(), rank.name.uppercase(), ignoreCase = true)
    }
}