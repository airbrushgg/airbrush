/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2023 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.punishments.events

import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.punishments.eventNode
import gg.airbrush.sdk.SDK
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent

class PlayerEvents {
	init {
		eventNode.addListener(
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