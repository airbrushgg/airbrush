/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2024 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.sdk.lib

import gg.airbrush.sdk.eventNode
import gg.airbrush.server.lib.mm
import net.minestom.server.entity.Player
import net.minestom.server.event.EventListener
import net.minestom.server.event.player.PlayerChatEvent

typealias PromptHandler = (String) -> Unit

fun isConfirmed (input: String): Boolean = input.equals("yes", true) || input.equals("y", true) || input.equals("confirm", true)

class Input {
    /**
     * The Player you want to fetch the input from.
     */
    var player: Player? = null
    /**
     * The handler for the input.
     */
    var handler: PromptHandler = {}

    /**
     * Waits for the player to input a message, and then calls the handler with the input.
     */
    fun prompt(): Input {
        lateinit var inputHandler: EventListener<PlayerChatEvent>
        inputHandler = EventListener.of(PlayerChatEvent::class.java) { event ->
            if (event.player != player) return@of

            event.isCancelled = true
            if(!event.message.equals("cancel", ignoreCase = true)) handler(event.message)
            else event.player.sendMessage("<s>Input cancelled.".mm())

            eventNode.removeListener(inputHandler)
        }

        eventNode.addListener(inputHandler)

        return this
    }
}

/**
 * Allows you to fetch an input from a player.
 *
 * @return An [Input] object.
 */
fun fetchInput(player: Player, handler: PromptHandler): Input {
    val input = Input()
    input.handler = handler
    input.player = player
    return input
}
