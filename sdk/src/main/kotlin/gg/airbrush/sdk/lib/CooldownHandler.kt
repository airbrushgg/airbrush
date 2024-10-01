/*
 * This file is part of Airbrush
 *
 * Copyright (c) Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.sdk.lib

// NOTE: Redis is not implemented here. Please only use this for small cooldowns (ex: debouncing)

private val cooldowns: MutableMap<String, Cooldown> = mutableMapOf()

class Cooldown() {
    var key = ""
    var duration = 0L
    private var startTime = 0L

    private fun reset() {
        duration = 0L
        startTime = 0L
    }

    fun isActive(): Boolean {
        if (startTime == 0L) return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - startTime) < duration
    }

    fun getRemainingTime(): Long {
        if (startTime == 0L) return 0L
        val currentTime = System.currentTimeMillis()
        return (duration - (currentTime - startTime)).coerceAtLeast(0L)
    }

    fun startCooldown(): Cooldown {
        this.startTime = System.currentTimeMillis()
        return this
    }
}

fun handleCooldown(init: Cooldown.() -> Unit): Cooldown {
    val cooldown = Cooldown()
    cooldown.init()
    return cooldowns.getOrPut(cooldown.key) {
        cooldown
    }
}