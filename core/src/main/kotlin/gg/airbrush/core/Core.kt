package gg.airbrush.core

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
import gg.airbrush.core.filter.ChatFilter
import gg.airbrush.core.lib.CanvasManager
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.builder.Command
import net.minestom.server.timer.TaskSchedule

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
			Boost(),
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
		    Ad(),
			MakeMeTiny()
	    )

	    // On start
        registerCommands()
        registerEvents()

        ChatFilter.initialize(info)

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

		// Save all canvases to disk.
		CanvasManager.saveAll()
		// Update the active boosters for next time to server starts.
		Boost.clearActiveBoosters()
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