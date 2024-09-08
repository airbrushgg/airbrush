package gg.airbrush.core.commands

import gg.airbrush.core.commands.mainmenu.openMainMenu
import gg.airbrush.core.lib.GUIItems
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.boosters.ActiveBooster
import gg.airbrush.sdk.classes.boosters.BoosterData
import gg.airbrush.server.lib.mm
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.timer.TaskSchedule
import java.time.Duration
import java.util.*
import kotlin.time.toKotlinDuration

class Boost : Command("boost", "booster", "boosters"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return
        openBoostersGui(sender)
    }

    companion object {
        private val scheduler = MinecraftServer.getSchedulerManager()
        private var combinedMultiplier = 1.0

        fun openBoostersGui(player: Player) {
            val sdkPlayer = SDK.players.get(player.uuid)

            val noBoostersItem = ItemStack.builder(Material.BARRIER)
                .customName("<s>You don't have any boosters!".mm())
                .lore("<p>➜ <em>Buy boosters in the shop.".mm())
                .build()
            val borderItem = ItemStack.builder(Material.PURPLE_STAINED_GLASS_PANE)
                .customName("<reset>".mm())
                .build()

            val template = """
            xxxxxxxxx
            xyyyyyyyx
            xyyyyyyyx
            xyyyyyyyx
            xxxxcxxxx
        """.trimIndent()
            val gui = GUI(template, "Your Boosters", InventoryType.CHEST_5_ROW)
            gui.put('x', borderItem)
	        gui.put('c', GUIItems.mainMenu) {
		        openMainMenu(player)
	        }

            val ownedBoosts = sdkPlayer.getOwnedBoosters()
            var itemIndex = 0

            if (ownedBoosts.isEmpty()) {
                gui.put(22, noBoostersItem)
                gui.open(player)
                return
            }

            for (row in template.lines()) {
                row.forEachIndexed { index, c ->
                    if (c != 'y' || itemIndex >= ownedBoosts.size)
                        return@forEachIndexed

                    val booster = ownedBoosts[itemIndex]
                    val boosterItem = ItemStack.builder(Material.BEACON)
                        .customName(booster.name.mm())
                        .build()
                    gui.put(index + 9, boosterItem) {
                        activateBooster(player, booster)
                        player.closeInventory()
                    }

                    itemIndex++
                }
            }

            gui.open(player)
        }

        private fun activateBooster(player: Player, booster: BoosterData) {
            val activeBoosters = SDK.boosters.getActiveBoosters()
            if (activeBoosters.size >= 3) {
                player.sendMessage("<error>You cannot activate ${booster.name} <#ff7f6e>because there are already 3 active boosters!".mm())
                return
            }

            val maybeActiveBooster = activeBoosters.find { b -> b.data.id == booster.id }
            if (maybeActiveBooster != null) {
                player.sendMessage("<error>You cannot active ${booster.name} <#ff7f6e>because it is already active!".mm())
                return
            }

            // Remove the booster from the player.
            val sdkPlayer = SDK.players.get(player.uuid)
            sdkPlayer.removeBooster(booster)

            // Create a new active booster.
            val activeBooster = SDK.boosters.create(booster)
            startBooster(activeBooster)

            val durationString = toDurationString(booster.duration)
            val hoverEvent = """
                ${booster.name}
                <s>Description: <i>${booster.description}</i>
                <s>Multiplier: <p>${booster.multiplier}
            """
                .trimIndent()
                .mm()
                .hoverEvent()

            Audiences.players().apply {
                sendMessage("\n<p>${player.username} <s>activated an ${booster.name} <s>for <p>${durationString}<s>!\n"
                    .mm()
                    .hoverEvent(hoverEvent)
                )
                playSound(
                    Sound.sound(Key.key("block.beacon.power_select"), Sound.Source.MASTER, 1.0f, 1.0f),
                    Sound.Emitter.self()
                )
            }
        }

        private fun toDurationString(seconds: Int) = Duration.ofSeconds(seconds.toLong())
            .toKotlinDuration()
            .toString()

        fun restoreActiveBoosters() {
            SDK.boosters.getActiveBoosters().forEach(this::startBooster)
        }

        fun clearActiveBoosters() {
            SDK.boosters.getActiveBoosters().forEach { activeBooster ->
                SDK.boosters.updateOrClear(UUID.fromString(activeBooster.id))
            }
        }

        fun getMultiplier(): Double = combinedMultiplier

        private fun startBooster(booster: ActiveBooster) {
            // We keep track of the combined multiplier instead of calculating it with a DB query.
            combinedMultiplier += booster.data.multiplier

            // TODO: We probably should not schedule two tasks for each booster...
            scheduler.scheduleTask(BoosterRunnable(booster), TaskSchedule.immediate(), TaskSchedule.seconds(1))
            scheduler.scheduleTask({
                combinedMultiplier -= booster.data.multiplier
                SDK.boosters.clear(UUID.fromString(booster.id))
            }, TaskSchedule.seconds(booster.duration.toLong()), TaskSchedule.stop())
        }
    }

    private class BoosterRunnable(private val booster: ActiveBooster) : Runnable {
        private val bossBar = BossBar.bossBar(booster.data.name.mm(), 1.0f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS)

        override fun run() {
            if (booster.hasCompleted()) {
                Audiences.players().hideBossBar(bossBar)
                return
            }

            Audiences.players().showBossBar(bossBar)

            val timePassed = System.currentTimeMillis() - booster.startedAt
            val progress = 1.0f - timePassed.toFloat() / (booster.duration * 1000)
            bossBar.progress(progress)
        }
    }
}