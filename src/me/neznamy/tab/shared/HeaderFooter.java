package me.neznamy.tab.shared;

import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

public class HeaderFooter {

	public static int refresh;
	public static boolean enable;

	public static void load() {
		if (!enable) return;
		for (ITabPlayer p : Shared.getPlayers()) refreshHeaderFooter(p, true);
		Shared.scheduleRepeatingTask(refresh, "refreshing header/footer", Feature.HEADERFOOTER, new Runnable(){
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) refreshHeaderFooter(p, false);
			}
		});
	}
	public static void clearHeaderFooter(ITabPlayer p) {
		if (p.disabledHeaderFooter || p.getVersion().getMinorVersion() < 8) return;
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""));
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) clearHeaderFooter(p);
	}
	public static void playerJoin(ITabPlayer p) {
		if (enable) refreshHeaderFooter(p, true);
	}
	public static void refreshHeaderFooter(ITabPlayer p, boolean force) {
		if (p.disabledHeaderFooter || p.getVersion().getMinorVersion() < 8) return;
		Property headerp = p.properties.get("header");
		Property footerp = p.properties.get("footer");
		boolean header = headerp.isUpdateNeeded();
		boolean footer = footerp.isUpdateNeeded();
		if (header || footer || force) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(headerp.get(), footerp.get()));
	}
}