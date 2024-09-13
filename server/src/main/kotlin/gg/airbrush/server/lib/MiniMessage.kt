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

package gg.airbrush.server.lib

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Global [MiniMessage] instance. */
private val miniMessage = MiniMessage.builder()
    .tags(TagResolver.resolver(
        Placeholder.parsed("error", "<#ff6e6e>⚠ <#ff7f6e>"),
        Placeholder.parsed("success", "<g>✔ "),

        TagResolver.resolver("p", Tag.styling(TextColor.color(200, 130, 224))),
        TagResolver.resolver("donator", Tag.styling(TextColor.color(255, 229, 99))),
        TagResolver.resolver("s", Tag.styling(TextColor.color(244, 212, 255))),
        TagResolver.resolver("g", Tag.styling(TextColor.color(191, 255, 198))),
        TagResolver.resolver("y", Tag.styling(TextColor.color(240, 245, 171)))
    ))
    .build()

@Suppress("unused")
fun String.mm(
    vararg resolvers: TagResolver,
): Component {
    return miniMessage.deserialize(this, *resolvers)
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
}