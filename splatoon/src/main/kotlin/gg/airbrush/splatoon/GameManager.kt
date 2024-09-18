package gg.airbrush.splatoon

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.scoreboard.Team
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

object GameManager {
    private val logger = LoggerFactory.getLogger(GameManager::class.java)

    private val blueTeam: Team = MinecraftServer.getTeamManager().createBuilder("blue")
        .collisionRule(TeamsPacket.CollisionRule.NEVER)
        .nameTagVisibility(TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS)
        .updateTeamPacket()
        .build()
    private val redTeam: Team = MinecraftServer.getTeamManager().createBuilder("red")
        .collisionRule(TeamsPacket.CollisionRule.NEVER)
        .nameTagVisibility(TeamsPacket.NameTagVisibility.HIDE_FOR_OTHER_TEAMS)
        .updateTeamPacket()
        .build()

    val parameters = GameParameters()
    private val queuedPlayers = ArrayList<Player>()
    private val activePlayers = ArrayList<Player>()
    private val matchInProgress = AtomicBoolean(false)

    fun start() {
        // Check if there are enough players to start a match.
        val onlineQueuedPlayers = queuedPlayers.filter { player -> player.isOnline }
        if (onlineQueuedPlayers.size < parameters.get(GameParameters.Parameter.MIN_PLAYERS)) {
            logger.info("Not enough players to start a match.")
            return
        }

        if (activePlayers.size > 0) {
            activePlayers.forEach { player -> player.team = null }
            activePlayers.clear()
        }

        // Check if a match is already in progress.
        if (!matchInProgress.compareAndSet(false, true)) {
            return
        }

        // Assign each player to a random team.
        queuedPlayers.shuffled().forEachIndexed { index, player ->
            activePlayers.add(player)
            player.team = if (index % 2 == 0) blueTeam else redTeam
        }
        queuedPlayers.clear()

        // TODO: Load the splatoon map, and teleport players to their respective spawn points.
    }

    fun stop() {
        // Check if a match is in progress.
        if (!matchInProgress.compareAndSet(true, false)) {
            return
        }

        activePlayers.forEach { player -> player.team = null }
        activePlayers.clear()

        // TODO: Teleport players back to the spawn world, then unload the map.
    }

    fun addPlayerToQueue(player: Player) {
        queuedPlayers.add(player)
    }
}