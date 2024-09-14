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
import com.mongodb.client.model.Sorts
import gg.airbrush.sdk.Database
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minestom.server.coordinate.Point
import net.minestom.server.item.Material
import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

class Pixels {
    private val db = Database.get()
    private val col = db.getCollection<Pixel>("pixels")

    fun paint(position: Point, player: UUID, material: Material) = runBlocking<Unit> {
        launch { col.insertOne(Pixel(position.to(), player, material.name(), System.currentTimeMillis())) }
    }

    fun paintMulti(positions: List<Point>, player: UUID, material: Material) = runBlocking<Unit> {
        val now = System.currentTimeMillis()
        launch {
            positions.map { pos -> Pixel(pos.to(), player, material.name(), now) }.let { col.insertMany(it) }
        }
    }

    fun getPixelAt(position: Point): Pixel? {
        val filter = Filters.eq(Pixel::position.name, position.to())
        return col.find(filter).sort(Sorts.descending(Pixel::timestamp.name)).firstOrNull()
    }

    fun getHistoryAt(position: Point, limit: Int): List<Pixel> {
        val filter = Filters.eq(Pixel::position.name, position.to())
        return col.find(filter).sort(Sorts.descending(Pixel::timestamp.name)).limit(limit).toList()
    }

    fun getPixelCount(player: UUID): Int {
        return col.countDocuments(Filters.eq(Pixel::player.name, player)).toInt()
    }

    data class MaterialPair(@BsonId val id: String, val count: Int)

    fun getTopMaterials(player: UUID): List<Pair<Material, Int>> {
        val result = col.aggregate<MaterialPair>(
            listOf(
                Aggregates.match(Filters.eq(Pixel::player.name, player)),
                Aggregates.group("\$${Pixel::material.name}", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending(MaterialPair::count.name)),
                Aggregates.limit(3),
            )
        )

        return result
            .map { Material.fromNamespaceId(it.id)!! to it.count }
            .toList()
    }
}