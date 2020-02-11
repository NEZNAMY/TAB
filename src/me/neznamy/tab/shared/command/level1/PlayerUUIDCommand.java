package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

public class PlayerUUIDCommand extends SubCommand {
	
	private static final String[] usualProperties = {"tabprefix", "tabsuffix", "tagprefix", "tagsuffix", "customtabname"};
	private static final String[] extraProperties = {"abovename", "belowname", "customtagname"};
	
	public PlayerUUIDCommand() {
		super("playeruuid", null);
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		//<uuid> <property> [value...]
		if (args.length > 1) {
			String name = args[0];
			ITabPlayer changed = Shared.getPlayer(name);
			if (changed == null) {
				sendMessage(sender, Configs.player_not_found);
				return;
			}
			String type = args[1].toLowerCase();
			String value = "";
			for (int i=2; i<args.length; i++){
				if (i>2) value += " ";
				value += args[i];
			}
			if (type.equals("remove")) {
				if (hasPermission(sender, "tab.remove")) {
					Configs.config.set("Users." + changed.getUniqueId().toString(), null);
					Configs.config.save();
					changed.updateAll();
					changed.forceUpdateDisplay();
					if (Configs.unlimitedTags) changed.restartArmorStands();
					sendMessage(sender, Configs.data_removed.replace("%category%", "player").replace("%value%", changed.getName() + "(" + changed.getUniqueId().toString() + ")"));
				}
				return;
			}
			for (String property : usualProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, changed, type, value);
					} else {
						sendMessage(sender, Configs.no_perm);
					}
					return;
				}
			}
			for (String property : extraProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, changed, type, value);
						if (!Configs.unlimitedTags) {
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
	public static void savePlayer(ITabPlayer sender, ITabPlayer player, String type, String value){
		if (value.length() == 0) value = null;
		Configs.config.set("Users." + player.getUniqueId() + "." + type, value);
		Configs.config.save();
		player.updateAll();
		player.forceUpdateDisplay();
		if (value != null){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player.getName() + "(" + player.getUniqueId().toString() + ")").replace("%category%", "UUID"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", player.getName() + "(" + player.getUniqueId().toString() + ")").replace("%category%", "UUID"));
		}
	}
	@Override
	public Object complete(ITabPlayer sender, String currentArgument) {
		// TODO Auto-generated method stub
		return null;
	}
}