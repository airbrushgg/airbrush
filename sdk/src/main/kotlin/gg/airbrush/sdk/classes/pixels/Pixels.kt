package gg.airbrush.sdk.classes.pixels

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.SDK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minestom.server.coordinate.Point
import net.minestom.server.item.Material
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

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