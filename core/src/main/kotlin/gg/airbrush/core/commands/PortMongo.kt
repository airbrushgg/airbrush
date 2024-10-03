

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

import gg.airbrush.sdk.SDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

class PortMongo : Command("portmongo"), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.admin") }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("""
            Porting pixels from MongoDB to SQL...
            This may take a while.
        """.trimIndent())

        CoroutineScope(Dispatchers.IO).launch {
            SDK.pixels.portFromMongo()

            sender.sendMessage("Finished porting pixels from MongoDB to SQL.")
        }
    }
}