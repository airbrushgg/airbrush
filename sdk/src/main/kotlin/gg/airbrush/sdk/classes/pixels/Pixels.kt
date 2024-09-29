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

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Sorts
import gg.airbrush.sdk.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minestom.server.coordinate.Point
import net.minestom.server.item.Material
import org.bson.codecs.pojo.annotations.BsonId
import java.util.*
import kotlin.system.measureTimeMillis

class Pixels {
    private val db = Database.get()
    private val col = db.getCollection<Pixel>("pixels")

    suspend fun paintMulti(positions: List<Point>, player: UUID, material: Material, world: String) {
        val now = System.currentTimeMillis()

        withContext(Dispatchers.IO) {
            val time = measureTimeMillis {
                positions.forEach { pos ->
                    val pixelLocation = pos.to()

                    val existingPixel = col.find(
                        Filters.and(
                            Filters.eq("position", pixelLocation),
                            Filters.eq("worldId", world)
                        )
                    ).firstOrNull()

                    val updatedPixel = existingPixel?.copy(
                        changes = existingPixel.changes + History(
                            player = player,
                            material = material.id(),
                            timestamp = now
                        )
                    ) ?: Pixel(
                        position = pixelLocation,
                        worldId = world,
                        changes = listOf(
                            History(
                                player = player,
                                material = material.id(),
                                timestamp = now
                            )
                        )
                    )

                    col.replaceOne(
                        Filters.and(
                            Filters.eq("position", pixelLocation),
                            Filters.eq("worldId", world)
                        ),
                        updatedPixel,
                        ReplaceOptions().upsert(true)
                    )
                }
            }
            println("[SDK] Pixels#paintMulti took $time ms, processed ${positions.size} pixels.")
        }
    }


    fun getHistoryAt(position: Point, limit: Int, world: String): List<History> {
        val filter = Filters.and(
            Filters.eq(Pixel::position.name, position.to()),
            Filters.eq(Pixel::worldId.name, world)
        )
        val pixel = col.find(filter).firstOrNull()
        return pixel?.changes ?: emptyList()
    }

    suspend fun wipeHistoryForWorld(world: String) {
        val filter = Filters.eq(Pixel::worldId.name, world)
        withContext(Dispatchers.IO) {
            col.deleteMany(filter)
        }
    }

    data class MaterialPair(@BsonId val id: String, val count: Int)

    fun getTopMaterials(player: UUID): List<Pair<Material, Int>> {
        val result = col.aggregate<MaterialPair>(
            listOf(
                Aggregates.match(Filters.eq(History::player.name, player)),
                Aggregates.group("\$${History::material.name}", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending(MaterialPair::count.name)),
                Aggregates.limit(3),
            )
        )

        return result
            .map { Material.fromNamespaceId(it.id)!! to it.count }
            .toList()
    }
}