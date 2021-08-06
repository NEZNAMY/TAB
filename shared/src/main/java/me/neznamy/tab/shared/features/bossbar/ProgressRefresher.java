package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.shared.PropertyUtils;

/**
 * An implementation of Refreshable for bossbar progress
 */
public class ProgressRefresher extends TabFeature {

	private final String progressProperty;
	
	//bossbar line this text belongs to
	private BossBarLine line;

	/**
	 * Constructs new instance with given parameter
	 * @param line - bossbar line this text belongs to
	 */
	public ProgressRefresher(BossBarLine line) {
		super("BossBar");
		this.line = line;
		progressProperty = PropertyUtils.bossbarProgress(line.getName());
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), line.parseProgress(refreshed.getProperty(progressProperty).updateAndGet())/100), this);
	}
}