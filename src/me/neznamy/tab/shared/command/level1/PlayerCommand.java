package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

public class PlayerCommand extends SubCommand {
	
	private static final String[] usualProperties = {"tabprefix", "tabsuffix", "tagprefix", "tagsuffix", "customtabname"};
	private static final String[] extraProperties = {"abovename", "belowname", "customtagname"};
	
	public PlayerCommand() {
		super("player", null);
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		//<name> <property> [value...]
		if (args.length > 1) {
			String player = args[0];
			String type = args[1].toLowerCase();
			String value = "";
			for (int i=2; i<args.length; i++){
				if (i>2) value += " ";
				value += args[i];
			}
			if (type.equals("remove")) {
				if (hasPermission(sender, "tab.remove")) {
					Configs.config.set("Users." + player, null);
					Configs.config.save();
					ITabPlayer pl = Shared.getPlayer(player);
					if (pl != null) {
						pl.updateAll();
						pl.forceUpdateDisplay();
						if (Shared.features.containsKey("nametagx")) pl.restartArmorStands();
					}
					sendMessage(sender, Configs.data_removed.replace("%category%", "player").replace("%value%", player));
				}
				return;
			}
			for (String property : usualProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, player, type, value);
					} else {
						sendMessage(sender, Configs.no_perm);
					}
					return;
				}
			}
			for (String property : extraProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, player, type, value);
						if (!Shared.features.containsKey("nametagx")) {
							sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
						}
					} else {
						sendMessage(sender, Configs.no_perm);
					}
					return;
				}
			}
		}
		sendMessage(sender, "&cSyntax&8: &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>");
		sendMessage(sender, "&7Valid Properties are:");
		sendMessage(sender, " - &9tabprefix&3/&9tabsuffix&3/&9tabname");
		sendMessage(sender, " - &9tagprefix&3/&9tagsuffix&3/&9tagname");
		sendMessage(sender, " - &9belowname&3/&9abovename");
	}
	public static void savePlayer(ITabPlayer sender, String player, String type, String value){
		ITabPlayer pl = Shared.getPlayer(player);
		if (value.length() == 0) value = null;
		Configs.config.set("Users." + player + "." + type, value);
		Configs.config.save();
		if (pl != null) {
			pl.updateAll();
			pl.forceUpdateDisplay();
		}
		if (value != null){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player).replace("%category%", "player"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", player).replace("%category%", "player"));
		}
	}
	@Override
	public Object complete(ITabPlayer sender, String currentArgument) {
		// TODO Auto-generated method stub
		return null;
	}
}