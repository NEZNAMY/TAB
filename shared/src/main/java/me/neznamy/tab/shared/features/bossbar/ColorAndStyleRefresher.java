package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.shared.TabConstants;

/**
 * An implementation of Refreshable for BossBar color and style
 */
public class ColorAndStyleRefresher extends TabFeature {

	private final String colorProperty;
	private final String styleProperty;
	
	//BossBar line this text belongs to
	private final BossBarLine line;
	
	/**
	 * Constructs new instance with given parameter
	 * @param line - BossBar line this text belongs to
	 */
	public ColorAndStyleRefresher(BossBarLine line) {
		super("BossBar", "Updating color and style");
		this.line = line;
		colorProperty = TabConstants.Property.bossbarColor(line.getName());
		styleProperty = TabConstants.Property.bossbarStyle(line.getName());
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), 
				line.parseColor(refreshed.getProperty(colorProperty).updateAndGet()), 
				line.parseStyle(refreshed.getProperty(styleProperty).updateAndGet())), TabConstants.PacketCategory.BOSSBAR_COLOR_STYLE);
	}
}