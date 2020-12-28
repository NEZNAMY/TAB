package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.PlaceholderManager;
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
			analyzed = Shared.getPlayer(args[0]);
			if (analyzed == null) {
				sendMessage(sender, Configs.player_not_found);
				return;
			}
		}
		if (analyzed == null && sender != null) {
			analyzed = Shared.getPlayer(sender.getUniqueId());
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
		GroupRefresher group = (GroupRefresher) Shared.featureManager.getFeature("group");
		sendMessage(sender, "&3[TAB] &a&lShowing debug information");
		sendMessage(sender, "&7&m>-------------------------------<");
		sendMessage(sender, "&6Server version: &a" + Shared.platform.getServerVersion());
		sendMessage(sender, "&6Plugin version: &a" + Shared.pluginVersion + (Shared.isPremium() ? " Premium" : ""));
		if (Configs.errorFile.exists()) {
			sendMessage(sender, "&6" + Configs.errorFile.getPath() + " size: &c" + Configs.errorFile.length()/1024 + "KB");
		}
		sendMessage(sender, "&6Permission plugin: &a" + Shared.permissionPlugin.getName());
		if (group.groupsByPermissions) {
			sendMessage(sender, "&6Permission group choice logic: &8&mPrimary group&8 / &r&8&mChoose from list&8 / &aPermissions");
		} else if (group.usePrimaryGroup) {
			sendMessage(sender, "&6Permission group choice logic: &aPrimary group&8 / &r&8&mChoose from list&8 / &r&8&mPermissions");
		} else {
			sendMessage(sender, "&6Permission group choice logic: &8&mPrimary group&r&8 / &aChoose from list&8 / &r&8&mPermissions");
		}

		NameTag nametag = Shared.featureManager.getNameTagFeature();
		String sortingType;

		if (nametag != null) {
			if (Shared.isPremium()) {
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
				sendMessage(sender, "&eFull permission group list: &a" + Arrays.toString(Shared.permissionPlugin.getAllGroups(analyzed)));
			} catch (Throwable e) {
				sendMessage(sender, "&eFull permission group list: &a[]");
			}
			sendMessage(sender, "&eChosen group: &a" + analyzed.getGroup());
		}

		if (nametag != null) {
			if (Shared.featureManager.getNameTagFeature().isDisabledWorld(analyzed.getWorldName())) {
				sendMessage(sender, "&eTeam name: &cSorting disabled in player's " + Shared.platform.getSeparatorType());
			} else {
				sendMessage(sender, "&eTeam name: &a" + analyzed.getTeamName());
				if (analyzed.getTeamNameNote() != null) sendMessage(sender, "&eTeam name note: &a" + analyzed.getTeamNameNote());
			}
		}
		if (Shared.featureManager.isFeatureEnabled("playerlist")) {
			Playerlist playerlist = (Playerlist) Shared.featureManager.getFeature("playerlist");
			boolean disabledPlayerlist = playerlist.isDisabledWorld(playerlist.disabledWorlds, analyzed.getWorldName());
			showProperty(sender, analyzed, "tabprefix", disabledPlayerlist);
			showProperty(sender, analyzed, "tabsuffix", disabledPlayerlist);
			showProperty(sender, analyzed, "customtabname", disabledPlayerlist);
		} else {
			sendMessage(sender, "&atabprefix: &cDisabled");
			sendMessage(sender, "&atabsuffix: &cDisabled");
			sendMessage(sender, "&atabname: &cDisabled");
		}
		if (Shared.featureManager.getNameTagFeature() != null) {
			boolean disabledNametags = Shared.featureManager.getNameTagFeature().isDisabledWorld(analyzed.getWorldName());
			showProperty(sender, analyzed, "tagprefix", disabledNametags);
			showProperty(sender, analyzed, "tagsuffix", disabledNametags);
			if (Shared.featureManager.isFeatureEnabled("nametagx")) {
				showProperty(sender, analyzed, "customtagname", disabledNametags);
				List<Object> lines;
				if (Shared.isPremium()) {
					lines = Lists.newArrayList((List<Object>) Configs.premiumconfig.getObject("unlimited-nametag-mode-dynamic-lines"));
					lines.addAll(Configs.premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines").keySet());
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
			sendMessage(sender, "&a" + property + ": &cDisabled in player's " + Shared.platform.getSeparatorType());
		} else {
			Property pr = analyzed.getProperty(property);
			String rawValue = pr.getCurrentRawValue().replace(PlaceholderManager.colorChar, '&');
			String value = PlaceholderManager.color("&a" + property + ": &e\"&r%rawValue%&r&e\" &7(" + rawValue.length() + ") &9(Source: " + pr.getSource() + ")").replace("%rawValue%", rawValue);
			sendRawMessage(sender, value);
		}
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		return arguments.length == 1 ? getPlayers(arguments[0]) : new ArrayList<String>();
	}
}