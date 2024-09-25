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
import gg.airbrush.sdk.lib.PlayerUtils
import me.santio.coffee.common.annotations.Command
import me.santio.coffee.jda.annotations.Description
import me.santio.coffee.jda.annotations.Permission
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.*
import net.dv8tion.jda.api.Permission as JDAPermission

@Command
class WhitelistCommand {
    @Description("List all whitelisted players")
    @Permission(JDAPermission.MESSAGE_MANAGE)
    fun list(e: SlashCommandInteractionEvent) {
        e.deferReply(true).queue()

        val whitelistedPlayers = SDK.whitelist.list()

        val msg = whitelistedPlayers.map { entry ->
            val timestamp = "<t:${entry.addedAt / 1000}:R>"
            val player = PlayerUtils.getName(UUID.fromString(entry.uuid))
            "- $player added $timestamp"
        }.toList()

        val embed = if(msg.isNotEmpty()) {
            EmbedBuilder().setTitle("Whitelisted players")
                .setColor(Color.decode("#bfffc6"))
                .setDescription(msg.joinToString("\n"))
                .build()
        } else {
            EmbedBuilder().setTitle("No whitelisted players")
                .setColor(Color.decode("#ff6e6e"))
                .build()
        }

        e.answer(embed)
    }

    @Description("Add a player to the whitelist")
    @Permission(JDAPermission.ADMINISTRATOR)
    fun add(e: SlashCommandInteractionEvent, username: String) {
        e.deferReply(true).queue()

        val uuid = PlayerUtils.getUUID(username)
        // NOTE: This is fetched separately, rather than using the username
        // from the arg, so we can properly format the username
        val user = PlayerUtils.getName(uuid)

        SDK.whitelist.add(uuid)

        val embed = EmbedBuilder().setTitle("Whitelisted player")
            .setColor(Color.decode("#bfffc6"))
            .setDescription("Added player `$user` to the whitelist.")
            .build()

        e.answer(embed)
    }

    @Description("Remove a player from the whitelist")
    @Permission(JDAPermission.ADMINISTRATOR)
    fun remove(e: SlashCommandInteractionEvent, username: String) {
        e.deferReply(true).queue()

        val uuid = PlayerUtils.getUUID(username)
        // NOTE: This is fetched separately, rather than using the username
        // from the arg, so we can properly format the username
        val user = PlayerUtils.getName(uuid)

        SDK.whitelist.remove(uuid)

        val embed = EmbedBuilder().setTitle("Removed player")
            .setColor(Color.decode("#bfffc6"))
            .setDescription("Removed player `$user` from the whitelist.")
            .build()

        e.answer(embed)
    }
}

