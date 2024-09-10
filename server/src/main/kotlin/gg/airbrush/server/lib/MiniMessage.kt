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