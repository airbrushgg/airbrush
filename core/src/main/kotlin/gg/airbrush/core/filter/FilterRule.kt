package gg.airbrush.core.filter

import net.minestom.server.entity.Player

interface FilterRule {
    /**
     * Apply a filter rule.
     * @return Return true if the message should be blocked. Otherwise, return false.
     */
    fun apply(player: Player, message: String): FilterAction
}