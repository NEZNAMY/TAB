package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.PropertyImpl;

/**
 * Handler for "/tab parse" subcommand
 */
public class ParseCommand extends SubCommand{

	/**
	 * Constructs new instance
	 */
	public ParseCommand() {
		super("parse", "tab.parse");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length > 0) {
			String replaced = String.join(" ", args);
			String message = EnumChatFormat.color("&6Replacing placeholder &e%placeholder%" + (sender == null ? "" : "&6 for player &e" + sender.getName())).replace("%placeholder%", replaced);
			sendRawMessage(sender, message);
			replaced = new PropertyImpl(null, sender, replaced).get();
			IChatBaseComponent colored = IChatBaseComponent.optimizedComponent("With colors: " + replaced);
			if (sender != null) {
				sender.sendMessage(colored);
			} else {
				sendRawMessage(sender, colored.toLegacyText());
			}
			sendRawMessage(sender, "Without colors: " + EnumChatFormat.decolor(replaced));
		} else {
			sendMessage(sender, "Usage: /tab parse <placeholder>");
		}
	}
}