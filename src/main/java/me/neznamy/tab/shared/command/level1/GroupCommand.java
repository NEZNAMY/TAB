package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * Handler for "/tab group" subcommand
 */
public class GroupCommand extends SubCommand {
	
	public GroupCommand() {
		super("group", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		//<name> <property> [value...]
		if (args.length > 1) {
			String group = args[0];
			String type = args[1].toLowerCase();
			String value = "";
			for (int i=2; i<args.length; i++){
				if (i>2) value += " ";
				value += args[i];
			}
			if (type.equals("remove")) {
				if (hasPermission(sender, "tab.remove")) {
					Configs.config.set("Groups." + group, null);
					for (TabPlayer pl : Shared.getPlayers()) {
						if (pl.getGroup().equals(group) || group.equals("_OTHER_")){
							pl.forceRefresh();
						}
					}
					sendMessage(sender, Configs.data_removed.replace("%category%", "group").replace("%value%", group));
				}
				return;
			}
			for (String property : allProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						saveGroup(sender, group, type, value);
						if (extraProperties.contains(property) && !Shared.featureManager.isFeatureEnabled("nametagx")) {
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
	 * Saves new group settings into config
	 * @param sender - command sender or null if console
	 * @param group - affected group
	 * @param type - property type
	 * @param value - new value
	 */
	private void saveGroup(TabPlayer sender, String group, String type, String value){
		Configs.config.set("Groups." + group.replace(".", "@#@") + "." + type, value.length() == 0 ? null : value);
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(value);
		for (TabPlayer pl : Shared.getPlayers()) {
			if (pl.getGroup().equals(group) || group.equals("_OTHER_")){
				pl.forceRefresh();
			}
		}
		if (value.length() > 0){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", group).replace("%category%", "group"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", group).replace("%category%", "group"));
		}
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		List<String> suggestions = new ArrayList<String>();
		if (arguments.length == 2) {
			for (String property : allProperties) {
				if (property.startsWith(arguments[1].toLowerCase())) suggestions.add(property);
			}
		}
		return suggestions;
	}
}