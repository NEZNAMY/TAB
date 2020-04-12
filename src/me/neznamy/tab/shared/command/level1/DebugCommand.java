package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

public class DebugCommand extends SubCommand {

	public DebugCommand() {
		super("debug", "tab.debug");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		ITabPlayer analyzed = null;
		if (args.length > 0) {
			analyzed = Shared.getPlayer(args[0]);
			if (analyzed == null) {
				sendMessage(sender, Configs.player_not_found);
				return;
			}
		}
		debug(sender, analyzed);
	}
	private void debug(ITabPlayer sender, ITabPlayer analyzed) {
		if (analyzed == null && sender != null) {
			analyzed = Shared.getPlayer(sender.getUniqueId());
		}
		sendMessage(sender, "&3[TAB] &a&lShowing debug information");
		sendMessage(sender, "&7&m>-------------------------------<");
		sendMessage(sender, "&6Server version: &a" + Shared.mainClass.getServerVersion());
		sendMessage(sender, "&6Plugin version: &a" + Shared.pluginVersion + (Premium.is() ? " Premium" : ""));
		sendMessage(sender, "&6PlaceholderAPI: &a" + (PluginHooks.placeholderAPI? "Yes" : "No"));
		sendMessage(sender, "&6Found Permission system: &a" + Shared.mainClass.getPermissionPlugin());
		if (Configs.groupsByPermissions) {
			sendMessage(sender, "&6Permission group choice logic: &8&mPrimary group&8 / &r&8&mChoose from list&8 / &aPermissions");
		} else if (Configs.usePrimaryGroup) {
			sendMessage(sender, "&6Permission group choice logic: &aPrimary group&8 / &r&8&mChoose from list&8 / &r&8&mPermissions");
		} else {
			sendMessage(sender, "&6Permission group choice logic: &8&mPrimary group&r&8 / &aChoose from list&8 / &r&8&mPermissions");
		}

		boolean sorting = Shared.features.containsKey("nametag16") || Shared.features.containsKey("nametagx");
		String sortingType;

		if (sorting) {
			if (Premium.is()) {
				sortingType = Premium.sortingType.toString();
				if (sortingType.contains("PLACEHOLDER")) sortingType += " - " + Premium.sortingPlaceholder;
			} else {
				if (Configs.sortedGroups.isEmpty()) {
					sortingType = "Tabprefix";
				} else {
					if (Configs.sortByPermissions) {
						sortingType = "Permissions &c(this option was enabled by user, it is disabled by default!)";
					} else {
						sortingType = "Groups";
					}
				}
			}
		} else {
			sortingType = "&cDISABLED";
		}
		sendMessage(sender, "&6Sorting system: &a" + sortingType);
		sendMessage(sender, "&7&m>-------------------------------<");
		if (analyzed != null) {
			sendMessage(sender, "&ePlayer: &a" + analyzed.getName());
			if (Configs.groupsByPermissions) {
				sendMessage(sender, "&eHighest permission for group: &a" + analyzed.getGroup());
			} else if (Configs.usePrimaryGroup) {
				sendMessage(sender, "&ePrimary permission group: &a" + analyzed.getGroup());
			} else {
				sendMessage(sender, "&eFull permission group list: &a" + Arrays.toString(analyzed.getGroupsFromPermPlugin()));
				sendMessage(sender, "&eChosen group: &a" + analyzed.getGroup());
			}
			
			if (sorting) {
				if (analyzed.disabledNametag) {
					sendMessage(sender, "&eTeam name: &cSorting disabled in player's world");
				} else {
					sendMessage(sender, "&eTeam name: &a" + analyzed.getTeamName());
				}
			}
			if (Shared.features.containsKey("playerlist")) {
				if (analyzed.disabledTablistNames) {
					sendMessage(sender, "&9tabprefix: &cDisabled in player's world");
					sendMessage(sender, "&9tabsuffix: &cDisabled in player's world");
					sendMessage(sender, "&9tabname: &cDisabled in player's world");
				} else {
					sendMessage(sender, "&9tabprefix: &b" + analyzed.properties.get("tabprefix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tabprefix").isStatic() + ")");
					sendMessage(sender, "&9tabsuffix: &b" + analyzed.properties.get("tabsuffix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tabsuffix").isStatic() + ")");
					sendMessage(sender, "&9tabname: &b" + analyzed.properties.get("customtabname").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("customtabname").isStatic() + ")");
				}
			} else {
				sendMessage(sender, "&9tabprefix: &cDisabled");
				sendMessage(sender, "&9tabsuffix: &cDisabled");
				sendMessage(sender, "&9tabname: &cDisabled");
			}
			if (Shared.features.containsKey("nametag16") || Shared.features.containsKey("nametagx")) {
				if (analyzed.disabledNametag) {
					sendMessage(sender, "&9tagprefix: &cDisabled in player's world");
					sendMessage(sender, "&9tagsuffix: &cDisabled in player's world");
				} else {
					sendMessage(sender, "&9tagprefix: &b" + analyzed.properties.get("tagprefix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tagprefix").isStatic() + ")");
					sendMessage(sender, "&9tagsuffix: &b" + analyzed.properties.get("tagsuffix").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("tagsuffix").isStatic() + ")");
				}
			} else {
				sendMessage(sender, "&9tagprefix: &cDisabled");
				sendMessage(sender, "&9tagsuffix: &cDisabled");
			}
			if (Shared.features.containsKey("nametagx")) {
				if (analyzed.disabledNametag) {
					sendMessage(sender, "&9abovename: &cDisabled in player's world");
					sendMessage(sender, "&9belowname: &cDisabled in player's world");
					sendMessage(sender, "&9tagname: &cDisabled in player's world");
				} else {
					sendMessage(sender, "&9abovename: &b" + analyzed.properties.get("abovename").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("abovename").isStatic() + ")");
					sendMessage(sender, "&9belowname: &b" + analyzed.properties.get("belowname").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("belowname").isStatic() + ")");
					sendMessage(sender, "&9tagname: &b" + analyzed.properties.get("customtagname").getCurrentRawValue() + " &7(static=" + analyzed.properties.get("customtagname").isStatic() + ")");
				}
			}
		}
	}
	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		return arguments.length == 1 ? getPlayers(arguments[0]) : new ArrayList<String>();
	}
}