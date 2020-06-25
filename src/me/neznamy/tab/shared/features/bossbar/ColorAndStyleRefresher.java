package me.neznamy.tab.shared.features.bossbar;

import java.util.Set;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class ColorAndStyleRefresher implements Refreshable {

	private BossBarLine line;
	private Set<String> usedPlaceholders;
	
	public ColorAndStyleRefresher(BossBarLine line) {
		this.line = line;
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(line.color);
		usedPlaceholders.addAll(Placeholders.getUsedPlaceholderIdentifiersRecursive(line.style));
	}
	@Override
	public void refresh(ITabPlayer refreshed) {
		if (!refreshed.activeBossBars.contains(line)) return;
		Property color = refreshed.properties.get("bossbar-color-" + line.name);
		Property style = refreshed.properties.get("bossbar-style-" + line.name);
		refreshed.sendCustomPacket(PacketPlayOutBoss.UPDATE_STYLE(line.uuid, line.parseColor(color.updateAndGet()), line.parseStyle(style.updateAndGet())));
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.BOSSBAR_COLOR_STYLE_REFRESH;
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
}