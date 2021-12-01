package me.neznamy.tab.shared.features.bossbar;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.shared.TabConstants;

/**
 * An implementation of Refreshable for BossBar text
 */
public class TextRefresher extends TabFeature {

	private final String textProperty;
	
	//BossBar line this text belongs to
	private final BossBarLine line;
	
	/**
	 * Constructs new instance with given parameter
	 * @param line - BossBar line this text belongs to
	 */
	public TextRefresher(BossBarLine line) {
		super("BossBar", "Updating text");
		this.line = line;
		textProperty = TabConstants.Property.bossbarTitle(line.getName());
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!line.getPlayers().contains(refreshed)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUniqueId(), refreshed.getProperty(textProperty).updateAndGet()), TabConstants.PacketCategory.BOSSBAR_TEXT);
	}
}