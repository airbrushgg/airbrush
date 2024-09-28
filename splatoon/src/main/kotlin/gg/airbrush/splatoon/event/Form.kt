package gg.airbrush.splatoon.event

import gg.airbrush.splatoon.lib.sendParticle
import gg.airbrush.splatoon.profile
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.event.player.PlayerStartSneakingEvent
import net.minestom.server.event.player.PlayerStopSneakingEvent
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.particle.Particle
import net.minestom.server.scoreboard.Team
import net.minestom.server.timer.TaskSchedule
import kotlin.math.abs

class Form {
    private val speed = Attribute.fromNamespaceId("minecraft:generic.movement_speed")!!
    private val stepHeight = Attribute.fromNamespaceId("minecraft:generic.step_height")!!
    private val noCollision: Team = MinecraftServer.getTeamManager()
        .createTeam("Collision")
        .apply {
            collisionRule = TeamsPacket.CollisionRule.NEVER
        }

    init {
        val connections = MinecraftServer.getConnectionManager()
        val scheduler = MinecraftServer.getSchedulerManager()
        val events = MinecraftServer.getGlobalEventHandler()

        val particle = (Particle.fromNamespaceId("minecraft:block")!! as Particle.Block)

        scheduler.submitTask {
            for (player in connections.onlinePlayers) {
                val velocity = player.previousPosition.sub(player.position)
                if (player.profile.isInGame && player.isSneaking && player.isOnGround && (abs(velocity.x) >= 0.1 || abs(velocity.z) >= 0.1)) {
                    val particle = particle.withBlock(player.position.)

                    connections.onlinePlayers.toList().sendParticle(ParticlePacket(
                            particle,
                            player.position,
                            Vec(0.0, 0.0, 0.0),
                            5f,
                            2
                    ))
                }
            }

            TaskSchedule.nextTick()
        }

        events.addListener(PlayerStopSneakingEvent::class.java, ::onEndSneak)
        events.addListener(PlayerStartSneakingEvent::class.java, ::onSneak)
    }

    private fun onSneak(event: PlayerStartSneakingEvent) {
        val player = event.player

        if (!player.profile.isInGame)
            return

        player.getAttribute(stepHeight).baseValue = 1.0
        player.getAttribute(speed).baseValue *= 3.5
        player.isInvisible = true
        player.team = noCollision
    }

    private fun onEndSneak(event: PlayerStopSneakingEvent) {
        val player = event.player

        if (!player.profile.isInGame)
            return

        player.getAttribute(stepHeight).baseValue = 0.6
        player.getAttribute(speed).baseValue /= 3.5
        player.isInvisible = false
        player.team = null
    }
}