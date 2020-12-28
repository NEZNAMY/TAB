package me.neznamy.tab.shared.features.bossbar;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;

/**
 * An implementation of Refreshable for bossbar color and style
 */
public class ColorAndStyleRefresher implements Refreshable {

	private BossBarLine line;
	private List<String> usedPlaceholders;
	
	public ColorAndStyleRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!refreshed.getActiveBossBars().contains(line)) return;
		Property color = refreshed.getProperty("bossbar-color-" + line.name);
		Property style = refreshed.getProperty("bossbar-style-" + line.name);
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.uuid, line.parseColor(color.updateAndGet()), line.parseStyle(style.updateAndGet())));
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(line.color);
		usedPlaceholders.addAll(PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(line.style));
	}
	
	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}