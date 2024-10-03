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

import gg.ingot.iron.Iron
import gg.ingot.iron.annotations.Column
import gg.ingot.iron.annotations.Model
import gg.ingot.iron.representation.DatabaseDriver
import gg.ingot.iron.sql.params.sqlParams
import gg.ingot.iron.strategies.NamingStrategy
import kotlinx.coroutines.*
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.item.Material
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

@Model
data class PixelData(
    @Column(primaryKey = true)
    val id: Int? = null,
    val timestamp: Long,
    val worldId: String,
    val playerUuid: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val material: Int,
) {
    companion object {
        val tableDefinition = """
            CREATE TABLE IF NOT EXISTS pixel_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                world_id TEXT NOT NULL,
                player_uuid TEXT NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                material INTEGER NOT NULL
            );
        """.trimIndent()
    }
}

class Pixels {
    val data = File("data")

    private val iron = Iron("jdbc:sqlite:${data.absolutePath}/pixel_data.db") {
        namingStrategy = NamingStrategy.SNAKE_CASE
        driver = DatabaseDriver.SQLITE
    }

    init {
        if(!data.exists()) data.mkdir()
        iron.connect()

        CoroutineScope(Dispatchers.IO).launch {
            iron.execute(PixelData.tableDefinition)
        }
    }

    suspend fun paintMulti(positions: List<Point>, player: UUID, material: Material, world: String) {
        iron.transaction {
            for (position in positions) {
                val data = PixelData(
                    worldId = world,
                    timestamp = System.currentTimeMillis(),
                    playerUuid = player.toString(),
                    x = position.x(),
                    y = position.y(),
                    z = position.z(),
                    material = material.id()
                )
                prepare("""
                    INSERT INTO pixel_data (timestamp, world_id, player_uuid, x, y, z, material)
                     VALUES (:timestamp, :worldId, :playerUuid, :x, :y, :z, :material)
                """.trimIndent(), data)
            }
        }
    }

    suspend fun getHistoryAt(position: Point, limit: Int, world: String): List<PixelData> {
        val history: List<PixelData> = iron.prepare("""
                 SELECT * FROM pixel_data
                 WHERE world_id = :worldId AND x = :x AND y = :y AND z = :z
                 ORDER BY timestamp DESC
                 LIMIT :limit
            """.trimIndent(),
            sqlParams(
                "worldId" to world,
                "x" to position.x(),
                "y" to position.y(),
                "z" to position.z(),
                "limit" to limit,
            )
        ).all<PixelData>()
        return history
    }

    suspend fun wipeHistoryForWorld(world: String) {
        MinecraftServer.LOGGER.info("[SDK] Wiping history for world $world...")
        val time = measureTimeMillis {
            iron.prepare("""
                DELETE FROM pixel_data
                WHERE world_id = :worldId
            """.trimIndent(), sqlParams("worldId" to world))
        }
        MinecraftServer.LOGGER.info("[SDK] Wiped history for world $world in $time ms")
    }

    @Model
    data class MaterialPair(val material: Int, val count: Int)

    suspend fun getTopMaterials(player: UUID): List<Pair<Material, Int>> {
        val query = """
            SELECT material, COUNT(*) AS count
            FROM pixel_data
            WHERE player_uuid = ?
            GROUP BY material
            ORDER BY count DESC
            LIMIT 3;
        """.trimIndent()

        val topMaterials = iron.prepare(query, player).all<MaterialPair>()

        return topMaterials
            .map { Material.fromId(it.material)!! to it.count }
            .toList()
    }

}