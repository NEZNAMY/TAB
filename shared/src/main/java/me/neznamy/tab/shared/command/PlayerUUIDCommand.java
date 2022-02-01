package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * Handler for "/tab playeruuid" subcommand
 */
public class PlayerUUIDCommand extends PropertyCommand {

	/**
	 * Constructs new instance
	 */
	public PlayerUUIDCommand() {
		super("playeruuid");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		//<uuid> <property> [value...]
		if (args.length <= 1) {
			help(sender);
			return;
		}

		TabPlayer changed = TAB.getInstance().getPlayer(args[0]);
		if (changed == null) {
			sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
			return;
		}
		String type = args[1].toLowerCase();
		String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		String world = null;
		String server = null;
		if (args[args.length-2].equals("-w")) {
			world = args[args.length-1];
			value = value.substring(0, value.length()-world.length()-4);
		}
		if (args[args.length-2].equals("-s")) {
			server = args[args.length-1];
			value = value.substring(0, value.length()-server.length()-4);
		}
		if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
			value = value.substring(1, value.length()-1);
		}
		if ("remove".equals(type)) {
			if (hasPermission(sender, TabConstants.Permission.COMMAND_DATA_REMOVE)) {
				TAB.getInstance().getConfiguration().getUsers().remove(changed.getUniqueId().toString());
				changed.forceRefresh();
				sendMessage(sender, getMessages().getPlayerDataRemoved(changed.getName() + "(" + changed.getUniqueId().toString() + ")"));
			} else {
				sendMessage(sender, getMessages().getNoPermission());
			}
			return;
		}
		for (String property : getAllProperties()) {
			if (type.equals(property)) {
				if (hasPermission(sender, TabConstants.Permission.COMMAND_PROPERTY_CHANGE_PREFIX + property)) {
					savePlayer(sender, changed, type, value, server, world);
					if (extraProperties.contains(property) && !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS)) {
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
	public void savePlayer(TabPlayer sender, TabPlayer player, String type, String value, String server, String world){
		if (value.length() > 0){
			sendMessage(sender, getMessages().getPlayerValueAssigned(type, value, player.getName() + "(" + player.getUniqueId().toString() + ")"));
		} else {
			sendMessage(sender, getMessages().getPlayerValueRemoved(type, player.getName() + "(" + player.getUniqueId().toString() + ")"));
		}
		String[] property = TAB.getInstance().getConfiguration().getUsers().getProperty(player.getUniqueId().toString(), type, server, world);
		if (property.length > 0 && String.valueOf(value.length() == 0 ? null : value).equals(String.valueOf(property[0]))) return;
		TAB.getInstance().getConfiguration().getUsers().setProperty(player.getUniqueId().toString(), type, server, world, value.length() == 0 ? null : value);
		player.forceRefresh();
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
		return super.complete(sender, arguments);
	}
}