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

package gg.airbrush.core.lib

import gg.airbrush.core.events.sidebars
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.capitalize
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.color.Color
import net.minestom.server.entity.Player
import net.minestom.server.tag.Tag

fun Player.getXPThreshold(): Int {
    val sdkPlayer = SDK.players.get(uuid)
    return (sdkPlayer.getLevel() % 100 + 1) * 25
}

fun Player.updatePlayerListInfo() {
    val sdkPlayer = SDK.players.get(uuid)
    // Set the player's display name in the tab list.
    val rankData = sdkPlayer.getRank().getData()
    val levelColor = TextColor.color(ColorUtil.oscillateHSV(Color(0xff0000), Color(0xff00ff), level))
    val displayNameComponent = Component.text { builder ->
        builder.append(Component.text("[$level]", levelColor))
        builder.appendSpace()
        if (rankData.prefix.isNotEmpty()) {
            builder.append("<s>${rankData.prefix} ${this.username}".mm())
        } else builder.append("<p>${this.username}".mm())
    }
    this.displayName = displayNameComponent
}

fun Player.teleportToCanvas(canvasUUID: String) {
    val instance = CanvasManager.get(canvasUUID)
    if (instance == null) {
        this.sendMessage("<error>A problem occurred teleporting to this world!".mm())
        return
    }

    if(this.instance.uniqueId == instance.uniqueId) {
        this.sendMessage("<error>You are already in this world!".mm())
        return
    }

    sidebars[this.uuid]?.updateLineContent("world", getWorldLine(this))
    this.setInstance(instance)

    if(this.openInventory != null) this.closeInventory()
}

fun Player.teleportToSpawn() {
    if(this.openInventory != null) this.closeInventory()
    this.instance = WorldManager.defaultInstance
}

fun Player.getCurrentWorldName(): String {
    val instance = this.instance

    val internalWorldName = instance.getTag(Tag.String("PersistentWorld")) ?: null
    val canvasId = instance.getTag(Tag.String("CanvasUUID")) ?: null

    if (internalWorldName == null && canvasId == null) return "Spawn"

    if (canvasId != null) {
        val world = SDK.worlds.getByUUID(canvasId) ?: return "Unknown World"
        return world.data.name
    }

    if(internalWorldName != null) {
        if(internalWorldName.equals("star_world", true)) return "<donator><b>⭐"
        return internalWorldName.replace("_", " ").capitalize()
    }

    return "Unknown World"
}

fun Player.getCurrentWorldID(): String {
    val instance = this.instance

    val internalWorldName = instance.getTag(Tag.String("PersistentWorld")) ?: null
    val canvasId = instance.getTag(Tag.String("CanvasUUID")) ?: null

    if (internalWorldName == null && canvasId == null) return "spawn_world"

    if (canvasId != null) {
        val world = SDK.worlds.getByUUID(canvasId) ?: return "unknown_world"
        return world.data.id
    }

    if(internalWorldName != null) return internalWorldName

    return "unknown_world"
}