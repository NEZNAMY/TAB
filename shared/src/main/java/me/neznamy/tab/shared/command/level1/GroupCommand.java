package me.neznamy.tab.shared.command.level1;

import java.util.Arrays;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Handler for "/tab group" subcommand
 */
public class GroupCommand extends PropertyCommand {
	
	/**
	 * Constructs new instance
	 */
	public GroupCommand() {
		super("group", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		//<name> <property> [value...]
		if (args.length > 1) {
			String group = args[0];
			String type = args[1].toLowerCase();
			String value = buildArgument(Arrays.copyOfRange(args, 2, args.length));
			if (type.equals("remove")) {
				if (hasPermission(sender, "tab.remove")) {
					TAB.getInstance().getConfiguration().getConfig().set("Groups." + group, null);
					for (TabPlayer pl : TAB.getInstance().getPlayers()) {
						if (pl.getGroup().equals(group) || group.equals("_OTHER_")){
							pl.forceRefresh();
						}
					}
					sendMessage(sender, getTranslation("data_removed").replace("%category%", "group").replace("%value%", group));
				}
				return;
			}
			for (String property : getAllProperties()) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						saveGroup(sender, group, type, value);
						if (extraProperties.contains(property) && !TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx")) {
							sendMessage(sender, getTranslation("unlimited_nametag_mode_not_enabled"));
						}
					} else {
						sendMessage(sender, getTranslation("no_permission"));
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
		if (value.length() > 0){
			sendMessage(sender, getTranslation("value_assigned").replace("%type%", type).replace("%value%", value).replace("%unit%", group).replace("%category%", "group"));
		} else {
			sendMessage(sender, getTranslation("value_removed").replace("%type%", type).replace("%unit%", group).replace("%category%", "group"));
		}
		if (String.valueOf(value.length() == 0 ? null : value).equals(String.valueOf(TAB.getInstance().getConfiguration().getConfig().getObject("Groups." + group.replace(".", "@#@") + "." + type)))) return;
		TAB.getInstance().getConfiguration().getConfig().set("Groups." + group.replace(".", "@#@") + "." + type, value.length() == 0 ? null : value);
		TAB.getInstance().getPlaceholderManager().checkForRegistration(value);
		for (TabPlayer pl : TAB.getInstance().getPlayers()) {
			if (pl.getGroup().equals(group) || group.equals("_OTHER_")){
				pl.forceRefresh();
			}
		}
	}
}