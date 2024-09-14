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

package gg.airbrush.sdk.classes.whitelist

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.FindIterable
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.config
import java.util.UUID

class Whitelist {
    data class WhitelistData(val uuid: String, val addedAt: Long = System.currentTimeMillis())

    private val db = Database.get()
    private val col = db.getCollection<WhitelistData>("whitelist")

    fun add(uuid: UUID): WhitelistData {
        if(get(uuid) !== null) {
            throw Exception("Player already added to whitelist.")
        }

        val data = WhitelistData(uuid.toString())
        col.insertOne(data)
        return data
    }

    fun get(uuid: UUID): WhitelistData? {
        return col.find(Filters.eq(WhitelistData::uuid.name, uuid.toString())).firstOrNull()
    }

    fun list(): FindIterable<WhitelistData> {
        return col.find()
    }

	fun isEnabled(): Boolean {
		return config.whitelistEnabled
	}

    fun remove(uuid: UUID) {
        if(get(uuid) === null) {
            throw Exception("Player not added to whitelist.")
        }

        col.deleteOne(Filters.eq(WhitelistData::uuid.name, uuid.toString()))
    }
}