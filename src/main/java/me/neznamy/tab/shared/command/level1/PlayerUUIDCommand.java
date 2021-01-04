package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * Handler for "/tab playeruuid" subcommand
 */
public class PlayerUUIDCommand extends SubCommand {

	public PlayerUUIDCommand() {
		super("playeruuid", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		//<uuid> <property> [value...]
		if (args.length > 1) {
			String name = args[0];
			TabPlayer changed = Shared.getPlayer(name);
			if (changed == null) {
				sendMessage(sender, Configs.player_not_found);
				return;
			}
			String type = args[1].toLowerCase();
			String value = buildArgument(Arrays.copyOfRange(args, 2, args.length));
			if (type.equals("remove")) {
				if (hasPermission(sender, "tab.remove")) {
					Configs.config.set("Users." + changed.getUniqueId().toString(), null);
					changed.forceRefresh();
					sendMessage(sender, Configs.data_removed.replace("%category%", "player").replace("%value%", changed.getName() + "(" + changed.getUniqueId().toString() + ")"));
				}
				return;
			}
			for (String property : allProperties) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, changed, type, value);
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
	 * Saves new player settings into config
	 * @param sender - command sender or null if console
	 * @param player - affected player
	 * @param type - property type
	 * @param value - new value
	 */
	public void savePlayer(TabPlayer sender, TabPlayer player, String type, String value){
		Configs.config.set("Users." + player.getUniqueId() + "." + type, value.length() == 0 ? null : value);
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(value);
		player.forceRefresh();
		if (value.length() > 0){
			sendMessage(sender, Configs.value_assigned.replace("%type%", type).replace("%value%", value).replace("%unit%", player.getName() + "(" + player.getUniqueId().toString() + ")").replace("%category%", "UUID"));
		} else {
			sendMessage(sender, Configs.value_removed.replace("%type%", type).replace("%unit%", player.getName() + "(" + player.getUniqueId().toString() + ")").replace("%category%", "UUID"));
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getPlayers(arguments[0]);
		List<String> suggestions = new ArrayList<String>();
		if (arguments.length == 2) {
			for (String property : allProperties) {
				if (property.startsWith(arguments[1].toLowerCase())) suggestions.add(property);
			}
		}
		return suggestions;
	}
}