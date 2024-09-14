

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

package gg.airbrush.core.events

import gg.airbrush.core.lib.*
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.PaletteType
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.scoreboard.Sidebar
import java.util.*

private val SIDEBAR_SPACER = "".mm()
val sidebars = mutableMapOf<UUID, Sidebar>()

class PlayerLogin {
    init {
        val eventHandler = MinecraftServer.getGlobalEventHandler()
        eventHandler.addListener(
            PlayerSpawnEvent::class.java
        ) { event: PlayerSpawnEvent -> execute(event) }

        eventHandler.addListener(
            PlayerDisconnectEvent::class.java
        ) { event: PlayerDisconnectEvent -> executeDisconnect(event) }

        eventHandler.addListener(
            AsyncPlayerPreLoginEvent::class.java
        ) { event: AsyncPlayerPreLoginEvent -> executePreLogin(event) }

        eventHandler.addListener(
            PlayerSpawnEvent::class.java
        ) { event: PlayerSpawnEvent -> executeSpawn(event) }
    }

    private fun execute(event: PlayerSpawnEvent) {
        val player = event.player

        val playerExists = SDK.players.exists(player.uuid)
        if (!playerExists) SDK.players.create(player.uuid)

        val sdkPlayer = SDK.players.get(player.uuid)
        val currentPaletteType = PaletteType.entries[sdkPlayer.getData().palette]

        val paletteSelector = ItemStack.builder(PaletteUtil.getIconBlock(currentPaletteType))
            .customName(Constants.paletteSelectorName)
            .set(Constants.airbrushToolTag, Constants.PALETTE_TOOL)
            .build()

        val airbrush = ItemStack.builder(Material.FEATHER)
            .customName(Constants.paintbrushName)
            .set(Constants.airbrushToolTag, Constants.PAINTBRUSH_TOOL)
            .build()

        val compass = ItemStack.builder(Material.COMPASS)
            .customName(Constants.mainMenuName)
            .set(Constants.airbrushToolTag, Constants.MAIN_MENU_TOOL)
            .build()

        val eraser = ItemStack.builder(Material.SUGAR)
            .customName(Constants.eraserName)
            .set(Constants.airbrushToolTag, Constants.ERASER_TOOL)
            .build()

	    val eyedropper = ItemStack.builder(Material.ARROW)
		    .customName(Constants.eyedropperName)
            .set(Constants.airbrushToolTag, Constants.EYEDROPPER_TOOL)
		    .build()

	    player.inventory.setItemStack(2, eyedropper)
        player.inventory.setItemStack(3, paletteSelector)
        player.inventory.setItemStack(4, airbrush)
        player.inventory.setItemStack(5, compass)
        player.inventory.setItemStack(6, eraser)

        val prefix = sdkPlayer.getRank().getData().prefix
        if (prefix.isNotEmpty()) {
            player.customName = "$prefix ${player.username}".mm()
        }

        player.isAllowFlying = true

        val joinInfo = Translations.translate("core.welcome", player.username)
        player.sendMessage(joinInfo.mm())

	    val joinNotification = Translations.translate("core.notifications.join", player.username)

        MinecraftServer.getConnectionManager().onlinePlayers.forEach {
            it.sendMessage(joinNotification.mm())
        }
    }

    private fun executeDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player

        val sdkPlayer = SDK.players.get(player.uuid)
        sdkPlayer.addPlaytime(PlayerDataCache.getSessionTime(player.uuid))

	    val leaveNotification = Translations.translate("core.notifications.leave", player.username)

	    MinecraftServer.getConnectionManager().onlinePlayers.forEach {
		    it.sendMessage(leaveNotification.mm())
	    }
    }

    private fun executePreLogin(event: AsyncPlayerPreLoginEvent) {
		if(!SDK.whitelist.isEnabled()) return
        val whitelistStatus = SDK.whitelist.get(event.playerUuid)
        if (whitelistStatus == null) {
            event.player.kick(Translations.translate("core.whitelist").mm())
        }
    }

    private fun executeSpawn(event: PlayerSpawnEvent) {
        val player = event.player
        val sdkPlayer = SDK.players.get(player.uuid)

        PlayerDataCache.populateCache(player.uuid)

        val xpThreshold = player.getXPThreshold()
        player.level = sdkPlayer.getLevel()
        player.exp = sdkPlayer.getExperience().toFloat() / xpThreshold

        if (player.uuid !in sidebars) {
            val sidebar = Sidebar(Translations.translate("core.scoreboard.title").mm())
            sidebar.createLine(Sidebar.ScoreboardLine("level", getLevelLine(player), 7))
            sidebar.createLine(Sidebar.ScoreboardLine("blocks", getBlocksLine(player), 6))
            sidebar.createLine(Sidebar.ScoreboardLine("xp", getXPLine(player), 5))
            sidebar.createLine(Sidebar.ScoreboardLine("spacer_1", SIDEBAR_SPACER, 4))
            sidebar.createLine(Sidebar.ScoreboardLine("rank", getRankLine(player), 3))
            //sidebar.createLine(Sidebar.ScoreboardLine("playtime", getPlaytimeLine(player), 2)) // todo: track playtime
            sidebar.createLine(Sidebar.ScoreboardLine("spacer_2", SIDEBAR_SPACER, 1))
            sidebar.createLine(Sidebar.ScoreboardLine("world", getWorldLine(player), 0))
            sidebar.addViewer(player)

            sidebars[player.uuid] = sidebar
        }

        val sidebar = sidebars[player.uuid] ?: return

        if (player !in sidebar.viewers)
            sidebar.addViewer(player)
    }
}