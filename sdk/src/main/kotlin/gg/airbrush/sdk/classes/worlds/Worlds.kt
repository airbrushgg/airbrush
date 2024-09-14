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

package gg.airbrush.sdk.classes.worlds

import com.mongodb.client.model.Filters
import gg.airbrush.sdk.Database
import java.util.UUID

class Worlds {
    private val db = Database.get()
    private val col = db.getCollection<WorldData>("worlds")

    fun create(name: String, ownedBy: UUID): AirbrushWorld {
        val data = WorldData(name, ownedBy.toString())
        col.insertOne(data)
        return AirbrushWorld(UUID.fromString(data.id))
    }

    fun getAll(): List<AirbrushWorld> {
        return col.find().map { data -> AirbrushWorld(UUID.fromString(data.id)) }.toList()
    }

    fun get(name: String): AirbrushWorld? {
        return col.find(Filters.eq(WorldData::name.name, name)).firstOrNull()?.let { data ->
            AirbrushWorld(UUID.fromString(data.id))
        }
    }

    fun getByOwner(playerUUID: String): AirbrushWorld? {
        return col.find(Filters.eq(WorldData::ownedBy.name, playerUUID)).firstOrNull()?.let { data ->
            AirbrushWorld(UUID.fromString(data.id))
        }
    }

	fun getByUUID(uuid: String): AirbrushWorld? {
		return col.find(Filters.eq(WorldData::id.name, uuid)).firstOrNull()?.let { data ->
			AirbrushWorld(UUID.fromString(data.id))
		}
	}
}