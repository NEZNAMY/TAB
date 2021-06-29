package me.neznamy.tab.shared.features.bossbar;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;

/**
 * An implementation of Refreshable for bossbar progress
 */
public class ProgressRefresher implements Refreshable {

	//bossbar line this text belongs to
	private BossBarLine line;
	
	//list of used placeholders in text
	private List<String> usedPlaceholders;
	
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
		if (!refreshed.getActiveBossBars().contains(line)) return;
		refreshed.sendCustomPacket(new PacketPlayOutBoss(line.getUuid(), line.parseProgress(refreshed.getProperty("bossbar-progress-" + line.getName()).updateAndGet())/100), TabFeature.BOSSBAR);
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(line.getProgress());
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}