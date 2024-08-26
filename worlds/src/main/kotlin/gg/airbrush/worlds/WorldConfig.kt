package gg.airbrush.worlds

data class WorldConfig(
    val default: World,
    val persistentWorlds: List<World>?,
    val templates: List<Template>?
) {
    data class World(val name: String, val schematic: String, val path: String?)
    data class Template(val id: String, val schematic: String)
}
