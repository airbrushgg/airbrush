

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

import net.kyori.adventure.util.HSVLike
import net.kyori.adventure.util.RGBLike
import kotlin.math.abs

object ColorUtil {
    fun oscillateHSV(start: RGBLike, end: RGBLike, value: Int): HSVLike {
        // TODO: This should be rewritten to fix the inaccuracy at higher levels (100+)
        val min = Math.toDegrees(start.asHSV().h().toDouble())
        val max = Math.toDegrees(end.asHSV().h().toDouble())
        val range = max - min

        val h = min + abs((value - 1 + range) % (range * 2) - range)
        return HSVLike.hsvLike(Math.toRadians(h).toFloat(), 0.7f, 1.0f)
    }
}