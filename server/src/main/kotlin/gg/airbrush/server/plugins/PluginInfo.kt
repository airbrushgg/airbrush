package gg.airbrush.server.plugins

data class PluginInfo(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val mainClass: String,
    val dependencies: MutableList<String> = mutableListOf()
) {
    init {
        for (dependency in dependencies) {
            if (dependency.matches(Regex("[0-9a-z-]+")))
                continue

            println("Plugin '$id' contains invalid dependency '$dependency' ([0-9a-z-])")
        }
    }
}
