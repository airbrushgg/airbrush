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
import gg.airbrush.server.plugins.PluginManager
import net.minestom.server.command.builder.Command

object Disable : Command("disable") {
    init {
        setCondition { sender, _ -> sender.hasPermission("airbrush.pluginManager") }
        addSyntax({ sender, context ->
            val plugin = context.get<Plugin>("plugin")

            if (!plugin.isSetup) {
                sender.sendMessage("<s>Plugin <p>${plugin.info.id}</p> is not enabled.".mm())
                return@addSyntax
            }

            sender.sendMessage("<s>Disabling plugin <p>${plugin.info.id}</p>...".mm())
            pluginManager.disablePlugin(plugin)
            sender.sendMessage("<s>Plugin <p>${plugin.info.id}</p> has been disabled.".mm())
        }, PluginArgument("plugin"))
    }
}