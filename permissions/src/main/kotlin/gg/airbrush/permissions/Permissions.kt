package gg.airbrush.permissions

import gg.airbrush.permissions.commands.PermissionManager
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.ranks.AirbrushRank
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.permission.Permission
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.parser.SNBTParser
import java.util.*

class Permissions : Plugin() {
    override fun setup() {
        val commandManager = MinecraftServer.getCommandManager()
        commandManager.register(PermissionManager())

        val eventManager = MinecraftServer.getGlobalEventHandler()
        eventManager.addListener(PlayerLoginEvent::class.java) { event ->
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
    }

    private fun addPermissions(player: Player, rank: AirbrushRank) {
        val parent = rank.getParent()
        val data = rank.getData()

        if (parent != null)
            addPermissions(player, parent)

        for (permissionData in data.permissions) {
            val nbt = SNBTParser((permissionData.value ?: "{}").reader()).parse()

            if (nbt !is NBTCompound) {
                println("Found invalid NBT for permission '${permissionData.key}' in rank '${data.name}'")
                continue
            }

            val permission = Permission(permissionData.key, nbt)

            if (player.hasPermission(permissionData.key))
                player.removePermission(permissionData.key) // Override permission

            player.addPermission(permission)
        }
    }
}