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

    @Suppress("unused")
    fun add(uuid: UUID): WhitelistData? {
        if(get(uuid) !== null) {
            throw Exception("Player already added to whitelist.")
        }

        val data = WhitelistData(uuid.toString())
        col.insertOne(data)
        return data
    }

    @Suppress("unused")
    fun get(uuid: UUID): WhitelistData? {
        return col.find(Filters.eq(WhitelistData::uuid.name, uuid.toString())).firstOrNull()
    }

    @Suppress("unused")
    fun list(): FindIterable<WhitelistData> {
        return col.find()
    }

	fun isEnabled(): Boolean {
		return config.whitelistEnabled
	}

    @Suppress("unused")
    fun remove(uuid: UUID) {
        if(get(uuid) === null) {
            throw Exception("Player not added to whitelist.")
        }

        col.deleteOne(Filters.eq(WhitelistData::uuid.name, uuid.toString()))
    }
}