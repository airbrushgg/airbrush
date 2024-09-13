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