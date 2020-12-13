package me.neznamy.tab.shared.features.bossbar;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;

/**
 * An implementation of Refreshable for bossbar progress
 */
public class ProgressRefresher implements Refreshable {

	private BossBarLine line;
	private List<String> usedPlaceholders;
	
	public ProgressRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!refreshed.getActiveBossBars().contains(line)) return;
		refreshed.sendCustomPacket(PacketPlayOutBoss.UPDATE_PCT(line.uuid, (float)line.parseProgress(refreshed.getProperty("bossbar-progress-" + line.name).updateAndGet())/100));
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(line.progress);
	}
	
	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}