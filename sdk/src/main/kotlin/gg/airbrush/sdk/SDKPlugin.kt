package gg.airbrush.sdk

import gg.airbrush.sdk.commands.ReloadCommand
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.plugins.Plugin
import net.minestom.server.MinecraftServer

class SDKPlugin : Plugin() {
    override fun setup() {
	    val manager = MinecraftServer.getCommandManager()
	    manager.register(ReloadCommand())
	    // Triggers an initialization to create the language files
	    Translations.reload()
	}
    override fun teardown() {}
}