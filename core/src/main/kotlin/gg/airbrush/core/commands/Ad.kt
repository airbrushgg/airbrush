
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

class Ad : Command("ad", "ads", "advertisement"), CommandExecutor {
	init {
		defaultExecutor = this
	}

	override fun apply(sender: CommandSender, context: CommandContext) {
		val list = listOf(
			"<bold><#ff21ff>Join and Paint <blue>| <#9b94ff>Earn XP <blue>| <#f8c9ff>Private Worlds",
			"<bold><#21ff90>Join and Paint <blue>| <#baff0a>Unlock Colors <blue>| <#ca8aff>Level Up",
			"<bold><#ff3385>Join and Paint <blue>| <#99ff33>Private Worlds <blue>| <#03cdff>Level Up"
		)

		list.forEach {
			val copy =  it.replace("<bold>", "&l")
				.replace("<blue>", "&b")
				.replace(">", "")
				.replace("<", "&")
			sender.sendMessage("<hover:show_text:'<p>➜ <em>Click to copy!'><click:copy_to_clipboard:/ad Airbrush $copy>/ad Airbrush $it</click></hover>".mm())
		}
	}
}