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