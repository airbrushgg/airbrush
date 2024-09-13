

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

package gg.airbrush.core.lib

import gg.airbrush.core.events.sidebars
import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.minestom.server.entity.Player

fun Player.getXPThreshold(): Int {
    val sdkPlayer = SDK.players.get(uuid)
    return (sdkPlayer.getLevel() % 100 + 1) * 25
}

fun Player.teleportToCanvas(canvasUUID: String) {
    val instance = CanvasManager.get(canvasUUID)
    if (instance == null) {
        sendMessage("<error>A problem occurred teleporting to this world!".mm())
        return
    }
    sidebars[uuid]?.updateLineContent("world", getWorldLine(this))
    setInstance(instance)
}

fun Player.teleportToSpawn() {
    this.instance = WorldManager.defaultInstance
}