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

package gg.airbrush.permissions

import gg.airbrush.permissions.commands.PermissionManager
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.ranks.AirbrushRank
import gg.airbrush.server.plugins.Plugin
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.permission.Permission
import java.util.*

internal val eventNode = EventNode.all("Permissions")

class Permissions : Plugin() {
    override fun setup() {
        val commandManager = MinecraftServer.getCommandManager()
        commandManager.register(PermissionManager())

        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
        eventNode.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            val player = event.player

            if (!SDK.players.exists(player.uuid))
                return@addListener

            val playerData = SDK.players.get(player.uuid)

            if (!SDK.ranks.exists(UUID.fromString(playerData.getData().rank)))
                return@addListener

            val rank = playerData.getRank()
            addPermissions(player, rank)
        }
    }

    override fun teardown() {
        // On shutdown
        MinecraftServer.getGlobalEventHandler().removeChild(eventNode)
    }

    private fun addPermissions(player: Player, rank: AirbrushRank) {
        val parent = rank.getParent()
        val data = rank.getData()

        if (parent != null)
            addPermissions(player, parent)

        for (permissionData in data.permissions) {
            val nbt = TagStringIO.builder().build().asCompound(permissionData.value ?: "{}")

            if (nbt !is CompoundBinaryTag) {
                MinecraftServer.LOGGER.info("Found invalid NBT for permission '${permissionData.key}' in rank '${data.name}'")
                continue
            }

            val permission = Permission(permissionData.key, nbt)

            if (player.hasPermission(permissionData.key))
                player.removePermission(permissionData.key) // Override permission

            player.addPermission(permission)
        }
    }
}