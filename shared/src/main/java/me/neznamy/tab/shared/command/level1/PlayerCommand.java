package me.neznamy.tab.shared.command.level1;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;

/**
 * Handler for "/tab player" subcommand
 */
public class PlayerCommand extends SubCommand {
	
	/**
	 * Constructs new instance
	 */
	public PlayerCommand() {
		super("player", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		//<name> <property> [value...]
		if (args.length > 1) {
			String player = args[0];
			String type = args[1].toLowerCase();
			String value = buildArgument(Arrays.copyOfRange(args, 2, args.length));
			if (type.equals("remove")) {
				if (hasPermission(sender, "tab.remove")) {
					TAB.getInstance().getConfiguration().getConfig().set("Users." + player, null);
					TabPlayer pl = TAB.getInstance().getPlayer(player);
					if (pl != null) {
						pl.forceRefresh();
					}
					sendMessage(sender, getTranslation("data_removed").replace("%category%", "player").replace("%value%", player));
				}
				return;
			}
			for (String property : getAllProperties()) {
				if (type.equals(property)) {
					if (hasPermission(sender, "tab.change." + property)) {
						savePlayer(sender, player, type, value);
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
	 * Saves new player settings into config
	 * @param sender - command sender or null if console
	 * @param player - affected player
	 * @param type - property type
	 * @param value - new value
	 */
	public void savePlayer(TabPlayer sender, String player, String type, String value){
		if (value.length() > 0){
			sendMessage(sender, getTranslation("value_assigned").replace("%type%", type).replace("%value%", value).replace("%unit%", player).replace("%category%", "player"));
		} else {
			sendMessage(sender, getTranslation("value_removed").replace("%type%", type).replace("%unit%", player).replace("%category%", "player"));
		}
		if (String.valueOf(value.length() == 0 ? null : value).equals(String.valueOf(TAB.getInstance().getConfiguration().getConfig().getObject("Users." + player + "." + type)))) return;
		TAB.getInstance().getConfiguration().getConfig().set("Users." + player + "." + type, value.length() == 0 ? null : value);
		TAB.getInstance().getPlaceholderManager().checkForRegistration(value);
		TabPlayer pl = TAB.getInstance().getPlayer(player);
		if (pl != null) {
			pl.forceRefresh();
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getPlayers(arguments[0]);
		return super.complete(sender, arguments);
	}
}