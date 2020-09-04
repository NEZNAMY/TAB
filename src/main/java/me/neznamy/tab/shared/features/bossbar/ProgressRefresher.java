package me.neznamy.tab.shared.features.bossbar;

import java.util.Set;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * An implementation of Refreshable for bossbar progress
 */
public class ProgressRefresher implements Refreshable {

	private BossBarLine line;
	private Set<String> usedPlaceholders;
	
	public ProgressRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (!refreshed.activeBossBars.contains(line)) return;
		refreshed.sendCustomPacket(PacketPlayOutBoss.UPDATE_PCT(line.uuid, (float)line.parseProgress(refreshed.getProperty("bossbar-progress-" + line.name).updateAndGet())/100));
	}

	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(line.progress);
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}