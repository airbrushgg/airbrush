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

import gg.airbrush.server.commands.plugins.Disable
import gg.airbrush.server.commands.plugins.Enable
import gg.airbrush.server.commands.plugins.Reload
import gg.airbrush.server.lib.mm
import gg.airbrush.server.pluginManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

class Plugins : Command("plugins", "pl"), CommandExecutor {
    init {
        defaultExecutor = this

        addSubcommand(Reload)
        addSubcommand(Enable)
        addSubcommand(Disable)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val plugins = pluginManager.plugins.values
        val list = plugins.joinToString("</p>, <p>") {
            if (it.isSetup) { it.info.name } else { "<error>${it.info.name}" }
        }
        sender.sendMessage("<s>Plugins (<p>${plugins.size}</p>): <p>$list".mm())
    }
}