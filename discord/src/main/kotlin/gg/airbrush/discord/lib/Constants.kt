/*
 * This file is part of Airbrush
 *
 * Copyright (c) Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.discord.lib

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

fun generateNoPermissionMessage(permission: String): MessageEmbed {
    return EmbedBuilder()
        .setTitle("No permission")
        .setColor(Color.decode("#ff6e6e"))
        .setDescription("This command requires the `$permission` permission.")
        .build()
}