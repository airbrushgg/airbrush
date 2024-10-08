

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

package gg.airbrush.core.filter

data class FilterConfig(
    val root: Root,
    val rulesets: List<Ruleset>?
) {
    data class Root(
        val message: String = "Uh oh! Your message was blocked.",
        val logChannel: String?
    )

    data class Ruleset(
        val priority: Int = 1,
        val action: FilterAction = FilterAction.BLOCK,
        val banReason: String = "hate",
        val path: String,
        val regex: Boolean = false
    )
}
