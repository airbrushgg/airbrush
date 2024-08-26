package gg.airbrush.punishments.events

import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.sdk.SDK
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent

class PlayerEvents {
	init {
		val eventHandler = MinecraftServer.getGlobalEventHandler()

		eventHandler.addListener(
			AsyncPlayerPreLoginEvent::class.java
		) { event: AsyncPlayerPreLoginEvent -> executePreLogin(event) }
	}

	private fun executePreLogin(event: AsyncPlayerPreLoginEvent) {
		val punishments = SDK.punishments.list(event.player)
		val activeBan = punishments.find {
			it.data.active && it.data.type == PunishmentTypes.BAN.ordinal
		}

		if(activeBan !== null) {
			event.player.kick(activeBan.data.reason)
			return
		}
	}
}