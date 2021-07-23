package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.TabFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;

/**
 * An implementation of Refreshable for bossbar text
 */
public class TextRefresher extends TabFeature {

	//bossbar line this text belongs to
	private BossBarLine line;
	
	/**
	 * Constructs new instance with given parameter
	 * @param line - bossbar line this text belongs to
	 */
	public TextRefresher(BossBarLine line) {
		super("Bossbar");
		this.line = line;
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), refreshed.getProperty("bossbar-title-" + line.getName()).updateAndGet()), getFeatureName());
	}
}