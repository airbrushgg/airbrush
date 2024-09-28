package gg.airbrush.splatoon.lib

import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.ParticlePacket

fun List<Player>.sendParticle(packet: ParticlePacket) {
    for (player in this)
        player.sendPacket(packet)
}