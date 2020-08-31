package me.neznamy.tab.shared.features.bossbar;

import java.util.Set;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * An implementation of Refreshable for bossbar text
 */
public class TextRefresher implements Refreshable {

	private BossBarLine line;
	private Set<String> usedPlaceholders;
	
	public TextRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (!refreshed.activeBossBars.contains(line)) return;
		Property text = refreshed.getProperty("bossbar-text-" + line.name);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			refreshed.sendCustomPacket(PacketPlayOutBoss.UPDATE_NAME(line.uuid, text.updateAndGet()));
		} else {
			DataWatcher w = new DataWatcher();
			w.helper().setCustomName(text.updateAndGet(), refreshed.getVersion());
			refreshed.sendCustomBukkitPacket(new PacketPlayOutEntityMetadata(line.entityId, w));
		}
	}

	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(line.text);
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}