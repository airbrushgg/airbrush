package gg.airbrush.worlds

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import dev.flavored.bamboo.SchematicReader
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.server.registerDefaultInstance
import gg.airbrush.worlds.events.InstanceReadyEvent
import net.hollowcube.polar.PolarLoader
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

object WorldManager {
    private val MANAGED_TAG = Tag.Boolean("ManagedInstance")
    private val PERSISTENT_TAG = Tag.String("PersistentWorld")

    private val config: WorldConfig
    private val instanceManager = MinecraftServer.getInstanceManager()

    private val reader = SchematicReader()

    private lateinit var _defaultInstance: InstanceContainer
    val defaultInstance get() = _defaultInstance

    val fullbrightDimension = MinecraftServer.getDimensionTypeRegistry().register(
        "airbrush:full_bright",
        DimensionType.builder()
            .ambientLight(2.0f)
            .build()
    )

    init {
        val mapper = tomlMapper {
            mapping<WorldConfig>(
                "template" to "templates",
                "world" to "persistentWorlds"
            )
        }
        val configPath = ConfigUtils.loadResource(WorldConfig::class.java, "worlds.toml", "..")
        config = mapper.decode<WorldConfig>(configPath)

        checkConfigValues()
    }

    /**
     * Creates a new instance from a template defined in the configuration file.
     *
     * **Note:** Instances created from templates will not be automatically saved to disk.
     */
    fun createFromTemplate(id: String): InstanceContainer {
        val template = config.templates.orEmpty().find { t -> t.id == id }
        checkNotNull(template) { "Template was not defined in the config file." }

        val instance = instanceManager.createInstanceContainer(fullbrightDimension)
        instance.setTag(MANAGED_TAG, true)

        val schematicPath = Path.of(template.schematic)
        val schematic = reader.fromPath(schematicPath) ?: return instance

        val position = Pos(0.0, 4.0, 0.0)
        schematic.paste(instance, position, true)

        // TODO: We should only send the event when the schematic has been fully pasted.
        val readyEvent = InstanceReadyEvent(instance)
        instance.eventNode().call(readyEvent)

        return instance
    }

    /**
     * Returns a persistent world by the name as defined in the configuration file.
     */
    fun getPersistentWorld(name: String): Instance? {
        return getPersistentWorlds().find { it.getTag(PERSISTENT_TAG) == name }
    }

    /**
     * Returns a list of all registered instances that were created through the [WorldManager].
     */
    fun getWorlds(): List<Instance> {
        return instanceManager.instances.filter { instance -> instance.hasTag(MANAGED_TAG) }
    }

    /**
     * Returns a list of all registered instances that were created through the [WorldManager] and
     * are persistent (saved to disk).
     */
    fun getPersistentWorlds(): List<Instance> {
        return getWorlds().filter { it.hasTag(PERSISTENT_TAG) }
    }

    fun initialize() {
        loadPersistentWorlds()
    }

    fun dispose() {
        // Save all the persistent worlds to disk.
        getPersistentWorlds().forEach { it.saveChunksToStorage().join() }
    }

    fun loadDefaultInstance() {
        _defaultInstance = instanceManager.createInstanceContainer(fullbrightDimension)
        registerDefaultInstance(_defaultInstance) // Register the default instance with the server.

        val worldPath = Path.of("${config.default.name}.polar")
        val chunkLoader = PolarLoader(worldPath)
        _defaultInstance.chunkLoader = chunkLoader

        if (worldPath.exists()) return

        val schematicPath = Path.of(config.default.schematic)
        if (schematicPath.notExists())
            throw IOException("The default world schematic does not exist.")

        val schematic = reader.fromPath(schematicPath)
        schematic.paste(_defaultInstance, Pos(0.0, 4.0, 0.0))
        // TODO: We should only save the world when the schematic has been fully pasted.
        _defaultInstance.saveChunksToStorage().join()
    }

    private fun loadPersistentWorlds() {
        val persistentWorlds = config.persistentWorlds.orEmpty()
        persistentWorlds.forEach { world ->
            if (persistentWorlds.singleOrNull() == null) {
                println("[Worlds] ERROR! Config file contains multiple worlds with the name: ${world.name}")
                return@forEach
            }

            val worldPath = Path.of(world.path ?: "${world.name}.polar")
            println("Loading world $worldPath")
            val instance = instanceManager.createInstanceContainer(fullbrightDimension).apply {
                chunkLoader = PolarLoader(worldPath)
                setTag(MANAGED_TAG, true)
                setTag(PERSISTENT_TAG, world.name)
            }
            println("Created instance")

            if (worldPath.exists()) return

            println("Pasting schematic")
            val schematic = reader.fromPath(Path.of(world.schematic))
            schematic.paste(instance, Pos(0.0, 4.0, 0.0))
            println("Schematic pasted")

            // TODO: We should only send the event when the schematic has been fully pasted.
            instance.eventNode().call(InstanceReadyEvent(instance))
            instance.saveChunksToStorage().join()
            println("Done")
        }
    }

    private fun checkConfigValues() {
        val registeredTemplates = config.templates.orEmpty()
        val uniqueTemplates = registeredTemplates.distinctBy { t -> t.id.lowercase() }
        check(registeredTemplates.size == uniqueTemplates.size) { "World templates must have a unique ID." }
    }
}