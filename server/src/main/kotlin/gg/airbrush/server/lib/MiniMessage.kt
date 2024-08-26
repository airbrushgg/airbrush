package gg.airbrush.server.lib

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

@Suppress("unused")
fun String.mm(): Component {
    val mm = MiniMessage.miniMessage()
    return mm.deserialize(this, *getMMResolvers())
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
}

private fun getMMResolvers(): Array<TagResolver> {
    val errorResolver = Placeholder.parsed("error", "<#ff6e6e>⚠ <#ff7f6e>")
    val successResolver = Placeholder.parsed("success", "<g>✔ ")

    return listOf<TagResolver>(
//        TagResolver.resolver("p", Tag.styling(TextColor.color(227, 176, 245))),
        TagResolver.resolver("p", Tag.styling(TextColor.color(200, 130, 224))),
	    TagResolver.resolver("donator", Tag.styling(TextColor.color(255, 229, 99))),
        TagResolver.resolver("s", Tag.styling(TextColor.color(244, 212, 255))),
        TagResolver.resolver("g", Tag.styling(TextColor.color(191, 255, 198))),
        TagResolver.resolver("y", Tag.styling(TextColor.color(240, 245, 171))),
        errorResolver,
        successResolver
    ).toTypedArray()
}