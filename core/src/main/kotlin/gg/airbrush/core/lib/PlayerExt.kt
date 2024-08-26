package gg.airbrush.core.lib

import gg.airbrush.core.events.sidebars
import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.minestom.server.entity.Player

fun Player.getXPThreshold(): Int {
    val sdkPlayer = SDK.players.get(uuid)
    return (sdkPlayer.getLevel() % 100 + 1) * 25
}

fun Player.teleportToCanvas(canvasUUID: String) {
    val instance = CanvasManager.get(canvasUUID)
    if (instance == null) {
        sendMessage("<error>A problem occurred teleporting to this world!".mm())
        return
    }
    sidebars[uuid]?.updateLineContent("world", getWorldLine(this))
    setInstance(instance)
}

fun Player.teleportToSpawn() {
    this.instance = WorldManager.defaultInstance
}