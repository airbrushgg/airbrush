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

data class RevertedData(
	val revertedBy: String,
	val revertedAt: Long = Instant.now().epochSecond,
	val revertedReason: String = "No reason specified",
)

data class PunishmentData(
	val moderator: UUID,
	val player: UUID,
	var reason: String,
	val type: Int,
	val createdAt: Long = Instant.now().epochSecond,
	val updatedAt: Long = createdAt,
	var duration: Long,
	var active: Boolean = true,
	val id: String = UUID.randomUUID().toString(),
	var notes: String? = null,
	var reverted: RevertedData? = null,
)

class AirbrushPunishment(id: UUID) {
    private val col = db.getCollection<PunishmentData>("punishments")
    private val query = Filters.eq(PunishmentData::id.name, id.toString())
    val data = col.find(query).firstOrNull()
        ?: throw NotFoundException("Punishment with ID of $id not found.")

    fun getModerator(): UUID {
        return data.moderator
    }

    fun getPlayer(): UUID {
        return data.player
    }

    fun getCreatedAt(): Instant {
        return Instant.ofEpochSecond(data.createdAt)
    }

    fun getUpdatedAt(): Instant {
        return Instant.ofEpochSecond(data.updatedAt)
    }

	fun setDuration(duration: Long) {
		val now = Instant.now().epochSecond
		val expiry = duration + now

		setActive(expiry > now)

		col.updateOne(query, Updates.set(PunishmentData::duration.name, duration))
		data.duration = duration
	}

	fun setActive(active: Boolean) {
		col.updateOne(query, Updates.set(PunishmentData::active.name, active))
		data.active = active
	}

	fun setReverted(reverted: RevertedData) {
		setActive(false)
		col.updateOne(query, Updates.set(PunishmentData::reverted.name, reverted))
		data.reverted = reverted
	}

	fun setNotes(notes: String?) {
		if(notes == null) {
			col.updateOne(query, Updates.unset(PunishmentData::notes.name))
			data.notes = null
			return
		}

		col.updateOne(query, Updates.set(PunishmentData::notes.name, notes))
		data.notes = notes
	}

	fun setReason(reason: String) {
		col.updateOne(query, Updates.set(PunishmentData::reason.name, reason))
		data.reason = reason
	}

	fun getExpiry(): Instant {
		val combined = data.createdAt + data.duration

		if (data.duration >= Instant.MAX.epochSecond || combined >= Instant.MAX.epochSecond) {
			return Instant.MAX
		}

		return Instant.ofEpochSecond(combined)
	}
}