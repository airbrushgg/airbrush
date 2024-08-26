package gg.airbrush.sdk.classes.punishments

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.NotFoundException
import gg.airbrush.sdk.classes.ranks.RankData
import java.time.Instant
import java.util.UUID

private val db = Database.get()

data class PunishmentData(
	val moderator: String,
	val player: String,
	val reason: String,
	val type: Int,
	val createdAt: Long = Instant.now().epochSecond,
	val updatedAt: Long = createdAt,
	val duration: Int?,
	var active: Boolean = true,
	val id: String = UUID.randomUUID().toString(),
	var notes: String? = null
)

class AirbrushPunishment(id: UUID) {
    private val col = db.getCollection<PunishmentData>("punishments")
    private val query = Filters.eq(PunishmentData::id.name, id.toString())
    val data = col.find(query).firstOrNull()
        ?: throw NotFoundException("Punishment with ID of $id not found.")

    fun getModerator(): UUID {
        return UUID.fromString(data.moderator)
    }

    fun getPlayer(): UUID {
        return UUID.fromString(data.player)
    }

    fun getCreatedAt(): Instant {
        return Instant.ofEpochSecond(data.createdAt)
    }

    fun getUpdatedAt(): Instant {
        return Instant.ofEpochSecond(data.updatedAt)
    }

	fun setActive(active: Boolean) {
		col.updateOne(query, Updates.set(PunishmentData::active.name, active))
		data.active = active
	}

	fun setNotes(notes: String) {
		col.updateOne(query, Updates.set(PunishmentData::notes.name, notes))
		data.notes = notes
	}
}