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

package gg.airbrush.server.plugins

import cc.ekblad.toml.tomlMapper
import net.minestom.server.MinecraftServer
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
                MinecraftServer.LOGGER.info("Found plugin '${info.name}' with an invalid ID. ([0-9a-z-])")
                continue
            }

            try {
                val clazz = loader.loadClass(info.mainClass)
                val plugin = clazz.getConstructor().newInstance() as Plugin
                plugin.info = info
                plugin.loader = loader
                plugins[info.id.lowercase()] = plugin
            } catch (e: ClassNotFoundException) {
                MinecraftServer.LOGGER.info("Found plugin '${info.name}' with an invalid main class.")
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
                    MinecraftServer.LOGGER.info("Plugin '${info.id}' requires dependency '$id' that is not present.")
                    continue@main
                }

                val dependingPlugin = plugins[id] ?: continue

                if (dependingPlugin.isSetup)
                    continue

                if (dependingPlugin.info.dependencies.contains(id)) {
                    MinecraftServer.LOGGER.info("Found circular dependency between '${info.id}' and '$id'.")
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