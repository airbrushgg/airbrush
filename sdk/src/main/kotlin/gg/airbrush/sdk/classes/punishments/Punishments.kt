package gg.airbrush.sdk.classes.punishments

import com.mongodb.client.model.Filters
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.NotFoundException
import gg.airbrush.sdk.classes.ranks.RankData
import net.minestom.server.entity.Player
import org.bson.conversions.Bson
import java.time.Instant
import java.util.UUID

class Punishments {
    private val db = Database.get()
    private val col = db.getCollection<PunishmentData>("punishments")

    fun create(
        moderator: UUID,
        player: UUID,
        reason: String,
        type: Int,
        duration: Int,
		notes: String? = null
    ): PunishmentData {
        val punishment = PunishmentData(
            moderator.toString(),
            player.toString(),
            reason,
            type,
            Instant.now().epochSecond,
            duration = duration,
	        notes = notes
        )

        col.insertOne(punishment)

        return punishment
    }

    fun delete(id: UUID) {
        if (!exists(id))
            throw NotFoundException("No punishment with ID of $id exists.")

        col.deleteOne(Filters.eq(PunishmentData::id.name, id.toString()))
    }

    fun exists(id: UUID): Boolean {
        return col.find(Filters.eq(PunishmentData::id.name, id.toString())).firstOrNull() != null
    }

    fun list(player: Player): List<AirbrushPunishment> {
        return col
            .find(Filters.eq(PunishmentData::player.name, player.uuid.toString()))
            .map { AirbrushPunishment(UUID.fromString(it.id)) }
            .toList()
    }

	fun list(playerUUID: UUID): List<AirbrushPunishment> {
		return col
			.find(Filters.eq(PunishmentData::player.name, playerUUID.toString()))
			.map { AirbrushPunishment(UUID.fromString(it.id)) }
			.toList()
	}

	fun getAll(): List<AirbrushPunishment> {
		return col.find()
			.map { AirbrushPunishment(UUID.fromString(it.id)) }
			.toList()
	}

    fun get(id: UUID): AirbrushPunishment {
        if (!exists(id))
            throw NotFoundException("No punishment with ID of $id exists.")

        val data = col.find(Filters.eq(PunishmentData::id.name, id.toString())).first()
        return AirbrushPunishment(UUID.fromString(data.id))
    }
}