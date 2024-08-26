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