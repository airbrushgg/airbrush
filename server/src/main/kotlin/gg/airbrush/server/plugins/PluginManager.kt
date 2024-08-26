package gg.airbrush.server.plugins

import cc.ekblad.toml.tomlMapper
import java.io.File

val PLUGIN_REGEX = Regex("[0-9a-z-]+")

class PluginManager {
    private val pluginsFolder = File("plugins")
    val plugins = mutableMapOf<String, Plugin>()

    fun registerPlugins() {
        val mapper = tomlMapper {}

        for (file in listJARs()) {
            val loader = PluginClassLoader(file, this.javaClass.classLoader)
            val info = loader.getPluginInfo() ?: continue

            if (!info.id.matches(PLUGIN_REGEX)) {
                println("Found plugin '${info.name}' with an invalid ID. ([0-9a-z-])")
                continue
            }

            try {
                val clazz = loader.loadClass(info.mainClass)
                val plugin = clazz.getConstructor().newInstance() as Plugin
                plugin.info = info
                plugin.loader = loader
                plugins[info.id.lowercase()] = plugin
            } catch (e: ClassNotFoundException) {
                println("Found plugin '${info.name}' with an invalid main class.")
            }
        }
    }

    fun setupPlugins() {
        main@for (plugin in plugins.values) {
            if (plugin.isSetup)
                continue

            val info = plugin.info

            for (id in info.dependencies) {
                if (!plugins.containsKey(id)) {
                    println("Plugin '${info.id}' requires dependency '$id' that is not present.")
                    continue@main
                }

                val dependingPlugin = plugins[id] ?: continue

                if (dependingPlugin.isSetup)
                    continue

                if (dependingPlugin.info.dependencies.contains(id)) {
                    println("Found circular dependency between '${info.id}' and '$id'.")
                    continue@main
                }

                dependingPlugin.setup()
                dependingPlugin.isSetup = true
            }

            plugin.setup()
            plugin.isSetup = true
        }
    }

    fun teardownPlugins() {
        plugins.values.forEach(Plugin::teardown)
    }

    private fun listJARs(): List<File> {
        pluginsFolder.mkdir()

        return (pluginsFolder
            .listFiles()?.toList() ?: emptyList())
            .filter { it.extension.lowercase() == "jar" }
    }
}