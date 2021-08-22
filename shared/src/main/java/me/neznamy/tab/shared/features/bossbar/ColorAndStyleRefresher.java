package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.shared.PropertyUtils;

/**
 * An implementation of Refreshable for bossbar color and style
 */
public class ColorAndStyleRefresher extends TabFeature {

	private final String colorProperty;
	private final String styleProperty;
	
	//bossbar line this text belongs to
	private BossBarLine line;
	
	/**
	 * Constructs new instance with given parameter
	 * @param line - bossbar line this text belongs to
	 */
	public ColorAndStyleRefresher(BossBarLine line) {
		super("BossBar");
		this.line = line;
		colorProperty = PropertyUtils.bossbarColor(line.getName());
		styleProperty = PropertyUtils.bossbarStyle(line.getName());
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), 
				line.parseColor(refreshed.getProperty(colorProperty).updateAndGet()), 
				line.parseStyle(refreshed.getProperty(styleProperty).updateAndGet())), "BossBar - Color and style");
	}
}