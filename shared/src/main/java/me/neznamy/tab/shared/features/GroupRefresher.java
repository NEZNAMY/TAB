package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Permission group refresher
 */
public class GroupRefresher implements Feature {

	private TAB tab;
	public boolean groupsByPermissions;
	public boolean usePrimaryGroup;
	private List<String> primaryGroupFindingList;
	
	public GroupRefresher(TAB tab) {
		this.tab = tab;
		usePrimaryGroup = tab.getConfiguration().config.getBoolean("use-primary-group", true);
		groupsByPermissions = tab.getConfiguration().config.getBoolean("assign-groups-by-permissions", false);
		primaryGroupFindingList = new ArrayList<String>();
		for (Object group : tab.getConfiguration().config.getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"))){
			primaryGroupFindingList.add(group.toString());
		}
		tab.getCPUManager().startRepeatingMeasuredTask(1000, "refreshing permission groups", getFeatureType(), UsageType.REPEATING_TASK, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer p : tab.getPlayers()) {
					((ITabPlayer) p).setGroup(detectPermissionGroup(p), true); 
				}
			}
		});
	}

	public String detectPermissionGroup(TabPlayer p) {
		if (groupsByPermissions) {
			return getByPermission(p);
		}
		if (usePrimaryGroup) {
			return getByPrimary(p);
		}
		return getFromList(p);
	}

	public String getByPrimary(TabPlayer p) {
		try {
			return tab.getPermissionPlugin().getPrimaryGroup(p);
		} catch (Throwable e) {
			return tab.getErrorManager().printError("<null>", "Failed to get permission group of " + p.getName() + " using " + tab.getPermissionPlugin().getName() + " v" + tab.getPermissionPlugin().getVersion(), e);
		}
	}

	public String getFromList(TabPlayer p) {
		try {
			String[] playerGroups = tab.getPermissionPlugin().getAllGroups(p);
			if (playerGroups != null && playerGroups.length > 0) {
				for (String groupFromList : primaryGroupFindingList) {
					for (String playerGroup : playerGroups) {
						if (playerGroup.equalsIgnoreCase(groupFromList)) {
							return playerGroup;
						}
					}
				}
				return playerGroups[0];
			} else {
				return "<null>";
			}
		} catch (Throwable e) {
			return tab.getErrorManager().printError("<null>", "Failed to get permission groups of " + p.getName() + " using " + tab.getPermissionPlugin().getName() + " v" + tab.getPermissionPlugin().getVersion(), e);
		}
	}

	public String getByPermission(TabPlayer p) {
		for (Object group : primaryGroupFindingList) {
			if (p.hasPermission("tab.group." + group)) {
				return String.valueOf(group);
			}
		}
		tab.getErrorManager().oneTimeConsoleError("Player " + p.getName() + " does not have any group permission while assign-groups-by-permissions is enabled! Did you forget to add his group to primary-group-finding-list?");
		return "<null>";
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.GROUP_REFRESHING;
	}
}