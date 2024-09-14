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

package gg.airbrush.sdk.classes.punishments

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.NotFoundException
import java.time.Instant
import java.util.*

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