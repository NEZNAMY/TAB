package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class HeaderFooter implements Loadable, JoinEventListener, WorldChangeListener, Refreshable{

	private Set<String> usedPlaceholders;
	
	public HeaderFooter() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("header", "footer");
	}
	@Override
	public void load() {
		for (ITabPlayer p : Shared.getPlayers()) refresh(p, true);
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			if (p.disabledHeaderFooter || p.getVersion().getMinorVersion() < 8) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""));
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		refresh(connectedPlayer, true);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.getVersion().getMinorVersion() < 8) return;
		if (p.disabledHeaderFooter) {
			if (!p.isDisabledWorld(Configs.disabledHeaderFooter, from)) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("", ""));
		} else {
			refresh(p, true);
		}
	}
	@Override
	public void refresh(ITabPlayer p, boolean force) {
		if (force) {
			updateRawValue(p, "header");
			updateRawValue(p, "footer");
		}
		if (p.disabledHeaderFooter || p.getVersion().getMinorVersion() < 8) return;
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(p.properties.get("header").updateAndGet(), p.properties.get("footer").updateAndGet()));
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.HEADER_FOOTER;
	}
	private void updateRawValue(ITabPlayer p, String name) {
		String worldGroup = p.getWorldGroupOf(p.getWorldName());
		StringBuilder rawValue = new StringBuilder();
		List<String> lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + ".Users." + p.getName() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + ".Users." + p.getUniqueId().toString() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Users." + p.getName() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Users." + p.getUniqueId().toString() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + ".Groups." + p.getGroup() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.separatorType + "-settings." + worldGroup + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Groups." + p.getGroup() + "." + name);
		if (lines == null) lines = Configs.config.getStringList(name);
		if (lines == null) lines = new ArrayList<String>();
		int i = 0;
		for (String line : lines) {
			if (++i > 1) rawValue.append("\n" + Placeholders.colorChar + "r");
			rawValue.append(line);
		}
		p.setProperty(name, rawValue.toString(), null);
	}
}