package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.Playerlist;

/**
 * Handler for "/tab debug" subcommand
 */
public class DebugCommand extends SubCommand {

	public DebugCommand() {
		super("debug", "tab.debug");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		TabPlayer analyzed = null;
		if (args.length > 0) {
			analyzed = TAB.getInstance().getPlayer(args[0]);
			if (analyzed == null) {
				sendMessage(sender, getTranslation("player_not_found"));
				return;
			}
		}
		if (analyzed == null && sender != null) {
			analyzed = TAB.getInstance().getPlayer(sender.getUniqueId());
		}
		debug(sender, analyzed);
	}

	/**
	 * Peforms debug on player and displays output
	 * @param sender - command sender or null if console
	 * @param analyzed - player to be analyzed
	 */
	@SuppressWarnings("unchecked")
	private void debug(TabPlayer sender, TabPlayer analyzed) {
		TAB tab = TAB.getInstance();
		GroupRefresher group = (GroupRefresher) tab.getFeatureManager().getFeature("group");
		sendMessage(sender, "&3[TAB] &a&lShowing debug information");
		sendMessage(sender, "&7&m>-------------------------------<");
		sendMessage(sender, "&6Server version: &a" + tab.getPlatform().getServerVersion());
		sendMessage(sender, "&6Plugin version: &a" + tab.getPluginVersion() + (tab.isPremium() ? " Premium" : ""));
		if (tab.getErrorManager().errorLog.exists()) {
			sendMessage(sender, "&6" + tab.getErrorManager().errorLog.getPath() + " size: &c" + tab.getErrorManager().errorLog.length()/1024 + "KB");
		}
		sendMessage(sender, "&6Permission plugin: &a" + tab.getPermissionPlugin().getName());
		if (group.groupsByPermissions) {
			sendMessage(sender, "&6Permission group choice logic: &8&mPrimary group&8 / &r&8&mChoose from list&8 / &aPermissions");
		} else if (group.usePrimaryGroup) {
			sendMessage(sender, "&6Permission group choice logic: &aPrimary group&8 / &r&8&mChoose from list&8 / &r&8&mPermissions");
		} else {
			sendMessage(sender, "&6Permission group choice logic: &8&mPrimary group&r&8 / &aChoose from list&8 / &r&8&mPermissions");
		}

		NameTag nametag = tab.getFeatureManager().getNameTagFeature();
		String sortingType;

		if (nametag != null) {
			if (tab.isPremium()) {
				sortingType = nametag.sorting.typesToString();
				if (sortingType.contains("PLACEHOLDER")) sortingType += " - " + nametag.sorting.sortingPlaceholder;
			} else if (nametag.sorting.sorting.get(0).getClass().getSimpleName().equals("GroupPermission")) {
				sortingType = "Permissions &c(this option was enabled by user, it is disabled by default!)";
			} else {
				sortingType = "Groups";
			}
		} else {
			sortingType = "&cDISABLED";
		}
		sendMessage(sender, "&6Sorting system: &a" + sortingType);
		sendMessage(sender, "&7&m>-------------------------------<");
		if (analyzed == null) return;
		sendMessage(sender, "&ePlayer: &a" + analyzed.getName());
		if (group.groupsByPermissions) {
			sendMessage(sender, "&eHighest permission for group: &a" + analyzed.getGroup());
		} else if (group.usePrimaryGroup) {
			sendMessage(sender, "&ePrimary permission group: &a" + analyzed.getGroup());
		} else {
			try {
				sendMessage(sender, "&eFull permission group list: &a" + Arrays.toString(tab.getPermissionPlugin().getAllGroups(analyzed)));
			} catch (Throwable e) {
				sendMessage(sender, "&eFull permission group list: &a[]");
			}
			sendMessage(sender, "&eChosen group: &a" + analyzed.getGroup());
		}

		if (nametag != null) {
			if (tab.getFeatureManager().getNameTagFeature().isDisabledWorld(analyzed.getWorldName())) {
				sendMessage(sender, "&eTeam name: &cSorting disabled in player's " + tab.getPlatform().getSeparatorType());
			} else {
				sendMessage(sender, "&eTeam name: &a" + analyzed.getTeamName());
				if (analyzed.getTeamNameNote() != null) sendMessage(sender, "&eTeam name note: &a" + analyzed.getTeamNameNote());
			}
		}
		if (tab.getFeatureManager().isFeatureEnabled("playerlist")) {
			Playerlist playerlist = (Playerlist) tab.getFeatureManager().getFeature("playerlist");
			boolean disabledPlayerlist = playerlist.isDisabledWorld(playerlist.disabledWorlds, analyzed.getWorldName());
			showProperty(sender, analyzed, "tabprefix", disabledPlayerlist);
			showProperty(sender, analyzed, "tabsuffix", disabledPlayerlist);
			showProperty(sender, analyzed, "customtabname", disabledPlayerlist);
		} else {
			sendMessage(sender, "&atabprefix: &cDisabled");
			sendMessage(sender, "&atabsuffix: &cDisabled");
			sendMessage(sender, "&atabname: &cDisabled");
		}
		if (tab.getFeatureManager().getNameTagFeature() != null) {
			boolean disabledNametags = tab.getFeatureManager().getNameTagFeature().isDisabledWorld(analyzed.getWorldName());
			showProperty(sender, analyzed, "tagprefix", disabledNametags);
			showProperty(sender, analyzed, "tagsuffix", disabledNametags);
			if (tab.getFeatureManager().isFeatureEnabled("nametagx")) {
				showProperty(sender, analyzed, "customtagname", disabledNametags);
				List<Object> lines;
				if (tab.isPremium()) {
					lines = Lists.newArrayList((List<Object>) tab.getConfiguration().premiumconfig.getObject("unlimited-nametag-mode-dynamic-lines"));
					lines.addAll(tab.getConfiguration().premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines").keySet());
				} else {
					lines = Arrays.asList("belowname", "nametag", "abovename");
				}
				for (Object line : lines) {
					if (line.toString().equals("nametag")) continue;
					showProperty(sender, analyzed, line+"", disabledNametags);
				}
			}
		} else {
			sendMessage(sender, "&atagprefix: &cDisabled");
			sendMessage(sender, "&atagsuffix: &cDisabled");
		}
	}

	/**
	 * Shows value and source of player's property
	 * @param sender - command sender or null if console
	 * @param analyzed - analyzed player
	 * @param property - property name
	 * @param disabled - if feature the property belongs to is disabled or not
	 */
	private void showProperty(TabPlayer sender, TabPlayer analyzed, String property, boolean disabled) {
		if (disabled) {
			sendMessage(sender, "&a" + property + ": &cDisabled in player's " + TAB.getInstance().getPlatform().getSeparatorType());
		} else {
			Property pr = analyzed.getProperty(property);
			String rawValue = pr.getCurrentRawValue().replace('\u00a7', '&');
			String value = TAB.getInstance().getPlaceholderManager().color("&a" + property + ": &e\"&r%rawValue%&r&e\" &7(" + rawValue.length() + ") &9(Source: " + pr.getSource() + ")").replace("%rawValue%", rawValue);
			sendRawMessage(sender, value);
		}
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		return arguments.length == 1 ? getPlayers(arguments[0]) : new ArrayList<String>();
	}
}