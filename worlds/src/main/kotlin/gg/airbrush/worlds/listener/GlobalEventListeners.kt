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

package gg.airbrush.worlds.listener

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerSpawnEvent

class GlobalEventListeners {

    init {
        val eventHandler = MinecraftServer.getGlobalEventHandler()
        //eventHandler.addListener(PlayerLoginEvent::class.java, this::handlePlayerLogin)
        eventHandler.addListener(PlayerSpawnEvent::class.java, this::handlePlayerSpawn)
    }

    private fun handlePlayerSpawn(event: PlayerSpawnEvent) {
        // TODO: Allow user to define spawn point in worlds.toml
        val player = event.player
        player.teleport(Pos(0.0, 6.0, 0.0))
        player.inventory.clear()

        // Prevents creative carrying over from previous worlds
        if (player.gameMode !== GameMode.SURVIVAL) {
            player.gameMode = GameMode.SURVIVAL
        }
    }
}