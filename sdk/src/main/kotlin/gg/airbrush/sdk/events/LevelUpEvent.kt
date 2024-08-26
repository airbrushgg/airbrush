package gg.airbrush.sdk.events

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/**
 * Called when a player levels up through block painting. This is unrelated to Minecraft's experience system.
 */
class LevelUpEvent(private val player: Player, val level: Int) : PlayerEvent {
    override fun getPlayer(): Player = player
}