

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

package gg.airbrush.core.commands

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player

class Gamemode : Command("gamemode", "gm"), CommandExecutor {
    init {
	    setCondition { sender, _ -> sender.hasPermission("core.staff") }

        addSyntax(this::apply, ArgumentType
            .Enum("gamemode", GameMode::class.java)
            .setFormat(ArgumentEnum.Format.LOWER_CASED))
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val gamemode = context.get<GameMode>("gamemode")
        val player = sender as Player

        player.gameMode = gamemode
        player.isAllowFlying = true

        player.sendMessage(Translations.translate("core.commands.gamemode", gamemode.name.lowercase()).mm())
    }
}