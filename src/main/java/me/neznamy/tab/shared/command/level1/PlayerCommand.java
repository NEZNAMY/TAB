package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * Handler for "/tab player" subcommand
 */
public class PlayerCommand extends SubCommand {
	
	public PlayerCommand() {
		super("player", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
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
					TabPlayer pl = Shared.getPlayer(player);
					if (pl != null) {
						pl.forceRefresh();
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
						if (!Shared.featureManager.isFeatureEnabled("nametagx")) {
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
		sendMessage(sender, " - &9tabprefix&3/&9tabsuffix&3/&9customtabname");
		sendMessage(sender, " - &9tagprefix&3/&9tagsuffix&3/&9customtagname");
		sendMessage(sender, " - &9belowname&3/&9abovename");
	}
	
	/**
	 * Saves new player settings into config
	 * @param sender - command sender or null if console
	 * @param player - affected player
	 * @param type - property type
	 * @param value - new value
	 */
	public void savePlayer(TabPlayer sender, String player, String type, String value){
		TabPlayer pl = Shared.getPlayer(player);
		Configs.config.set("Users." + player + "." + type, value.length() == 0 ? null : value);
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(value);
		if (pl != null) {
			pl.forceRefresh();
		}
		if (value.length() > 0){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player).replace("%category%", "player"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", player).replace("%category%", "player"));
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getPlayers(arguments[0]);
		List<String> suggestions = new ArrayList<String>();
		if (arguments.length == 2) {
			for (String property : usualProperties) {
				if (property.startsWith(arguments[1].toLowerCase())) suggestions.add(property);
			}
			for (String property : extraProperties) {
				if (property.startsWith(arguments[1].toLowerCase())) suggestions.add(property);
			}
		}
		return suggestions;
	}
}