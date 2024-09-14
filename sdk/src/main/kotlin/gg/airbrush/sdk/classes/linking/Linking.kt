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

package gg.airbrush.sdk.classes.linking

import com.mongodb.client.model.Filters
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.SDK
import java.util.*

class Linking {
    data class LinkData(
        var code: String,
        val player: String,
        val discordId: Long,
    )

    private val db = Database.get()
    private val col = db.getCollection<LinkData>("linking")

    @Suppress("private")
    fun getSession(sessionId: UUID): LinkData? {
        return col.find(Filters.eq(LinkData::code.name, sessionId.toString())).firstOrNull()
    }

    fun createSession(player: UUID, discordId: Long): LinkData {
        val playerUUID = player.toString()

        // we're not using getSession since this uses the players name,
        // rather than a session id
        val currentSession = col.find(Filters.eq(LinkData::player.name, playerUUID)).firstOrNull()

        if(currentSession !== null) throw Exception("$playerUUID already has an existing linking session")

        val linkData = LinkData(
            code = UUID.randomUUID().toString(),
            player = playerUUID,
			discordId
        )

        col.insertOne(linkData)

        return linkData
    }

    fun verifySession(sessionId: UUID) {
        val session = getSession(sessionId)
            ?: throw Exception("$sessionId is not a valid linking session")

	    col.deleteOne(Filters.eq(LinkData::code.name, sessionId.toString()))

		val player = SDK.players.get(UUID.fromString(session.player))

	    player.setDiscordId(session.discordId)
    }
}