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

package gg.airbrush.server.commands

import gg.airbrush.server.consoleThread
import gg.airbrush.server.lib.mm
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class Stop : Command("stop"), CommandExecutor {
    init {
        setCondition { sender, _ ->
            sender.hasPermission("airbrush.stop")
        }

        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        for (player in MinecraftServer.getConnectionManager().onlinePlayers)
            player.kick("<s>Server is stopping.".mm())

	    val manager = MinecraftServer.getInstanceManager()
	    manager.instances.forEach {
			it.saveChunksToStorage()
	    }

        thread {
            Thread.sleep(1000)
            MinecraftServer.stopCleanly()
            consoleThread.interrupt()
            Thread.sleep(500)
            exitProcess(0)
        }
    }
}