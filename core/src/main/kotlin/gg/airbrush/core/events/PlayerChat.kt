

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
import gg.airbrush.core.filter.FilterAction
import gg.airbrush.core.filter.chatFilterInstance
import gg.airbrush.core.lib.ColorUtil
import gg.airbrush.discord.useBot
import gg.airbrush.punishments.commands.nilUUID
import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.punishments.lib.Punishment
import gg.airbrush.punishments.lib.User
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.InputHandler
import gg.airbrush.server.lib.mm
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.color.Color
import net.minestom.server.event.player.PlayerChatEvent
import org.slf4j.LoggerFactory

class PlayerChat {
    private val logger = LoggerFactory.getLogger(PlayerChat::class.java)

    init {
        // NOTE: This is intentionally added to the global event handler.
        val eventHandler = MinecraftServer.getGlobalEventHandler()

        eventHandler.addListener(
            PlayerChatEvent::class.java
        ) { event: PlayerChatEvent -> execute(event) }
    }

    private fun PlayerChatEvent.runFilter() {
        val filterResult = chatFilterInstance.validateMessage(this.player, this.message)
        val filterRuleset = filterResult.ruleset
        val filterAction = filterRuleset?.action

        if (filterAction == FilterAction.BLOCK || filterAction == FilterAction.BAN) {
            this.isCancelled = true

            logger.info("${player.username} triggered the filter with message: ${this.message}")

            val underlinedMessage = this.message.replace(filterResult.failedTokens.first().value, "<u><#ff6e6e>${filterResult.failedTokens.first().value}</#ff6e6e></u>")
            Audiences.players { it.hasPermission("core.staff") }.sendMessage("<s>[Filter] <p>${player.username}</p> sent: $underlinedMessage (<p>$filterAction</p>)".mm())

            if (filterRuleset.action == FilterAction.BAN) {
                Punishment(
                    User(nilUUID, "Console"),
                    User(player.uuid, player.username),
                    filterRuleset.banReason,
                    PunishmentTypes.AUTO_BAN
                ).handle()
            }

            useBot {
                it.getTextChannelById(chatFilterInstance.logChannel ?: "0")?.let { logChannel ->
                    val firstToken = filterResult.failedTokens.first()

                    val formattedMessage = this.message
                        .replace("`", "\\`")
                        .replaceFirst(firstToken.value, "`>>>${firstToken.value}<<<`")

                    val environment = if(SDK.isDev) "Development" else "Production"

                    val logEmbed = EmbedBuilder()
                        .setTitle("`${this.player.username}` triggered the filter")
                        .setColor(java.awt.Color.decode("#ff6e6e"))
                        .addField(MessageEmbed.Field("Message", formattedMessage, false))
                        .addField(MessageEmbed.Field("Action", filterAction.toString(), false))
                        .setFooter("Path: ${filterResult.ruleset.path.substringAfterLast('/')} (Priority: ${filterResult.ruleset.priority}) [Env: $environment]")
                        .build()

                    logChannel.sendMessageEmbeds(logEmbed).queue()
                }
            }

            return
        }
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

        if(!InputHandler.isUsingUnfiltered(player)) event.runFilter()

        event.setChatFormat { _ ->
            val level = sdkPlayer.getLevel()
            val levelColor = ColorUtil.oscillateHSV(Color(0xff0000), Color(0xff00ff), level)
            val rankData = sdkPlayer.getRank().getData()

	        val pronounData = sdkPlayer.getData().pronouns
	        val pronouns = if(!pronounData.isNullOrEmpty()) pronounData else "None set"
	        val message = PlainTextComponentSerializer.plainText().serialize(event.message.mm())

            val hoverComponent = Component.text { builder ->
                if (player.hasPermission("core.staff")) {
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