package gg.airbrush.sdk.classes.worlds

import com.mongodb.client.model.Filters
import gg.airbrush.sdk.Database
import java.util.*

enum class WorldVisibility {
    PUBLIC,
    PRIVATE
}

data class WorldData(
    var name: String,
    val ownedBy: String,
    val id: String = UUID.randomUUID().toString(),
    var visibility: WorldVisibility = WorldVisibility.PUBLIC,
)

class AirbrushWorld(uuid: UUID) {
    private val db = Database.get()
    private val col = db.getCollection<WorldData>("worlds")
    val data: WorldData

    init {
        data = col.find(Filters.eq(WorldData::id.name, uuid.toString())).firstOrNull()
            ?: throw Exception("World not found")
    }

    fun rename(newName: String) {
        data.name = newName
        col.replaceOne(Filters.eq(WorldData::id.name, data.id), data)
    }

    fun changeVisibility(visibility: WorldVisibility) {
        data.visibility = visibility
        col.replaceOne(Filters.eq(WorldData::id.name, data.id), data)
    }
}