package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;

/**
 * An implementation of Refreshable for bossbar color and style
 */
public class ColorAndStyleRefresher extends TabFeature {

	//bossbar line this text belongs to
	private BossBarLine line;
	
	/**
	 * Constructs new instance with given parameter
	 * @param line - bossbar line this text belongs to
	 */
	public ColorAndStyleRefresher(BossBarLine line) {
		super("BossBar");
		this.line = line;
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), 
				line.parseColor(refreshed.getProperty("bossbar-color-" + line.getName()).updateAndGet()), 
				line.parseStyle(refreshed.getProperty("bossbar-style-" + line.getName()).updateAndGet())), this);
	}
}