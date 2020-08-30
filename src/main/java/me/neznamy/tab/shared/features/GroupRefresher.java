package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
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
				for (ITabPlayer all : Shared.getPlayers()) {
					all.updateGroupIfNeeded(true);
				}
			}
		});
	}
}
