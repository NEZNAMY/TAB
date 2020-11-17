package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * Permission group refresher
 */
public class GroupRefresher {

	public static boolean groupsByPermissions;
	public static boolean usePrimaryGroup;
	private static List<String> primaryGroupFindingList;
	
	public GroupRefresher() {
		usePrimaryGroup = Configs.config.getBoolean("use-primary-group", true);
		groupsByPermissions = Configs.config.getBoolean("assign-groups-by-permissions", false);
		primaryGroupFindingList = Configs.config.getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));
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
		if (groupsByPermissions) {
			return getByPermission(p);
		}
		if (usePrimaryGroup) {
			return getByPrimary(p);
		}
		return getFromList(p);
	}

	public static String getByPrimary(TabPlayer p) {
		try {
			return Shared.permissionPlugin.getPrimaryGroup(p);
		} catch (Throwable e) {
			return Shared.errorManager.printError("<null>", "Failed to get permission group of " + p.getName() + " using " + Shared.permissionPlugin.getName() + " v" + Shared.permissionPlugin.getVersion(), e);
		}
	}

	public static String getFromList(TabPlayer p) {
		try {
			String[] playerGroups = Shared.permissionPlugin.getAllGroups(p);
			if (playerGroups != null && playerGroups.length > 0) {
				for (Object groupFromList : primaryGroupFindingList) {
					for (String playerGroup : playerGroups) {
						if (playerGroup.equalsIgnoreCase(groupFromList + "")) {
							return playerGroup;
						}
					}
				}
				return playerGroups[0];
			} else {
				return "<null>";
			}
		} catch (Throwable e) {
			return Shared.errorManager.printError("<null>", "Failed to get permission groups of " + p.getName() + " using " + Shared.permissionPlugin.getName() + " v" + Shared.permissionPlugin.getVersion(), e);
		}
	}

	public static String getByPermission(TabPlayer p) {
		for (Object group : primaryGroupFindingList) {
			if (p.hasPermission("tab.group." + group)) {
				return String.valueOf(group);
			}
		}
		Shared.errorManager.oneTimeConsoleError("Player " + p.getName() + " does not have any group permission while assign-groups-by-permissions is enabled! Did you forget to add his group to primary-group-finding-list?");
		return "<null>";
	}
}