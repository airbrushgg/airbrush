package gg.airbrush.sdk.classes.worlds

import com.mongodb.client.model.Filters
import gg.airbrush.sdk.Database
import java.util.UUID

class Worlds {
    private val db = Database.get()
    private val col = db.getCollection<WorldData>("worlds")

    @Suppress("unused")
    fun create(name: String, ownedBy: UUID): AirbrushWorld {
        val data = WorldData(name, ownedBy.toString())
        col.insertOne(data)
        return AirbrushWorld(UUID.fromString(data.id))
    }

    @Suppress("unused")
    fun getAll(): List<AirbrushWorld> {
        return col.find().map { data -> AirbrushWorld(UUID.fromString(data.id)) }.toList()
    }

    @Suppress("unused")
    fun get(name: String): AirbrushWorld? {
        return col.find(Filters.eq(WorldData::name.name, name)).firstOrNull()?.let { data ->
            AirbrushWorld(UUID.fromString(data.id))
        }
    }

    @Suppress("unused")
    fun getByOwner(playerUUID: String): AirbrushWorld? {
        return col.find(Filters.eq(WorldData::ownedBy.name, playerUUID)).firstOrNull()?.let { data ->
            AirbrushWorld(UUID.fromString(data.id))
        }
    }

	@Suppress("unused")
	fun getByUUID(uuid: String): AirbrushWorld? {
		return col.find(Filters.eq(WorldData::id.name, uuid)).firstOrNull()?.let { data ->
			AirbrushWorld(UUID.fromString(data.id))
		}
	}
}