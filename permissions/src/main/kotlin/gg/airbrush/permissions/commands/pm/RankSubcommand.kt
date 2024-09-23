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

package gg.airbrush.permissions.commands.pm

import gg.airbrush.permissions.commands.arguments.RankArgument
import gg.airbrush.permissions.commands.arguments.rankCache
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.ranks.AirbrushRank
import gg.airbrush.server.lib.mm
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.CommandSyntax
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType.*
import net.minestom.server.permission.Permission
import java.util.*

object RankSubcommand : Command("rank") {
     init {
         val set = Literal("set")
         val get = Literal("get")

         addSyntax(this::rename, Literal("rename"), String("new-name"))
         addSyntax(this::delete, Literal("delete"))

         val parent = Literal("parent")
         addSyntax(this::setParent, parent, set, RankArgument("new-parent"))
         addSyntax(this::getParent, parent, get)

         val weight = Literal("weight")
         addSyntax(this::setWeight, weight, set, Integer("new-weight"))
         addSyntax(this::getWeight, weight, get)

         val prefix = Literal("prefix")
         addSyntax(this::setPrefix, prefix, set, String("new-prefix"))
         addSyntax(this::getPrefix, prefix, get)

         val permission = Literal("permission")
         val key = String("key")
         addSyntax(this::getPermission, permission, get, key)
         addSyntax(this::listPermissions, permission, Literal("list"))
         addSyntax(this::removePermission, permission, Literal("remove"), key)
         addSyntax(this::addPermission, permission, Literal("add"), key, NbtCompound("value")
             .setDefaultValue(CompoundBinaryTag.empty()))
     }

    override fun addSyntax(
        executor: CommandExecutor,
        vararg args: Argument<*>
    ): MutableCollection<CommandSyntax> {
        return super.addSyntax(executor, RankArgument("rank-name"), *args)
    }

    private fun setParent(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val (parentName, parent) = getRank(context, "new-parent")

        if (parent == rank) {
            sender.sendMessage("<s>You cannot set the parent of a rank to itself.".mm())
            return
        }

        val id = UUID.fromString(rank.getData().id)
        val badParent = checkParent(parent, mutableListOf(id))?.getData()

        if (badParent != null) {
            sender.sendMessage("<s>Detected circular dependency! <p>${badParent.name}</p> is a child of <p>$name</p> which is a child of the former.".mm())
            return
        }

        rank.setParent(parent)
        sender.sendMessage("<s>Set parent of rank <p>$name</p> to rank <p>$parentName</p>.".mm())
    }

    private fun getParent(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val parent = rank.getParent()

        if (parent == null) {
            sender.sendMessage("<s>Rank <p>$name</p> has no parent.".mm())
            return
        }

        sender.sendMessage("<s>Rank <p>${parent.getData().name}</p> is the parent of rank <p>$name</p>.".mm())
    }

    private fun setWeight(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val weight = context.get<Int>("new-weight")

        rank.setWeight(weight)
        sender.sendMessage("<s>Set weight of rank <p>$name</p> to <p>$weight</p>.".mm())
    }

    private fun getWeight(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val weight = rank.getWeight()

        sender.sendMessage("<s>Rank <p>$name</p> has a weight of <p>$weight</p>.".mm())
    }

    private fun checkParent(rank: AirbrushRank, checked: MutableList<UUID>): AirbrushRank? {
        val parent = rank.getParent() ?: return null
        val parentId = UUID.fromString(parent.getData().id)

        if (parentId in checked)
            return rank

        val rankId = UUID.fromString(rank.getData().id)
        checked.add(rankId)

        return checkParent(parent, checked)
    }

    private fun rename(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val newName = context.get<String>("new-name")

        rank.setName(newName)
        sender.sendMessage("<s>Renamed rank <p>$name</p> to <p>$newName</p>.".mm())
    }

    private fun delete(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)

        SDK.ranks.delete(UUID.fromString(rank.getData().id))
        rankCache.remove(rank)
        sender.sendMessage("<s>Deleted rank <p>$name</p>.".mm())
    }

    private fun setPrefix(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val prefix = context.get<String>("new-prefix")

        rank.setPrefix(prefix)
        sender.sendMessage("<s>Set the prefix of rank <p>$name</p> to <p>$prefix</p><reset><s>.".mm())
    }

    private fun getPrefix(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)

        val prefix = rank.getData().prefix
        sender.sendMessage("<s>The prefix of rank <p>$name</p> is <p>$prefix</p><reset><s>.".mm())
    }

    private fun listPermissions(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val permissions = rank.getData().permissions
        val pages = mutableListOf<Component>()
        val book = Book
            .builder()
            .author("".mm())
            .title("".mm())

        val header = "<s>Permissions for <p>$name</p>\nClick on a key to view NBT.\n".mm()

        for ((index, chunk) in permissions.chunked(10).withIndex()) {
            var component = "<s>".mm()

            if (index == 0)
                component = component.append(header)

            for ((key) in chunk) {
                component = component.append("<p>$key\n"
                    .mm()
                    .clickEvent(ClickEvent
                        .runCommand("/pm rank $name permission get $key")))
            }

            pages.add(component)
        }

        if (pages.isEmpty())
            pages.add(header)

        sender.openBook(book.pages(pages))
    }

    private fun getPermission(sender: CommandSender, context: CommandContext) {
        val (_, rank) = getRank(context)
        val key = context.get<String>("key")

        val permissions = rank.getData().permissions
        val permission = permissions.find { it.key == key  }

        if (permission == null) {
            sender.sendMessage("<s>That rank doesn't have the permission <p>$key</p>.".mm())
            return
        }

        sender.sendMessage("<s>\nViewing permission <p>$key</p>.</s>\n${permission.value ?: "{}"}\n".mm())
    }

    private fun addPermission(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val key = context.get<String>("key")
        val value = context.getOrDefault("value", CompoundBinaryTag.empty())

        if (rank.getData().permissions.any { it.key == key })
            rank.removePermission(key) // Override permission

        rank.addPermission(key, value)

        for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
            val airbrushPlayer = SDK.players.get(player.uuid)

            if (airbrushPlayer.getRank() == rank)
                player.addPermission(Permission(key, value))
        }

        sender.sendMessage("<s>Added permission <p>$key</p> with value <p>${TagStringIO.builder().build().asString(value)}</p> to rank <p>$name</p>.".mm())
    }

    private fun removePermission(sender: CommandSender, context: CommandContext) {
        val (name, rank) = getRank(context)
        val key = context.get<String>("key")

        for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
            val airbrushPlayer = SDK.players.get(player.uuid)

            if (airbrushPlayer.getRank() == rank)
                player.removePermission(key)
        }

        rank.removePermission(key)
        sender.sendMessage("<s>Removed permission <p>$key</p> from rank <p>$name</p>.".mm())
    }

    private fun getRank(context: CommandContext, id: String = "rank-name"): Pair<String, AirbrushRank> {
        val rank = context.get<AirbrushRank>(id)
        return rank.getData().name to rank
    }
}