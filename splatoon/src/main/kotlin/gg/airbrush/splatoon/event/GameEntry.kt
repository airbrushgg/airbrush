package gg.airbrush.splatoon.event

import gg.airbrush.server.lib.mm
import gg.airbrush.splatoon.profile
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class GameEntry {
    private val joinGame = ItemStack.builder(Material.ENDER_EYE)
        .customName("<i:false>Splatoon".mm())
        .build()

    init {
        val events = MinecraftServer.getGlobalEventHandler()
        events.addListener(PlayerSpawnEvent::class.java, ::onPlayerJoin)
        events.addListener(PlayerDisconnectEvent::class.java, ::onPlayerQuit)
        events.addListener(PlayerUseItemEvent::class.java, ::onUseItem)
    }

    private fun onPlayerJoin(event: PlayerSpawnEvent) {
        val player = event.player

        if (!player.hasPermission("admin.splatoon"))
            return // Temporary whitelist, assuming Splatoon won't be done in time for testing

        player.inventory.setItemStack(8, joinGame)
    }

    private fun onPlayerQuit(event: PlayerDisconnectEvent) {
        event.player.profile.isInGame = false
    }

    private fun onUseItem(event: PlayerUseItemEvent) {
        val player = event.player

        if (event.itemStack != joinGame)
            return

        player.inventory.clear()
        player.profile.isInGame = true
    }
}