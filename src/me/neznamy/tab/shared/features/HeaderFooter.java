package me.neznamy.tab.shared.features;

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
			refresh(p, false);
		}
	}
	@Override
	public void refresh(ITabPlayer p, boolean force) {
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
}