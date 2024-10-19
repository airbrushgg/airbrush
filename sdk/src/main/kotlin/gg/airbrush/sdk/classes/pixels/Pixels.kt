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

package gg.airbrush.sdk.classes.pixels

import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.config
import gg.ingot.iron.Iron
import gg.ingot.iron.annotations.Column
import gg.ingot.iron.annotations.Model
import gg.ingot.iron.bindings.bind
import gg.ingot.iron.strategies.NamingStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.item.Material
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

@Model
data class PixelData(
    val id: Int? = null,
    val timestamp: Long,
    @Column(name = "world_id")
    val worldId: String,
    @Column(name = "player_uuid")
    val playerUuid: UUID,
    val x: Int,
    val y: Int,
    val z: Int,
    val material: Int,
) {
    companion object {
        val TABLE_NAME = if(SDK.isDev) "pixel_data_dev" else "pixel_data"

        val tableDefinition = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                id SERIAL PRIMARY KEY,
                timestamp BIGINT NOT NULL,
                world_id TEXT NOT NULL,
                player_uuid UUID NOT NULL,
                x SMALLINT NOT NULL,
                y SMALLINT NOT NULL,
                z SMALLINT NOT NULL,
                material SMALLINT NOT NULL
            );
        """.trimIndent()
    }
}

@Model
data class MaterialPair(val material: Int, val count: Int)

class Pixels {
    private val iron = Iron(config.sqlURL) {
        namingStrategy = NamingStrategy.SNAKE_CASE
        minConnections = 2
        maxConnections = 10
    }

    init {
        iron.connect()

        CoroutineScope(Dispatchers.IO).launch {
            iron.execute(PixelData.tableDefinition)
        }
    }

    /**
     * Paints multiple pixels
     */
    suspend fun paintMulti(positions: List<Point>, player: UUID, material: Material, world: String) {
        iron.transaction {
            for (position in positions) {
                val data = PixelData(
                    worldId = world,
                    timestamp = System.currentTimeMillis(),
                    playerUuid = player,
                    x = position.x().roundToInt(),
                    y = position.y().roundToInt(),
                    z = position.z().roundToInt(),
                    material = material.id()
                )
                prepare("""
                    INSERT INTO ${PixelData.TABLE_NAME} (timestamp, world_id, player_uuid, x, y, z, material)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(), data.timestamp, data.worldId, data.playerUuid, data.x, data.y, data.z, data.material)
            }
        }
    }

    /**
     * Gets the history for a specific world, during a specific time.
     */
    suspend fun getHistoryByTime(threshold: Instant, world: String): Flow<PixelData> = flow {
        val history = iron.prepare("""
            SELECT * FROM ${PixelData.TABLE_NAME}
            WHERE world_id = :worldId AND timestamp >= :timestamp
            ORDER BY timestamp ASC
        """.trimIndent(), bind {
            "worldId" to world
            "timestamp" to threshold.toEpochMilli()
        })

        while (history.next())
            emit(history.get()!!)
    }

    /**
     * Gets the history for a specific position.
     */
    suspend fun getHistoryAt(position: Point, limit: Int, world: String): List<PixelData> {
         val history: List<PixelData> = iron.prepare("""
                 SELECT * FROM ${PixelData.TABLE_NAME}
                 WHERE world_id = :worldId AND x = :x AND y = :y AND z = :z
                 ORDER BY timestamp DESC
                 LIMIT :limit
            """.trimIndent(),
            bind {
                "worldId" to world
                "x" to position.x().roundToInt()
                "y" to position.y().roundToInt()
                "z" to position.z().roundToInt()
                "limit" to limit
            }
        ).all<PixelData>()
        return history
    }

    /**
     * Wipes all pixels for a specific world.
     */
    suspend fun wipeHistoryForWorld(world: String) {
        MinecraftServer.LOGGER.info("[SDK] Wiping history for world $world...")
        val time = measureTimeMillis {
            iron.prepare("""
                DELETE FROM ${PixelData.TABLE_NAME}
                WHERE world_id = :worldId
            """.trimIndent(), bind("worldId" to world))
        }
        MinecraftServer.LOGGER.info("[SDK] Wiped history for world $world in $time ms")
    }

    /**
     * Prunes pixels older than three days.
     */
    fun prunePixels() {
        CoroutineScope(Dispatchers.IO).launch {
            val threshold = Instant.now().minus(3, ChronoUnit.DAYS)

            val count = iron.prepare("""
                SELECT COUNT(*) FROM ${PixelData.TABLE_NAME}
                WHERE timestamp < ?
            """.trimIndent(), threshold.toEpochMilli()).single<Long>()

            if (count == 0L) return@launch

            iron.prepare("""
                DELETE FROM ${PixelData.TABLE_NAME}
                WHERE timestamp < ?
            """.trimIndent(), threshold.toEpochMilli())
        }
    }

    suspend fun getTopMaterials(playerUUID: UUID): List<Pair<Material, Int>> {
        val query = """
            SELECT material, COUNT(*) AS count
            FROM ${PixelData.TABLE_NAME}
            WHERE player_uuid = ?
            GROUP BY material
            ORDER BY count DESC
            LIMIT 3;
        """.trimIndent()

        val topMaterials = iron.prepare(query, playerUUID).all<MaterialPair>()

        return topMaterials
            .map { Material.fromId(it.material)!! to it.count }
            .toList()
    }

}