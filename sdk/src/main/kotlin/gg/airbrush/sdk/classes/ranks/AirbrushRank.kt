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

package gg.airbrush.sdk.classes.ranks

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import gg.airbrush.sdk.NotFoundException
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.SDK
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import java.util.*

data class RankData(
    var name: String,
    var prefix: String,
    var permissions: List<PermissionData>,
    var parent: String?,
    val id: String = UUID.randomUUID().toString(),
)

data class PermissionData(
    val key: String,
    val value: String?
)

private val db = Database.get()

class AirbrushRank(id: UUID) {
    private val data: RankData
    private val col = db.getCollection<RankData>("ranks")

    private val query = Filters.eq(RankData::id.name, id.toString())

    init {
        data = col.find(query).firstOrNull()
            ?: throw NotFoundException("Rank with ID of $id not found.")
    }

    @Suppress("unused")
    fun getData(): RankData {
        return data
    }

    @Suppress("unused")
    fun addPermission(key: String, value: CompoundBinaryTag?) {
        val currentPermissions = data.permissions.toMutableList()
        val writtenNbt = TagStringIO.builder().build().asString(value ?: CompoundBinaryTag.empty())
        currentPermissions.add(PermissionData(key, writtenNbt))

        col.updateOne(query, Updates.set(RankData::permissions.name, currentPermissions))

        data.permissions = currentPermissions
    }

    @Suppress("unused")
    fun removePermission(key: String) {
        val currentPermissions = data.permissions.toMutableList()
        currentPermissions.removeIf { it.key == key }

        col.updateOne(query, Updates.set(RankData::permissions.name, currentPermissions))

        data.permissions = currentPermissions
    }

    @Suppress("unused")
    fun setParent(rank: AirbrushRank?) {
        val id = rank?.data?.id
        col.updateOne(query, Updates.set(RankData::parent.name, id))
        data.parent = id
    }

    @Suppress("unused")
    fun getParent(): AirbrushRank? {
        if (data.parent == null)
            return null

        return SDK.ranks.get(UUID.fromString(data.parent))
    }

    @Suppress("unused")
    fun setPrefix(prefix: String) {
        col.updateOne(query, Updates.set(RankData::prefix.name, prefix))

        data.prefix = prefix
    }

    @Suppress("unused")
    fun setName(name: String) {
        col.updateOne(query, Updates.set(RankData::name.name, name))

        data.name = name
    }
}