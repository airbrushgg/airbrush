

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

package gg.airbrush.core.events

import gg.airbrush.core.commands.admin.Lockdown
import gg.airbrush.core.filter.ChatFilter
import gg.airbrush.core.filter.FilterAction
import gg.airbrush.core.filter.FilterResult
import gg.airbrush.core.filter.chatFilterInstance
import gg.airbrush.core.lib.ColorUtil
import gg.airbrush.discord.bot
import gg.airbrush.sdk.SDK
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
        // NOTE: This is intentionally added to the global event handler.
        val eventHandler = MinecraftServer.getGlobalEventHandler()

        eventHandler.addListener(
            PlayerChatEvent::class.java
        ) { event: PlayerChatEvent -> execute(event) }
    }

    private fun execute(event: PlayerChatEvent) {
        if (Lockdown.isLockedDown()) {
            event.isCancelled = true
            return
        }

        val player = event.player
        val sdkPlayer = SDK.players.get(player.uuid)

        val punishments = SDK.punishments.list(player)
        val activeMute = punishments.find { it.data.active }
        if (activeMute !== null) {
            player.sendMessage("<error>${activeMute.data.reason}".mm())
            event.isCancelled = true
            return
        }

        val filterResult = chatFilterInstance.validateMessage(player, event.message)
        if (filterResult.action == FilterAction.BLOCK) {
            event.isCancelled = true

            // TODO(cal): This should be better.
            val filterLogs = bot.getTextChannelById(chatFilterInstance.logChannel)
                ?: throw Exception("Failed to find respective logs channel")

            val firstToken = filterResult.failedTokens.first()
            val formattedMessage = event.message
                .replace("`", "\\`")
                .replaceFirst(firstToken.value, "`${firstToken.value}`")

            // TODO: Eventually get the filter rule they triggered? idk, that's an @apple thing
            val logEmbed = EmbedBuilder().setTitle("${event.player.username} triggered the filter")
                .setColor(java.awt.Color.decode("#ff6e6e"))
                .addField(MessageEmbed.Field("Message", formattedMessage, false))
                .build()

            filterLogs.sendMessageEmbeds(logEmbed).queue()
            return
        }

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
                    builder.append("<donator>☆ ${player.username}</donator> <s>is a star.\n\n".mm())
                }

                if (rankData.prefix.isNotEmpty()) {
                    builder.append(rankData.prefix.mm()).appendSpace()
                }

                builder.append("<p>${player.username}\n".mm())
                builder.append("<s>Level: <${TextColor.color(levelColor).asHexString()}>[$level]\n".mm())
                builder.append("<s>Pronouns: $pronouns".mm())
            }

            val messageComponent = Component.text { builder ->
                builder.append("<${TextColor.color(levelColor).asHexString()}>[$level]".mm())
                builder.appendSpace()
                if (rankData.prefix.isNotEmpty()) {
                    builder.append("<s>${rankData.prefix} ${player.username}".mm())
                } else builder.append("<p>${player.username}".mm())
                builder.appendSpace().append("<s>- <white>$message".mm())
            }

            messageComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent))
        }
    }
}