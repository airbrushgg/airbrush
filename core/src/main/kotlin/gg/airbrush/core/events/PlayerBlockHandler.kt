

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

package gg.airbrush.core.events

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent

class PlayerBlockHandler {
    init {
        val eventHandler = MinecraftServer.getGlobalEventHandler()

        eventHandler.addListener(
            PlayerBlockBreakEvent::class.java
        ) { event: PlayerBlockBreakEvent ->
            event.isCancelled = event.player.gameMode !== GameMode.CREATIVE
        }

        eventHandler.addListener(
            PlayerBlockPlaceEvent::class.java
        ) { event: PlayerBlockPlaceEvent ->
            event.isCancelled = event.player.gameMode !== GameMode.CREATIVE
        }
    }

}