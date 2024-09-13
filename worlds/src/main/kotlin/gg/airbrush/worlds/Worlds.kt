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

package gg.airbrush.worlds

import gg.airbrush.server.plugins.Plugin
import gg.airbrush.worlds.listener.GlobalEventListeners
import net.minestom.server.MinecraftServer

class Worlds : Plugin() {
    override fun setup() {
        WorldManager.initialize()
        WorldManager.loadDefaultInstance()

        MinecraftServer.getSchedulerManager().buildShutdownTask {
            WorldManager.defaultInstance.saveChunksToStorage().join()
            WorldManager.dispose()
        }

        // Register events
        GlobalEventListeners()
    }

    override fun teardown() {
    }
}