package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.TabFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;

/**
 * An implementation of Refreshable for bossbar progress
 */
public class ProgressRefresher extends TabFeature {

	//bossbar line this text belongs to
	private BossBarLine line;

	/**
	 * Constructs new instance with given parameter
	 * @param line - bossbar line this text belongs to
	 */
	public ProgressRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), line.parseProgress(refreshed.getProperty("bossbar-progress-" + line.getName()).updateAndGet())/100), getFeatureType());
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(line.getProgress());
	}

	@Override
	public String getFeatureType() {
		return "BossBar";
	}
}