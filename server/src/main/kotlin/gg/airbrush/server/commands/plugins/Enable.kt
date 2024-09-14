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

package gg.airbrush.server.commands.plugins

import gg.airbrush.server.commands.arguments.PluginArgument
import gg.airbrush.server.lib.mm
import gg.airbrush.server.pluginManager
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.command.builder.Command

object Enable : Command("enable") {
    init {
        setCondition { sender, _ -> sender.hasPermission("airbrush.pluginManager") }
        addSyntax({ sender, context ->
            val plugin = context.get<Plugin>("plugin")

            if (plugin.isSetup) {
                sender.sendMessage("<s>Plugin <p>${plugin.info.id}</p> is already enabled.".mm())
                return@addSyntax
            }

            sender.sendMessage("<s>Enabling plugin <p>${plugin.info.id}</p>...".mm())
            pluginManager.enablePlugin(plugin)
            sender.sendMessage("<s>Plugin <p>${plugin.info.id}</p> has been enabled.".mm())
        }, PluginArgument("plugin"))
    }
}