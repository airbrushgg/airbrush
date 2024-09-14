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

package gg.airbrush.server.plugins

import net.minestom.server.MinecraftServer

data class PluginInfo(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val mainClass: String,
    val dependencies: MutableList<String> = mutableListOf()
) {
    init {
        for (dependency in dependencies) {
            if (dependency.matches(Regex("[0-9a-z-]+")))
                continue

            MinecraftServer.LOGGER.warn("Plugin '$id' contains invalid dependency '$dependency' ([0-9a-z-])")
        }
    }
}
