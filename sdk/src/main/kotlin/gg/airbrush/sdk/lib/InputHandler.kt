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
import net.minestom.server.entity.Player
import net.minestom.server.event.EventListener
import net.minestom.server.event.player.PlayerChatEvent

typealias PromptHandler = (String) -> Unit

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
    fun prompt() {
        lateinit var inputHandler: EventListener<PlayerChatEvent>
        inputHandler = EventListener.of(PlayerChatEvent::class.java) { event ->
            if (event.player == player) {
                event.isCancelled = true
                handler(event.message)
                eventNode.removeListener(inputHandler)
            }
        }

        eventNode.addListener(inputHandler)
    }
}

/**
 * Allows you to fetch an input from a player.
 *
 * @return An [Input] object.
 */
fun fetchInput(init: Input.() -> Unit): Input {
    val input = Input()
    input.init()
    return input
}
