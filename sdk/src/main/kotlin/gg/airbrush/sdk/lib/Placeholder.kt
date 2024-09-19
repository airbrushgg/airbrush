/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2024 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.sdk.lib

class Placeholder(val string: String, val replacement: String) {
    /**
     * Returns a string representation of the [Placeholder] object.
     */
    override fun toString(): String {
        return "Placeholder(string='$string', replacement='$replacement')"
    }
}

fun String.parsePlaceholders(placeholders: List<Placeholder>): String {
    var message = this

    for (p in placeholders) {
        val regex = Regex("%${p.string.replace("%", "")}(_([a-zA-Z]+))?%")

        message = message.replace(regex) { matchResult ->
            val found = matchResult.value
            when {
                found.endsWith("_lowercase%") -> p.replacement.lowercase()
                found.endsWith("_uppercase%") -> p.replacement.uppercase()
                else -> p.replacement
            }
        }
    }

    return message
}
