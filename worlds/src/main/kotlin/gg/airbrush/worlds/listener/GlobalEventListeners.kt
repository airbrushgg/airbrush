package gg.airbrush.worlds.listener

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.PlayerSpawnEvent

class GlobalEventListeners {

    init {
        val eventHandler = MinecraftServer.getGlobalEventHandler()
        //eventHandler.addListener(PlayerLoginEvent::class.java, this::handlePlayerLogin)
        eventHandler.addListener(PlayerSpawnEvent::class.java, this::handlePlayerSpawn)
    }

    private fun handlePlayerSpawn(event: PlayerSpawnEvent) {
        // TODO: Allow user to define spawn point in worlds.toml
        val player = event.player
        player.teleport(Pos(0.0, 4.0, 0.0))
    }
}