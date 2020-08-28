package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;

/**
 * Permission group refresher
 */
public class GroupRefresher {

	public GroupRefresher() {
		Shared.featureCpu.startRepeatingMeasuredTask(1000, "refreshing permission groups", CPUFeature.GROUP_REFRESHING, new Runnable() {

			@Override
			public void run() {
				for (ITabPlayer all : Shared.getPlayers()) {
					all.updateGroupIfNeeded(true);
				}
			}
		});
	}
}
