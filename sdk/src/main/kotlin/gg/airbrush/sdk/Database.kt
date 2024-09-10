package gg.airbrush.sdk

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoDatabase
import net.minestom.server.MinecraftServer
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries

object Database {
    private val conn: MongoClient
    private val db: MongoDatabase
    private var connected: Boolean = false

    init {
        MinecraftServer.LOGGER.info("[SDK] Connecting to database.")

        try {
	        conn = MongoClient.create(config.database)
		} catch (ex: Exception) {
			throw ex
		}

        connected = true

        val codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(UuidCodec(UuidRepresentation.STANDARD)),
            MongoClientSettings.getDefaultCodecRegistry()
        )

	    // note: we could just use this to separate production and development but,
	    // we use seperate ones to make sure latency is as small as it can be.
	    // the dev db is hosted on the same machine, whereas the production db is hosted on atlas c:
        db = conn.getDatabase("Airbrush")
            .withCodecRegistry(codecRegistry)
    }

    fun close() {
        if(!connected) return
        conn.close()
    }

    fun load() {}

    fun get() = db
}