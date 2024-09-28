package gg.airbrush.splatoon

import net.minestom.server.entity.Player

private val profiles = mutableMapOf<Player, Profile>()

class Profile(val player: Player) {
    var isInGame = false
}

val Player.profile
    get() = profiles.computeIfAbsent(this) { Profile(it) }