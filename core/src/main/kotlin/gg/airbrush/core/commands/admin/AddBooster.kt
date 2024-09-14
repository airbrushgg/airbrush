

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

package gg.airbrush.core.commands.admin

import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class AddBooster : Command("addbooster"), CommandExecutor {
    private val registeredBoosters = SDK.boosters.getAvailableBoosters()

    init {
        setCondition { sender, _ -> sender.hasPermission("core.staff") }
        addSyntax(this::apply, ArgumentType.String("player"), ArgumentType.String("id"), ArgumentType.Integer("amount"))
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return

        val errorMessage = executeCommand(sender, context) ?: return
        sender.sendMessage(errorMessage.mm())
    }

    private fun executeCommand(sender: Player, context: CommandContext): String? {
        val player = context.get<String>("player") ?: return "<error>No specified player!"
        val id = context.get<String>("id") ?: return "<error>No specified ID!"
        val amount = context.get<Int>("amount") ?: return "<error>No specified amount!"

        val boosterInfo = registeredBoosters.find { b -> b.id == id }
            ?: return "<error>No such booster exists with that id!"

        val sdkPlayer = SDK.players.get(PlayerUtils.getUUID(player))
        for (i in 0 until amount) {
            sdkPlayer.addBooster(boosterInfo)
        }

        sender.sendMessage("<success>They were given ${boosterInfo.name}!".mm())
        return null
    }
}