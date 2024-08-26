package gg.airbrush.worlds

import gg.airbrush.server.plugins.Plugin
import gg.airbrush.worlds.listener.GlobalEventListeners
import net.minestom.server.MinecraftServer

class Worlds : Plugin() {
    override fun setup() {
        WorldManager.initialize()
        WorldManager.loadDefaultInstance()

        MinecraftServer.getSchedulerManager().buildShutdownTask {
            WorldManager.defaultInstance.saveChunksToStorage().join()
            WorldManager.dispose()
        }

        // Register events
        GlobalEventListeners()
    }

    override fun teardown() {
    }
}