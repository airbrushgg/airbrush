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

package gg.airbrush.worlds

import cc.ekblad.toml.decode
import cc.ekblad.toml.model.TomlException
import cc.ekblad.toml.tomlMapper
import dev.flavored.bamboo.SchematicReader
import gg.airbrush.sdk.lib.ConfigUtils
import gg.airbrush.server.registerDefaultInstance
import net.hollowcube.polar.PolarLoader
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.tag.Tag
import net.minestom.server.world.DimensionType
import java.io.File
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.notExists

object WorldManager {
    private val MANAGED_TAG = Tag.Boolean("ManagedInstance")
    private val PERSISTENT_TAG = Tag.String("PersistentWorld")

    private val logger = LoggerFactory.getLogger(WorldManager::class.java)
    private val instanceManager = MinecraftServer.getInstanceManager()
    private val reader = SchematicReader()
    private var config: WorldConfig

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
        val configPath = ConfigUtils.loadResource(WorldConfig::class.java, "worlds.toml", Worlds.pluginInfo!!)
        try {
            config = mapper.decode<WorldConfig>(configPath)
        } catch (e: TomlException) {
            // We don't want to disable the plugin since it is essential. Instead, we load the default config.
            logger.error("[Worlds] Failed to load worlds config, loading default.", e)

            val stream = Worlds::class.java.getResourceAsStream("/worlds.toml")!!
            stream.use { config = mapper.decode(it) }
        }

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
        val schematic = reader.fromPath(schematicPath)

        val position = Pos(0.0, 4.0, 0.0)
        schematic.paste(instance, position, true)

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

    fun deleteCanvas(canvasId: String) {
        val instance = getWorlds().find { it.getTag(Tag.String("CanvasUUID")) == canvasId } ?: return
        instanceManager.unregisterInstance(instance)
        val filePath = "canvases/$canvasId.polar"
        val file = File(filePath)

        if (file.exists() && file.isFile) {
            val success = file.delete()
            if (!success) MinecraftServer.LOGGER.error("[Worlds] Failed to delete canvas file: $filePath")
        } else MinecraftServer.LOGGER.error("[Worlds] Canvas file does not exist: $filePath")
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
        schematic.paste(_defaultInstance, Pos(0.0, 4.0, 0.0), true)
        // TODO: We should only save the world when the schematic has been fully pasted.
        _defaultInstance.saveChunksToStorage().join()
    }

    private fun loadPersistentWorlds() {
        val persistentWorlds = config.persistentWorlds.orEmpty()

        logger.info("[Worlds] Loading ${persistentWorlds.size} persistent worlds...")

        persistentWorlds.forEach { world ->
            if (persistentWorlds.singleOrNull() == null) {
                logger.error("[Worlds] Config file contains multiple worlds with the name: ${world.name}")
                return@forEach
            }

            val worldPath = Path.of(world.path ?: "${world.name}.polar")
            val instance = instanceManager.createInstanceContainer(fullbrightDimension).apply {
                chunkLoader = PolarLoader(worldPath)
                setTag(MANAGED_TAG, true)
                setTag(PERSISTENT_TAG, world.name)
            }

            if (worldPath.exists()) return

            val schematicPath = Path.of(world.schematic)
            if (schematicPath.notExists()) {
                logger.error("[Worlds] The schematic (path={}) for world '${world.name}' does not exist. Skipping...", schematicPath.absolute())
                return@forEach
            }

            val schematic = reader.fromPath(schematicPath)
            schematic.paste(instance, Pos(0.0, 4.0, 0.0))

            instance.saveChunksToStorage().join()
        }
    }

    private fun checkConfigValues() {
        val registeredTemplates = config.templates.orEmpty()
        val uniqueTemplates = registeredTemplates.distinctBy { t -> t.id.lowercase() }
        check(registeredTemplates.size == uniqueTemplates.size) { "World templates must have a unique ID." }
    }
}