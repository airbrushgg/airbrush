

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

package gg.airbrush.core.commands.history

import gg.airbrush.core.lib.*
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.sdk.lib.handleCooldown
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.item.Material

class History : Command("history"), CommandExecutor {
    private val limitArgument = ArgumentType.Integer("limit")
        .setDefaultValue(5)

    init {
        defaultExecutor = this
        addSyntax(this::apply, limitArgument)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val player = sender as Player

        val block = player.getTargetBlockPosition(Constants.EXTENDED_RANGE) ?: return
        val blockPos = Pos(
            block.blockX().toDouble(),
            block.blockY().toDouble(),
            block.blockZ().toDouble()
        )

        val playerCooldown = handleCooldown {
            key = "history-${player.uuid}"
            duration = if(player.hasPermission("core.staff")) 1000 else 5000
        }

        if(playerCooldown.isActive()) {
            player.sendMessage("<error>Slow down! You've recently viewed history.".mm())
            return
        }

        // NOTE: This is up here because DB queries happen
        // regardless of if the command succeeds or not.
        playerCooldown.startCooldown()

        player.sendMessage("<p>Loading pixel data for this pixel...".mm())
        val world = player.getCurrentWorldID()

        val limit = context.get(limitArgument) ?: 5
        val pixelData = SDK.pixels.getHistoryAt(blockPos, limit, world)
        if (pixelData.isEmpty()) {
            sender.sendMessage("<error>Could not fetch pixel data for location".mm())
            return
        }

        val msg = mutableListOf<String>()
        // Reverse the pixel data since it is returned in descending order (by timestamp).
        pixelData.asReversed().forEach {
            val time = it.timestamp.toRelativeTime()
            val painter = PlayerUtils.getName(it.player)
            val material = Material.fromId(it.material) ?: return@forEach
            msg.add("<p><s>$painter</s> painted <s>${material.name().prettify()}</s> ($time)")
        }

        player.sendMessage("<p><s>${msg.size}</s> actions have occurred here.".mm())
        player.sendMessage(msg.joinToString("\n").mm())
    }
}