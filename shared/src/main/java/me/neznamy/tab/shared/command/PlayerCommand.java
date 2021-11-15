package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * Handler for "/tab player" subcommand
 */
public class PlayerCommand extends PropertyCommand {
	
	/**
	 * Constructs new instance
	 */
	public PlayerCommand() {
		super("player", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		//<name> <property> [value...]
		if (args.length <= 1) {
			help(sender);
			return;
		}
		String player = args[0];
		String type = args[1].toLowerCase();
		String value = buildArgument(Arrays.copyOfRange(args, 2, args.length));
		if ("remove".equals(type)) {
			if (hasPermission(sender, TabConstants.Permission.COMMAND_DATA_REMOVE)) {
				TAB.getInstance().getConfiguration().getUsers().remove(player);
				TabPlayer pl = TAB.getInstance().getPlayer(player);
				if (pl != null) {
					pl.forceRefresh();
				}
				sendMessage(sender, getMessages().getPlayerDataRemoved(player));
			} else {
				sendMessage(sender, getMessages().getNoPermission());
			}
			return;
		}
		for (String property : getAllProperties()) {
			if (type.equals(property)) {
				if (hasPermission(sender, TabConstants.Permission.COMMAND_PROPERTY_CHANGE_PREFIX + property)) {
					savePlayer(sender, player, type, value);
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
	 * Saves new player settings into config
	 * @param sender - command sender or null if console
	 * @param player - affected player
	 * @param type - property type
	 * @param value - new value
	 */
	public void savePlayer(TabPlayer sender, String player, String type, String value){
		if (value.length() > 0){
			sendMessage(sender, getMessages().getPlayerValueAssigned(type, value, player));
		} else {
			sendMessage(sender, getMessages().getPlayerValueRemoved(type, player));
		}
		String[] property = TAB.getInstance().getConfiguration().getUsers().getProperty(player, type, null, null);
		if (property.length > 0 && String.valueOf(value.length() == 0 ? null : value).equals(String.valueOf(property[0]))) return;
		TAB.getInstance().getConfiguration().getUsers().setProperty(player, type, null, null, value.length() == 0 ? null : value);
		TabPlayer pl = TAB.getInstance().getPlayer(player);
		if (pl != null) {
			pl.forceRefresh();
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
		return super.complete(sender, arguments);
	}
}