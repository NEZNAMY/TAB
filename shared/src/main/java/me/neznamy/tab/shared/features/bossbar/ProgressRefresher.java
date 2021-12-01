package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.shared.TabConstants;

/**
 * An implementation of Refreshable for BossBar progress
 */
public class ProgressRefresher extends TabFeature {

	private final String progressProperty;
	
	//BossBar line this text belongs to
	private final BossBarLine line;

	/**
	 * Constructs new instance with given parameter
	 * @param line - BossBar line this text belongs to
	 */
	public ProgressRefresher(BossBarLine line) {
		super("BossBar", "Updating progress");
		this.line = line;
		progressProperty = TabConstants.Property.bossbarProgress(line.getName());
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), line.parseProgress(refreshed.getProperty(progressProperty).updateAndGet())/100), TabConstants.PacketCategory.BOSSBAR_PROGRESS);
	}
}