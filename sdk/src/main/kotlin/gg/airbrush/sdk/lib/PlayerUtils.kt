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