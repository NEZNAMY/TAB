package me.neznamy.tab.shared.features.bossbar;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;

/**
 * An implementation of Refreshable for bossbar text
 */
public class TextRefresher implements Refreshable {

	private BossBarLine line;
	private List<String> usedPlaceholders;
	
	public TextRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!refreshed.getActiveBossBars().contains(line)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.uuid, refreshed.getProperty("bossbar-title-" + line.name).updateAndGet()));
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(line.title);
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}