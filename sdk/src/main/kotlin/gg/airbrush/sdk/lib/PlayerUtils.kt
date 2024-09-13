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

package gg.airbrush.sdk.lib

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

object PlayerUtils {
    @Serializable
    private data class MojangResponse(
        val id: String,
        val name: String
    )

    private const val UUID_URL = "https://api.mojang.com/users/profiles/minecraft"
    private const val NAME_URL = "https://api.mojang.com/user/profile"
    private val REGEX = Regex("([a-f0-9]{8})([a-f0-9]{4})([a-f0-9]{4})([a-f0-9]{4})([a-f0-9]{12})")

    @Suppress("unused")
    fun getUUID(name: String): UUID {
        val lower = name.lowercase()
        val response = HTTP.get("$UUID_URL/$lower")
        val body = Json.decodeFromString(response.body()) as MojangResponse
        return formatUUID(body.id)
    }

    @Suppress("unused")
    fun getName(uuid: UUID): String {
        val response = HTTP.get("$NAME_URL/$uuid")
        val body = Json.decodeFromString(response.body()) as MojangResponse
        return body.name
    }

    private fun formatUUID(uuid: String): UUID {
        val id = REGEX
            .find(uuid)!!
            .groups
            .filter { it!!.value.length != 32 }
            .joinToString("-") { it!!.value }

        return UUID.fromString(id)
    }
}