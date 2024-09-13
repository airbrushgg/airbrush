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