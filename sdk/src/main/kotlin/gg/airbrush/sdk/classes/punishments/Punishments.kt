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
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
import java.time.Instant
import java.util.*

class Punishments {
    private val db = Database.get()
    private val col = db.getCollection<PunishmentData>("punishments")

    fun create(
        moderator: UUID,
        player: UUID,
        reason: String,
        type: Int,
        duration: Long,
		notes: String? = null,
        active: Boolean = true
    ): PunishmentData {
        val punishment = PunishmentData(
            moderator,
            player,
            reason,
            type,
            createdAt = Instant.now().epochSecond,
            active = active,
            duration = duration,
	        notes = notes
        )

        col.insertOne(punishment)

        punishmentCache.remove(player)

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

    fun list(player: Player) = list(player.uuid)

	fun list(playerUUID: UUID): List<AirbrushPunishment> {
        return punishmentCache.computeIfAbsent(playerUUID) {
            col
                .find(Filters.eq(PunishmentData::player.name, playerUUID))
                .map { AirbrushPunishment(UUID.fromString(it.id)) }
                .toList()
        }
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

    companion object {
        /* player UUID -> list of punishments */
        private val punishmentCache = HashMap<UUID, List<AirbrushPunishment>>()

        init {
            val eventNode = EventNode.type("Player Cache", EventFilter.PLAYER)
            eventNode.addListener(PlayerDisconnectEvent::class.java) { event ->
                punishmentCache.remove(event.player.uuid)
            }
            MinecraftServer.getGlobalEventHandler().addChild(eventNode)
        }
    }
}