package me.neznamy.tab.shared.command;

import java.util.Arrays;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

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
		if (args.length <= 1) {
			help(sender);
			return;
		}
		String group = args[0];
		String type = args[1].toLowerCase();
		String value = buildArgument(Arrays.copyOfRange(args, 2, args.length));
		if ("remove".equals(type)) {
			if (hasPermission(sender, TabConstants.Permission.COMMAND_DATA_REMOVE)) {
				TAB.getInstance().getConfiguration().getGroups().remove(group);
				for (TabPlayer pl : TAB.getInstance().getOnlinePlayers()) {
					if (pl.getGroup().equals(group) || "_DEFAULT_".equals(group)){
						pl.forceRefresh();
					}
				}
				sendMessage(sender, getMessages().getGroupDataRemoved(group));
			} else {
				sendMessage(sender, getMessages().getNoPermission());
			}
			return;
		}
		for (String property : getAllProperties()) {
			if (type.equals(property)) {
				if (hasPermission(sender, TabConstants.Permission.COMMAND_PROPERTY_CHANGE_PREFIX + property)) {
					saveGroup(sender, group, type, value);
					if (extraProperties.contains(property) && !TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx")) {
						sendMessage(sender, getMessages().getUnlimitedNametagModeNotEnabled());
					}
				} else {
					sendMessage(sender, getMessages().getNoPermission());
				}
				return;
			}
		}
		help(sender);
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
			sendMessage(sender, getMessages().getGroupValueAssigned(type, value, group));
		} else {
			sendMessage(sender, getMessages().getGroupValueRemoved(type, group));
		}
		String[] property = TAB.getInstance().getConfiguration().getGroups().getProperty(group, type, null, null);
		if (property.length > 0 && String.valueOf(value.length() == 0 ? null : value).equals(String.valueOf(property[0]))) return;
		TAB.getInstance().getConfiguration().getGroups().setProperty(group, type, null, null, value.length() == 0 ? null : value);
		for (TabPlayer pl : TAB.getInstance().getOnlinePlayers()) {
			if (pl.getGroup().equals(group) || "_DEFAULT_".equals(group)){
				pl.forceRefresh();
			}
		}
	}
}