package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.platforms.bukkit.PluginHooks;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

public class ParseCommand extends SubCommand{

	public ParseCommand() {
		super("parse", "tab.parse");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (args.length > 0) {
			String replaced = "";
			for (int i=0; i<args.length; i++){
				if (i>0) replaced += " ";
				replaced += args[i];
			}
			String message = Placeholders.color("&6Replacing placeholder &e%placeholder%" + (sender == null ? "" : "&6 for player &e" + sender.getName())).replace("%placeholder%", replaced);
			sendRawMessage(sender, message);
			for (Placeholder p : Placeholders.getAllPlaceholders()) {
				if (replaced.contains(p.getIdentifier())) {
					if (p instanceof ServerPlaceholder) {
						((ServerPlaceholder)p).update();
					}
					if (p instanceof PlayerPlaceholder) {
						((PlayerPlaceholder)p).update(sender);
					}
					replaced = p.set(replaced, sender);
				}
			}
			if (Shared.separatorType.equals("world")) replaced = PluginHooks.setPlaceholders(sender == null ? null : sender.getUniqueId(), replaced);
			IChatBaseComponent colored = IChatBaseComponent.optimizedComponent("With colors: " + replaced);
			if (sender != null) {
				sender.sendCustomPacket(new PacketPlayOutChat(colored));
			} else {
				sendRawMessage(sender, colored.toColoredText());
			}
			sendRawMessage(sender, "Without colors: " + replaced.replace(Placeholders.colorChar, '&'));
		} else {
			sendMessage(sender, "Usage: /tab parse <placeholder>");
		}
	}
}