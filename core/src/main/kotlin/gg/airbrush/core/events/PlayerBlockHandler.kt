package gg.airbrush.core.events

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent

class PlayerBlockHandler {
    init {
        val eventHandler = MinecraftServer.getGlobalEventHandler()

        eventHandler.addListener(
            PlayerBlockBreakEvent::class.java
        ) { event: PlayerBlockBreakEvent ->
            event.isCancelled = event.player.gameMode !== GameMode.CREATIVE
        }

        eventHandler.addListener(
            PlayerBlockPlaceEvent::class.java
        ) { event: PlayerBlockPlaceEvent ->
            event.isCancelled = event.player.gameMode !== GameMode.CREATIVE
        }
    }

}