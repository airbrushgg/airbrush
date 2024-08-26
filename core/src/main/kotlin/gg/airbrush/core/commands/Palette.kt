package gg.airbrush.core.commands

import gg.airbrush.core.lib.Constants
import gg.airbrush.core.lib.PaletteUtil
import gg.airbrush.core.lib.prettify
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.PaletteType
import gg.airbrush.sdk.lib.Translations
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemComponent

class Palette : Command("palette"), CommandExecutor {
    private val palettes = PaletteType.entries.map {
        it.name.prettify()
    }.toTypedArray()

    private val chosenPalette = ArgumentType.Word("palette")
        .from(*palettes)

    init {
        defaultExecutor = this

        addSyntax({ sender: CommandSender, context: CommandContext ->
            run(sender, context)
        }, chosenPalette)
    }

    private fun run(sender: CommandSender, context: CommandContext) {
        val player = sender as Player
        val sdkPlayer = SDK.players.get(player.uuid)

        val palette = context.get(chosenPalette)

        val foundPalette = PaletteType.entries.find {
            it.name.prettify() == palette
        } ?: return

        sdkPlayer.setPalette(foundPalette)

        val currentPaletteType = PaletteType.entries[sdkPlayer.getData().palette]
        val selectorIndex = player.inventory.itemStacks.indexOfFirst { item -> item.get(ItemComponent.CUSTOM_NAME) == Constants.paletteSelectorName }
        player.inventory.setItemStack(selectorIndex, player.inventory.getItemStack(selectorIndex).withMaterial(
            PaletteUtil.getIconBlock(currentPaletteType)))

        player.sendMessage(Translations.translate("core.commands.palette.message", palette))
    }

    override fun apply(sender: CommandSender, context: CommandContext) {}
}