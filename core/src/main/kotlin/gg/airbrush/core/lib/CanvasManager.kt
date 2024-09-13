

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
import gg.airbrush.worlds.WorldManager
import gg.airbrush.worlds.events.InstanceReadyEvent
import net.hollowcube.polar.PolarLoader
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.tag.Tag
import java.nio.file.Path
import kotlin.io.path.exists

object CanvasManager {
    private val canvasIdTag = Tag.String("CanvasUUID")

    fun create(player: Player) {
        if (SDK.worlds.getByOwner(player.uuid.toString()) != null) {
            player.sendMessage("<error>You already have a player canvas!")
            return
        }

        val sdkWorld = SDK.worlds.create("${player.username}'s World", player.uuid)
        val instance = createInstance(sdkWorld.data.id)
        instance.eventNode().addListener(InstanceReadyEvent::class.java) {
            sidebars[player.uuid]?.updateLineContent("world", getWorldLine(player))
            player.setInstance(it.instance)
        }
    }

    fun get(canvasId: String): Instance? {
        // If the world is already loaded, return it.
        val existingWorld = WorldManager.getWorlds().find { it.getTag(canvasIdTag) == canvasId }
        if (existingWorld != null) {
            return existingWorld
        }

        // Otherwise, check if it exists on disk. If it does, load and return it.
        val worldPath = Path.of("canvases/${canvasId}.polar")
        if (worldPath.exists()) {
            return createInstance(canvasId)
        }

        // If all else fails, return null.
        return null
    }

    fun saveAll() {
        WorldManager.getWorlds()
            .filter { it.hasTag(canvasIdTag) }
            .forEach { it.saveChunksToStorage().join() }
    }

    private fun createInstance(canvasId: String): Instance {
        return WorldManager.createFromTemplate("player_canvas").apply {
            // The WorldManager does not handle saving of templates, so we must do it manually.
            chunkLoader = PolarLoader(Path.of("canvases/${canvasId}.polar"))
            setTag(canvasIdTag, canvasId)
        }
    }
}