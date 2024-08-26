package gg.airbrush.sdk.classes.players

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.Updates
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.classes.palettes.Palettes
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
import java.util.UUID

class Players {
    private val db = Database.get()
    private val col = db.getCollection<PlayerData>("players")

    @Suppress("unused")
    fun get(uuid: UUID): AirbrushPlayer {
        // OPTIMIZATION: Cache the player data so that we don't have to query the database each time.
        return playerCache.computeIfAbsent(uuid, ::AirbrushPlayer)
    }

    @Suppress("unused")
    fun exists(uuid: UUID): Boolean {
        val data = col.find(Filters.eq(PlayerData::uuid.name, uuid.toString())).firstOrNull()
        return data !== null
    }

	@Suppress("unused")
	fun getByDiscordID(id: String): PlayerData? {
		return col.find(Filters.eq(PlayerData::discordId.name, id.toLong())).firstOrNull()
	}

	@Suppress("unused")
    fun create(uuid: UUID): PlayerData {
        val defaultRank = SDK.ranks.get("Default")

        val paletteProgression = mutableListOf<ProgressionData>()

        PaletteType.entries.forEach {
            val index = if(it == PaletteType.CONCRETE)
                Palettes().get(PaletteType.CONCRETE).size
            else 1
            paletteProgression.add(ProgressionData(it.ordinal, index))
        }

        val player = PlayerData(
            uuid = uuid.toString(),
            firstJoin = System.currentTimeMillis(),
            rank = defaultRank.getData().id,
            paletteProgression = paletteProgression,
            ownedBoosters = emptyList(),
        )

        col.insertOne(player)

        return player
    }

    @Suppress("unused")
    fun updateBlockCounts(counts: HashMap<UUID, Int>) = runBlocking<Unit> {
        launch {
            col.bulkWrite(counts.map { (uuid, delta) ->
                UpdateOneModel(
                    Filters.eq(PlayerData::uuid.name, uuid.toString()),
                    Updates.inc(PlayerData::blockCount.name, delta)
                )
            })
        }
    }

    companion object {
        private val playerCache = HashMap<UUID, AirbrushPlayer>()

        init {
            val eventNode = EventNode.type("Player Cache", EventFilter.PLAYER)
            eventNode.addListener(PlayerDisconnectEvent::class.java) { event ->
                // Remove the player from the cache so that we can refresh their data when they join again.
                playerCache.remove(event.player.uuid)
            }

            MinecraftServer.getGlobalEventHandler().addChild(eventNode)
        }
    }
}