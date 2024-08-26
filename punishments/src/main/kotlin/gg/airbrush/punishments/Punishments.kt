package gg.airbrush.punishments

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.airbrush.core.lib.setInterval
import gg.airbrush.punishments.commands.PunishCommand
import gg.airbrush.punishments.commands.PunishmentsCommand
import gg.airbrush.punishments.commands.RevertPunishmentCommand
import gg.airbrush.punishments.events.*
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.MinecraftServer
import java.time.Instant

data class Punishment(
	val reason: String,
	val action: String,
	val duration: String?
)

data class PunishmentsConfig(
	val hate: Punishment,
	val flood: Punishment,
	val filter: Punishment,
	val nsfw: Punishment,
	val ad: Punishment,
	val grief: Punishment,
	val arguing: Punishment,
	val dox: Punishment,
	val death: Punishment,
	val under13: Punishment
)

lateinit var punishmentConfig: PunishmentsConfig

class Punishments : Plugin() {
	private val mapper = tomlMapper {}

    override fun setup() {
        // On start
	    val configPath = ConfigUtils.loadResource(
		    clazz = Punishments::class.java,
		    fileName = "punishments.toml",
		    pluginInfo = this.info
	    )
	    punishmentConfig = mapper.decode(configPath)

	    val manager = MinecraftServer.getCommandManager()
	    manager.register(PunishCommand())
	    manager.register(PunishmentsCommand())
	    manager.register(RevertPunishmentCommand())

	    PlayerEvents()

	    // Every minute, check for punishment expiry
	    setInterval(60 * 1000) {
		    val now = Instant.now().epochSecond

		    val allPunishments = SDK.punishments.getAll().filter { it.data.duration !== null && it.data.active }
		    allPunishments.forEach {
				val duration = it.data.duration ?: return@forEach
			    val expiry = duration + it.getCreatedAt().epochSecond

			    if(expiry < now) {
				    println("[Punishments] Punishment expired, ID: ${it.data.id}.")
				    it.setActive(false)
				}
		    }
	    }
    }

    override fun teardown() {
        // On shutdown
    }
}