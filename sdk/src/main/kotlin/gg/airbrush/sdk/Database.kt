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

        db = conn.getDatabase(if(config.isDev) "AirbrushDev" else "Airbrush")
            .withCodecRegistry(codecRegistry)
    }

    fun close() {
        if(!connected) return
        conn.close()
    }

    fun get() = db
}