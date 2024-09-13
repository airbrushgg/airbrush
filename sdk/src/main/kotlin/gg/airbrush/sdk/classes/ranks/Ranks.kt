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

package gg.airbrush.sdk.classes.ranks

import com.mongodb.client.model.Filters
import gg.airbrush.sdk.NotFoundException
import gg.airbrush.sdk.Database
import java.util.UUID

class Ranks {
    private val db = Database.get()
    private val col = db.getCollection<RankData>("ranks")

    @Suppress("unused")
    fun create(name: String): RankData {
        val exists = exists(name)

        if(exists) throw Exception("Rank names must be unique.")

        val rank = RankData(name, name, listOf(), null)

        col.insertOne(rank)

        return rank
    }

    @Suppress("unused")
    fun delete(name: String) {
        if (!exists(name))
            throw NotFoundException("No rank was found with the name of $name")

        val data = col.find(Filters.eq(RankData::name.name, name)).first()
        delete(UUID.fromString(data.id))
    }

    @Suppress("unused")
    fun delete(id: UUID) {
        if (!exists(id))
            throw NotFoundException("Rank with ID '$id' does not exist.")

        for (rank in list())
            if (rank.getData().parent == id.toString())
                rank.setParent(null)

        col.deleteOne(Filters.eq(RankData::id.name, id.toString()))
    }

    @Suppress("unused")
    fun get(id: UUID): AirbrushRank {
        return AirbrushRank(id)
    }

    @Suppress("unused")
    fun get(name: String): AirbrushRank {
        if(!exists(name)) throw NotFoundException("No rank was found with the name of $name")
        val data = col.find(Filters.eq(RankData::name.name, name)).first()
        return AirbrushRank(UUID.fromString(data.id))
    }

    fun list(): List<AirbrushRank> {
        return col.find().map { AirbrushRank(UUID.fromString(it.id)) }.toList()
    }

    @Suppress("unused")
    fun exists(name: String): Boolean {
        val data = col.find(Filters.eq(RankData::name.name, name)).firstOrNull()
        return data !== null
    }

    @Suppress("unused")
    fun exists(id: UUID): Boolean {
        val data = col.find(Filters.eq(RankData::id.name, id.toString())).firstOrNull()
        return data !== null
    }
}