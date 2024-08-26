package gg.airbrush.server.plugins

abstract class Plugin {
    lateinit var info: PluginInfo
    lateinit var loader: PluginClassLoader
    var isSetup = false

    abstract fun setup()
    abstract fun teardown()
}
