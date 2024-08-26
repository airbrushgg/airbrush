package gg.airbrush.sdk.classes.boosters

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.lib.ConfigUtils
import java.util.UUID

class Boosters {
    private val db = Database.get()
    private val col = db.getCollection<ActiveBooster>("boosters")

    private val config: BoosterConfig

    init {
        val mapper = tomlMapper {
            mapping<BoosterConfig>("booster" to "boosters")
        }
        val configPath = ConfigUtils.loadResource(
            clazz = BoosterConfig::class.java,
            fileName = "boosters.toml",
            folder = "sdk"
        )
        config = mapper.decode(configPath)
    }

    @Suppress("unused")
    fun create(data: BoosterData): ActiveBooster {
        val booster = ActiveBooster(
            id = UUID.randomUUID().toString(),
            data = data,
            startedAt = System.currentTimeMillis(),
            duration = data.duration
        )
        return booster.also { col.insertOne(it) }
    }

    @Suppress("unused")
    fun get(id: UUID): ActiveBooster? {
        return col
            .find(Filters.eq(ActiveBooster::id.name, id.toString()))
            .firstOrNull()
    }

    fun updateOrClear(id: UUID) {
        val booster = get(id) ?: return
        if (!booster.hasCompleted()) {
            val now = System.currentTimeMillis()
            val timeLeft = booster.startedAt + (booster.duration * 1000) - now
            col.updateOne(
                Filters.eq(ActiveBooster::id.name, id.toString()),
                listOf(
                    Updates.set(ActiveBooster::duration.name, timeLeft / 1000),
                    Updates.set(ActiveBooster::startedAt.name, now)
                )
            )
        } else {
            clear(id)
        }
    }

    @Suppress("unused")
    fun clear(id: UUID) {
        col.deleteOne(Filters.eq(ActiveBooster::id.name, id.toString()))
    }

    @Suppress("unused")
    fun getActiveBoosters(): List<ActiveBooster> {
        return col.find().toList()
    }

    @Suppress("unused")
    fun getAvailableBoosters(): List<BoosterData> {
        return config.boosters.orEmpty()
    }
}