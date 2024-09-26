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

import java.util.*

/**
 * Runs the given code after the given time.
 */
fun delay(time: Long, block: () -> Unit) {
    Thread.sleep(time)
    block()
}

fun setInterval(interval: Long, task: () -> Unit): Timer {
    val timer = Timer(true)
    timer.scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            task()
        }
    }, 0, interval)
    return timer
}