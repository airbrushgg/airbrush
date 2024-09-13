

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

import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class Teleport : Command("tp", "teleport"), CommandExecutor {
	private val destinationArg = ArgumentType.Entity("destination").onlyPlayers(true)

	init {
		defaultExecutor = this
		addSyntax(this, destinationArg)
		setCondition { sender, _ -> sender.hasPermission("core.staff") }
	}

	override fun apply(sender: CommandSender, context: CommandContext) {
		if (sender !is Player) return

		if (!context.has(destinationArg)) {
			sender.sendMessage("<error>You must specify a player to teleport to".mm())
			return
		}

		val destination = context.get(destinationArg).findFirstPlayer(sender)
		if (destination == null) {
			sender.sendMessage("<error>That player is not online".mm())
			return
		}

		if (sender.instance != destination.instance) {
			sender.setInstance(destination.instance, destination.position)
		} else {
			sender.teleport(destination.position)
		}
	}
}
