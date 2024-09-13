

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

import gg.airbrush.sdk.SDK
import net.minestom.server.instance.block.Block
import java.util.UUID

object PlayerDataCache {
    val timeJoined = HashMap<UUID, Long>()
    val blockCounts = HashMap<UUID, Long>()
	private val blockMasks = HashMap<UUID, Block>()

    fun incrementBlockCount(uuid: UUID, value: Int) {
        val oldValue = blockCounts[uuid] ?: 0
        blockCounts[uuid] = oldValue + value
    }

//    fun populateCache(uuid: UUID) {
//        val pixels = sdk.pixels.get(uuid)
//        blockCounts[uuid] = pixels.getData().pixels.size
//        timeJoined[uuid] = System.currentTimeMillis()
//    }

	fun populateCache(uuid: UUID) {
		val pixels = SDK.players.get(uuid).getData().blockCount
		blockCounts[uuid] = pixels
		timeJoined[uuid] = System.currentTimeMillis()
	}

    fun getCurrentPlaytime(uuid: UUID): Long {
        val playerData = SDK.players.get(uuid).getData()
        val sessionDuration = System.currentTimeMillis() - (timeJoined[uuid] ?: 0)
        return playerData.timePlayed + sessionDuration
    }

    fun getSessionTime(uuid: UUID): Long {
        return System.currentTimeMillis() - (timeJoined[uuid] ?: 0)
    }

	fun setBlockMask(uuid: UUID, block: Block) {
		blockMasks[uuid] = block
	}

	fun getBlockMask(uuid: UUID): Block? {
		return blockMasks[uuid]
	}

	fun clearBlockMask(uuid: UUID) {
		blockMasks.remove(uuid)
	}
}