package me.neznamy.tab.shared.features.bossbar;

import java.util.Set;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class ProgressRefresher implements Refreshable {

	private BossBarLine line;
	private Set<String> usedPlaceholders;
	
	public ProgressRefresher(BossBarLine line) {
		this.line = line;
		refreshUsedPlaceholders();
	}
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (!refreshed.activeBossBars.contains(line)) return;
		Property progress = refreshed.properties.get("bossbar-progress-" + line.name);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			refreshed.sendCustomPacket(PacketPlayOutBoss.UPDATE_PCT(line.uuid, (float)line.parseProgress(progress.updateAndGet())/100));
		} else {
			DataWatcher w = new DataWatcher(null);
			float health = (float)3*line.parseProgress(progress.updateAndGet());
			if (health == 0) health = 1;
			w.helper().setHealth(health);
			refreshed.sendCustomBukkitPacket(new PacketPlayOutEntityMetadata(line.entityId, w));
		}
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.BOSSBAR_PROGRESS_REFRESH;
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(line.progress);
	}
}