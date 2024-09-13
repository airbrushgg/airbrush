

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
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class SetRadius : Command("setradius", "sr"), CommandExecutor {
	init {
		setCondition { sender, _ -> sender.hasPermission("core.staff") }
		addSyntax(this::apply, ArgumentType.Integer("radius"))
	}

	override fun apply(sender: CommandSender, context: CommandContext) {
		val radius = context.get<Int>("radius")
		val player = sender as Player

		val sdkPlayer = SDK.players.get(player.uuid)
		sdkPlayer.setRadius(radius, true)

		player.sendMessage(Translations.translate("core.commands.radius", radius).mm())
	}
}