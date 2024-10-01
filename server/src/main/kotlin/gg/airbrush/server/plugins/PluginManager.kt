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

import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.InvocationTargetException

val PLUGIN_REGEX = Regex("[0-9a-z-]+")

class PluginManager {
    private val logger = LoggerFactory.getLogger(PluginManager::class.java)
    private val pluginsFolder = File("plugins")
    val plugins = mutableMapOf<String, Plugin>()

    fun registerPlugins() {
        for (file in listJARs()) {
            val loader = PluginClassLoader(file, javaClass.classLoader)
            val info = loader.getPluginInfo() ?: continue

            if (!info.id.matches(PLUGIN_REGEX)) {
                logger.error("Found plugin '${info.name}' with an invalid ID. ([0-9a-z-])")
                continue
            }

            try {
                val clazz = Class.forName(info.mainClass, true, loader)
                val plugin = clazz.getConstructor().newInstance() as Plugin
                plugin.info = info
                plugin.loader = loader
                plugins[info.id.lowercase()] = plugin
            } catch (e: ClassNotFoundException) {
                logger.error("Found plugin '${info.name}' with an invalid main class.")
            } catch (e: InvocationTargetException) {
                logger.error("Exception thrown while running plugin constructor", e.targetException)
            }
        }
    }

    fun setupPlugins() {
        outer@for (plugin in plugins.values) {
            if (plugin.isSetup)
                continue

            val info = plugin.info

            for (id in info.dependencies) {
                if (!plugins.containsKey(id)) {
                    logger.warn("Plugin '${info.id}' requires dependency '$id' that is not present.")
                    continue@outer
                }

                val dependingPlugin = plugins[id] ?: continue

                if (dependingPlugin.isSetup)
                    continue

                if (dependingPlugin.info.dependencies.contains(id)) {
                    logger.warn("Found circular dependency between '${info.id}' and '$id'.")
                    continue@outer
                }

                enablePlugin(dependingPlugin)
            }

            enablePlugin(plugin)
        }
    }

    fun teardownPlugins() {
        plugins.values.forEach(Plugin::teardown)
    }

    fun enablePlugin(plugin: Plugin) {
        if (plugin.isSetup) {
            return
        }

        logger.info("Enabling plugin '${plugin.info.id}'...")
        try {
            plugin.setup()
            plugin.isSetup = true
        } catch (e: Exception) {
            MinecraftServer.getExceptionManager().handleException(e)
        }
    }

    fun disablePlugin(plugin: Plugin) {
        if (!plugin.isSetup) {
            return
        }

        logger.info("Disabling plugin '${plugin.info.id}'...")
        try {
            plugin.teardown()
        } catch (e: Exception) {
            MinecraftServer.getExceptionManager().handleException(e)
        } finally {
            plugin.isSetup = false
        }
    }

    private fun listJARs(): List<File> {
        pluginsFolder.mkdir()

        return (pluginsFolder
            .listFiles()?.toList() ?: emptyList())
            .filter { it.extension.lowercase() == "jar" }
    }
}