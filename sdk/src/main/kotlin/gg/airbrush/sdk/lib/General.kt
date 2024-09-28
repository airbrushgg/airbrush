/*
 * This file is part of Airbrush
 *
 * Copyright (c) Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.sdk.lib

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

/**
 * Runs the given code after the given time.
 */
fun delay(time: Long, block: () -> Unit) {
    Thread.sleep(time)
    block()
}

fun Player.executeCommand(command: String) {
    val manager = MinecraftServer.getCommandManager()
    manager.execute(this, command)
}

fun debounce(time: Long, identifier: String, block: () -> Unit) {
    val cooldown = handleCooldown {
        key = "debounce-${identifier}"
        duration = time
    }

    MinecraftServer.LOGGER.info("Creating debounce for ${cooldown.key} | ${cooldown.duration}")

    if(cooldown.isActive()) return

    cooldown.startCooldown()
    block()
}