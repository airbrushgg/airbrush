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

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.airbrush.server.pluginManager
import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader

class PluginClassLoader(private val file: File, parent: ClassLoader) : URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    private var cachedInfo: PluginInfo? = null
    private val logger = LoggerFactory.getLogger(PluginClassLoader::class.java)
    private val cachedClasses = HashMap<String, Class<*>>()

//    override fun loadClass(name: String, resolve: Boolean): Class<*> {
//        println("[${Thread.currentThread().name}] Plugin '${cachedInfo?.name}' is loading class '$name'")
//        return loadClass0(name, resolve, true)
//    }
//
//    private fun loadClass0(name: String, resolve: Boolean, checkPlugins: Boolean): Class<*> {
//        if (checkPlugins) {
//            for (plugin in pluginManager.plugins.values) {
//                try {
//                    val clazz = plugin.loader.loadClass0(name, resolve, false)
//                    val loader = clazz.classLoader
//
//                    if (loader is PluginClassLoader) {
//                        val info = loader.getPluginInfo() ?: return clazz
//
//                        if (plugin.info.dependencies.contains(info.id) || info.id == plugin.info.id)
//                            return clazz
//
//                        MinecraftServer.LOGGER.info("Plugin '${plugin.info.id}' loaded class '${clazz.name}' from non-dependency plugin '${info.id}'.")
//                    }
//
//                    return clazz
//                } catch (_: ClassNotFoundException) {
//                }
//            }
//        }
//
//        return super.loadClass(name, resolve)
//    }

    override fun findClass(name: String): Class<*> {
        return findClass(name, true)
    }

    private fun findClass(name: String, checkPlugins: Boolean): Class<*> {
        if (cachedClasses.containsKey(name)) {
            return cachedClasses[name]!!
        }

        if (checkPlugins) {
            if (cachedParentClasses.containsKey(name)) {
                return cachedParentClasses[name]!!
            }

            for (plugin in pluginManager.plugins.values) {
                try {
                    val clazz = plugin.loader.findClass(name, false)
                    val classLoader = clazz.classLoader

                    if (classLoader is PluginClassLoader) {
                        classLoader.cachedInfo?.let { info ->
                            if (!plugin.info.dependencies.contains(info.id) && info.id != plugin.info.id) {
                                logger.warn("Plugin '${plugin.info.id}' loaded class '${clazz.name}' from non-dependency plugin '${info.id}'.")
                            }
                        }
                    }

                    cachedClasses[name] = clazz
                    return clazz
                } catch (_: ClassNotFoundException) {
                }
            }
        }

        val clazz = super.findClass(name)
        if (clazz != null) {
            cachedParentClasses[name] = clazz
//            cachedClasses[name] = clazz
        }

        return clazz
    }

    fun getPluginInfo(): PluginInfo? {
        if (cachedInfo != null)
            return cachedInfo

        val mapper = tomlMapper {}
        val stream = getResourceAsStream("plugin.toml")

        if (stream == null) {
            MinecraftServer.LOGGER.warn("Found JAR '${file.name}' in the plugins folder without a plugin.toml file.")
            return null
        }

        val infoResult = runCatching {
            mapper.decode<PluginInfo>(stream.bufferedReader().use { it.readText() })
        }
        val info = infoResult.getOrNull() ?: return null

        cachedInfo = info
        return info
    }

    private companion object {
        val cachedParentClasses = HashMap<String, Class<*>>()
    }
}