package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;

/**
 * Handler for "/tab parse" subcommand
 */
public class ParseCommand extends SubCommand{

	public ParseCommand() {
		super("parse", "tab.parse");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length > 0) {
			String replaced = "";
			for (int i=0; i<args.length; i++){
				if (i>0) replaced += " ";
				replaced += args[i];
			}
			String message = PlaceholderManager.color("&6Replacing placeholder &e%placeholder%" + (sender == null ? "" : "&6 for player &e" + sender.getName())).replace("%placeholder%", replaced);
			sendRawMessage(sender, message);
			replaced = Shared.platform.replaceAllPlaceholders(replaced, sender);
			IChatBaseComponent colored = IChatBaseComponent.optimizedComponent("With colors: " + replaced);
			if (sender != null) {
				sender.sendCustomPacket(new PacketPlayOutChat(colored));
			} else {
				sendRawMessage(sender, colored.toLegacyText());
			}
			sendRawMessage(sender, "Without colors: " + replaced.replace(PlaceholderManager.colorChar, '&'));
		} else {
			sendMessage(sender, "Usage: /tab parse <placeholder>");
		}
	}
}