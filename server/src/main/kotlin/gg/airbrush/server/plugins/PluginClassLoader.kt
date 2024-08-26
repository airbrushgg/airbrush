package gg.airbrush.server.plugins

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.airbrush.server.pluginManager
import java.io.File
import java.net.URLClassLoader

class PluginClassLoader(private val file: File, parent: ClassLoader) : URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    private var cachedInfo: PluginInfo? = null

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return loadClass0(name, resolve, true)
    }

    private fun loadClass0(name: String, resolve: Boolean, checkPlugins: Boolean): Class<*> {
        try {
            return super.loadClass(name, resolve)
        } catch (_: ClassNotFoundException) {}

        if (checkPlugins)
            for (plugin in pluginManager.plugins.values) {

                try {
                    val clazz = plugin.loader.loadClass0(name, resolve, false)
                    val loader = clazz.classLoader

                    if (loader is PluginClassLoader) {
                        val info = loader.getPluginInfo() ?: return clazz

                        if (plugin.info.dependencies.contains(info.id) || info.id == plugin.info.id)
                            return clazz

                        println("Plugin '${plugin.info.id}' loaded class '${clazz.name}' from non-dependency plugin '${info.id}'.")
                    }

                    return clazz
                } catch (_: ClassNotFoundException) { }
            }

        throw ClassNotFoundException(name)
    }

    fun getPluginInfo(): PluginInfo? {
        if (cachedInfo != null)
            return cachedInfo

        val mapper = tomlMapper {}
        val stream = getResourceAsStream("plugin.toml")

        if (stream == null) {
            println("Found JAR '${file.name}' in the plugins folder without a plugin.toml file.")
            return null
        }

        val info = mapper.decode<PluginInfo>(stream.bufferedReader().use { it.readText() })
        cachedInfo = info
        return info
    }
}