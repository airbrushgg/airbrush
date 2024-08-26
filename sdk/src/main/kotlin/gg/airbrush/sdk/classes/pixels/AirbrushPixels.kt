package gg.airbrush.sdk.classes.pixels

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class Location(
    val x: Int,
    val y: Int,
    val z: Int,
)
fun Point.to(): Location {
    return Location(blockX(), blockY(), blockZ())
}

data class Pixel(
    val position: Location,
    val player: UUID,
    val material: String,
    val timestamp: Long,
)

//    fun getTopPixels(): List<Material> {
//        val materialGroups = data.pixels
//            .groupBy { it.from().material }
//            .map { (k, v) -> v.count() to k }
//            .sortedBy { it.first }
//
//        return materialGroups.take(3).map { it.second }
//    }