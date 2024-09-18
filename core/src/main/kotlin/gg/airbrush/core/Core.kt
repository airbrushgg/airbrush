


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

package gg.airbrush.core

import Ad
import Boost
import gg.airbrush.core.commands.*
import gg.airbrush.core.commands.admin.*
import gg.airbrush.core.commands.history.History
import gg.airbrush.core.commands.history.Rollback
import gg.airbrush.server.plugins.Plugin
import gg.airbrush.core.commands.mainmenu.MainMenu
import gg.airbrush.core.commands.mainmenu.SelectWorld
import gg.airbrush.core.events.PlayerLogin
import gg.airbrush.core.events.BrushEvents
import gg.airbrush.core.events.PlayerChat
import gg.airbrush.core.events.PlayerBlockHandler
import gg.airbrush.core.events.PlayerLevelUp
import gg.airbrush.core.lib.CanvasManager
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.builder.Command
import net.minestom.server.event.EventNode
import net.minestom.server.timer.TaskSchedule

internal val eventNode = EventNode.all("Core")

class Core : Plugin() {
	private var commands: List<Command> = listOf()

    override fun setup() {
	    commands = listOf(
		    Whitelist(),
		    MainMenu(),
		    Create(),
		    Canvas(),
		    Discord(),
		    SelectWorld(),
		    Gamemode(),
		    History(),
		    TestColors(),
		    Palette(),
		    Filter(),
		    Hardware(),
		    SetRadius(),
			ClearChat(),
			StaffChat(),
			Vanish(),
			Lockdown(),
			Reset(),
			AddBooster(),
			Tpa(),
			TpAccept(),
			TpDeny(),
			Rules(),
			Stats(),
		    Pronouns(),
			Rollback(),
			Teleport(),
		    Mask(),
			MakeMeTiny(),
			Ad(),
			Boost()
	    )

	    // On start
		MinecraftServer.getGlobalEventHandler().addChild(eventNode)

        registerCommands()
        registerEvents()

		Boost.restoreActiveBoosters()

		MinecraftServer.LOGGER.info("[Core] Loaded!")

	    MinecraftServer.getSchedulerManager().scheduleTask({
			val broadcasts = Translations.translate("core.broadcasts").trimIndent().split("\n")
		    val message = broadcasts.random()
		    Audiences.players().sendMessage(message.mm())
	    }, TaskSchedule.immediate(), TaskSchedule.minutes(3))
    }

    override fun teardown() {
	    val manager = MinecraftServer.getCommandManager()
	    commands.forEach {
		    manager.unregister(it)
	    }

		MinecraftServer.getGlobalEventHandler().removeChild(eventNode)

		try {
			// Save all canvases to disk.
			CanvasManager.saveAll()
		} catch (e: Exception) {
			MinecraftServer.LOGGER.error("[Core] Failed to save canvases.", e)
		}

		try {
			// Update the active boosters for next time to server starts.
			Boost.clearActiveBoosters()
		} catch (e: Exception) {
			MinecraftServer.LOGGER.error("[Core] Failed to clear active boosters.", e)
		}
    }

    private fun registerCommands() {
        val manager = MinecraftServer.getCommandManager()
	    commands.forEach {
			manager.register(it)
	    }
    }

    private fun registerEvents() {
        PlayerLogin()
        PlayerLevelUp()
        PlayerChat()
        BrushEvents()
        PlayerBlockHandler()
    }
}