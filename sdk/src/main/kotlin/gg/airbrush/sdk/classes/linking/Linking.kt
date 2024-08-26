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

    @Suppress("unused")
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

    @Suppress("unused")
    fun verifySession(sessionId: UUID) {
        val session = getSession(sessionId)
            ?: throw Exception("$sessionId is not a valid linking session")

	    col.deleteOne(Filters.eq(LinkData::code.name, sessionId.toString()))

		val player = SDK.players.get(UUID.fromString(session.player))

	    player.setDiscordId(session.discordId)
    }
}