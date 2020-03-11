package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

public class HeaderFooter implements SimpleFeature{

	public int refresh;

	@Override
	public void load() {
		refresh = Configs.config.getInt("header-footer-refresh-interval-milliseconds", 100);
		for (ITabPlayer p : Shared.getPlayers()) refreshHeaderFooter(p, true);
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing header/footer", "Header/Footer", new Runnable(){
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) refreshHeaderFooter(p, false);
			}
		});
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			if (p.disabledHeaderFooter || p.getVersion().getMinorVersion() < ProtocolVersion.v1_8.getMinorVersion()) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""));
		}
	}
	@Override
	public void onJoin(ITabPlayer p) {
		refreshHeaderFooter(p, true);
	}
	@Override
	public void onQuit(ITabPlayer p) {
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledHeaderFooter) {
			if (!p.isDisabledWorld(Configs.disabledHeaderFooter, from))
				p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("", ""));
		} else {
			refreshHeaderFooter(p, true);
		}
	}
	public void refreshHeaderFooter(ITabPlayer p, boolean force) {
		if (p.disabledHeaderFooter || p.getVersion().getMinorVersion() < ProtocolVersion.v1_8.getMinorVersion()) return;
		Property headerp = p.properties.get("header");
		Property footerp = p.properties.get("footer");
		boolean header = headerp.isUpdateNeeded();
		boolean footer = footerp.isUpdateNeeded();
		if (header || footer || force) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(headerp.get(), footerp.get()));
	}
}