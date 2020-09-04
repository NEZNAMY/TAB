package me.neznamy.tab.shared.features.bossbar;

import java.util.Set;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * An implementation of Refreshable for bossbar text
 */
public class TextRefresher implements Refreshable {

	private BossBarLine line;
	private Set<String> usedPlaceholders;
	
	public TextRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (!refreshed.activeBossBars.contains(line)) return;
		refreshed.sendCustomPacket(PacketPlayOutBoss.UPDATE_NAME(line.uuid, refreshed.getProperty("bossbar-text-" + line.name).updateAndGet()));
	}

	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(line.text);
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}