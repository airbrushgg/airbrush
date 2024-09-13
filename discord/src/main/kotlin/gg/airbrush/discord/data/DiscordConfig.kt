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

package gg.airbrush.discord.data

data class DiscordConfig(
    val botToken: String,
    val channel: String,
    val donor: String,
    val superdonor: String,
    val join: MessageContent,
    val leave: MessageContent,
    val chat: MessageContent,
    val ingame: MessageContent,
	val linkRequest: String
)

data class MessageContent(
    val content: String
)
