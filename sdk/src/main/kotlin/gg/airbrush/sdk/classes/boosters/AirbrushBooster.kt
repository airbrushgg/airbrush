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

package gg.airbrush.sdk.classes.boosters

import kotlinx.serialization.Serializable

data class BoosterConfig(
    val boosters: List<BoosterData>?
)

@Serializable
data class BoosterData(
    val id: String,
    /** The name of the booster. */
    val name: String,
    /** A fun, little description that will be shown to the player. */
    val description: String?,
    /** The multiplier strength of the booster. Must be greater than 1.0. */
    val multiplier: Double,
    /** The duration of the booster, in seconds. */
    val duration: Int,
)

data class ActiveBooster(
    val id: String,
    val data: BoosterData,
    val startedAt: Long,
    val duration: Int,
) {
    fun hasCompleted(): Boolean {
        return System.currentTimeMillis() > startedAt + (duration * 1000)
    }
}