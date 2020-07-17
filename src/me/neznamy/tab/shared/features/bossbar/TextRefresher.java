package me.neznamy.tab.shared.features.bossbar;

import java.util.Set;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.Placeholders;

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
		Property text = refreshed.properties.get("bossbar-text-" + line.name);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			refreshed.sendCustomPacket(PacketPlayOutBoss.UPDATE_NAME(line.uuid, text.updateAndGet()));
		} else {
			DataWatcher w = new DataWatcher(null);
			DataWatcher.Helper.setCustomName(w, text.updateAndGet(), refreshed.getVersion());
			refreshed.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(line.entityId, w.toNMS(), true));
		}
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.BOSSBAR_TEXT_REFRESH;
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(line.text);
	}
}