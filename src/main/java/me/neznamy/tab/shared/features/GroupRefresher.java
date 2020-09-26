package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * Permission group refresher
 */
public class GroupRefresher {

	public GroupRefresher() {
		Shared.cpu.startRepeatingMeasuredTask(1000, "refreshing permission groups", TabFeature.GROUP_REFRESHING, UsageType.REPEATING_TASK, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer p : Shared.getPlayers()) {
					p.setGroup(detectPermissionGroup(p), true); 
				}
			}
		});
	}

	public static String detectPermissionGroup(TabPlayer p) {
		if (Configs.groupsByPermissions) {
			return getByPermission(p);
		}
		if (Configs.usePrimaryGroup) {
			return getByPrimary(p);
		}
		return getFromList(p);
	}

	public static String getByPrimary(TabPlayer p) {
		try {
			return Shared.permissionPlugin.getPrimaryGroup(p);
		} catch (Throwable e) {
			return Shared.errorManager.printError("null", "Failed to get permission group of " + p.getName() + " using " + Shared.permissionPlugin.getName() + " v" + Shared.permissionPlugin.getVersion(), e);
		}
	}

	public static String getFromList(TabPlayer p) {
		try {
			String[] playerGroups = Shared.permissionPlugin.getAllGroups(p);
			if (playerGroups != null && playerGroups.length > 0) {
				for (Object groupFromList : Configs.primaryGroupFindingList) {
					for (String playerGroup : playerGroups) {
						if (playerGroup.equalsIgnoreCase(groupFromList + "")) {
							return playerGroup;
						}
					}
				}
				return playerGroups[0];
			} else {
				return "null";
			}
		} catch (Throwable e) {
			return Shared.errorManager.printError("null", "Failed to get permission groups of " + p.getName() + " using " + Shared.permissionPlugin.getName() + " v" + Shared.permissionPlugin.getVersion(), e);
		}
	}

	public static String getByPermission(TabPlayer p) {
		for (Object group : Configs.primaryGroupFindingList) {
			if (p.hasPermission("tab.group." + group)) {
				return String.valueOf(group);
			}
		}
		Shared.errorManager.oneTimeConsoleError("Player " + p.getName() + " does not have any group permission while assign-groups-by-permissions is enabled! Did you forget to add his group to primary-group-finding-list?");
		return "null";
	}
}