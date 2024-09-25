/*
 * This file is part of Airbrush
 *
 * Copyright (c) Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.discord.discordCommands

import gg.airbrush.discord.lib.answer
import me.santio.coffee.common.annotations.Command
import me.santio.coffee.jda.annotations.Description
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.minestom.server.MinecraftServer
import java.awt.Color

@Command
@Description("View players on Airbrush")
class PlayersCommand {
    private fun Int.pluralize() = if(this == 1) "" else "s"

    fun main(e: SlashCommandInteractionEvent) {
        e.deferReply(true).queue()

        val players = MinecraftServer.getConnectionManager().onlinePlayers

        var playerList = players.joinToString("\n") { "- ${it.username}" }

        // prevent message from being too long, causing an error
        if(playerList.length > 4096) playerList = playerList.substring(0, 4096)

        val embed = if(players.isNotEmpty()) {
            EmbedBuilder().setTitle("${players.size} player${players.size.pluralize()} on Airbrush")
                .setColor(Color.decode("#bfffc6"))
                .setDescription(playerList)
                .build()
        } else {
            EmbedBuilder().setTitle("No players online")
                .setColor(Color.decode("#ff6e6e"))
                .build()
        }

        e.answer(embed)
    }
}