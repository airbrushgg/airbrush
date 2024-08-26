package gg.airbrush.sdk.events

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.PlayerEvent

/**
 * Called when a player successfully links their Discord account.
 */
class LinkAccountEvent(private val player: Player, private val discordAccount: Long) : PlayerEvent {
	override fun getPlayer(): Player = player
	fun getDiscordID(): Long = discordAccount
}