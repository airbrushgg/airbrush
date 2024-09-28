

/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2023 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.core.lib

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Formats an int to a string with commas
 */
fun Int.format(): String {
    return DecimalFormat("#,###,###").format(this)
}

/**
 * Formats a long to a string with commas
 */
fun Long.format(): String {
	return DecimalFormat("#,###,###").format(this)
}

fun Long.toRelativeTime(): String {
    val currentInstant = Instant.now()
    val targetInstant = Instant.ofEpochMilli(this)

    val zoneId = ZoneId.systemDefault()
    val currentDateTime = LocalDateTime.ofInstant(currentInstant, zoneId)
    val targetDateTime = LocalDateTime.ofInstant(targetInstant, zoneId)

    val diff = ChronoUnit.SECONDS.between(targetDateTime, currentDateTime)

    fun formatAgo(diff: Long, secondsInUnit: Long, unit: String): String {
        val time = diff / secondsInUnit
        return "$time ${if (time > 1) unit + "s" else unit} ago"
    }

    return when {
        diff < 60 -> "Just now"
        diff < 3600 -> formatAgo(diff, 60, "minute")
        diff < 86400 -> formatAgo(diff, 3600, "hour")
        diff < 604800 -> formatAgo(diff, 86400, "day")
        else -> formatAgo(diff, 604800, "week")
    }
}

fun Long.formatTime(): String {
    val seconds = this / 1000.0
    return when {
        seconds < 60 -> "%.2f seconds remaining".format(seconds)
        seconds < 3600 -> {
            val minutes = seconds / 60
            if (minutes < 2) "%.0f minute remaining".format(minutes)
            else "%.0f minutes remaining".format(minutes)
        }
        else -> {
            val hours = seconds / 3600
            if (hours < 2) "%.0f hour remaining".format(hours)
            else "%.0f hours remaining".format(hours)
        }
    }
}
