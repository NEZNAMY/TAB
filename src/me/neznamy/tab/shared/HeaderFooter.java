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
		if (p.disabledHeaderFooter || p.getVersion().getNetworkId() < ProtocolVersion.v1_8.getNetworkId()) return;
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""));
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) clearHeaderFooter(p);
	}
	public static void playerJoin(ITabPlayer p) {
		if (enable) refreshHeaderFooter(p, true);
	}
	public static void refreshHeaderFooter(ITabPlayer p, boolean force) {
		if (p.disabledHeaderFooter || p.getVersion().getNetworkId() < ProtocolVersion.v1_8.getNetworkId()) return;
		boolean header = p.properties.get("header").isUpdateNeeded();
		boolean footer = p.properties.get("footer").isUpdateNeeded();
		if (header || footer || force) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(p.properties.get("header").get(), p.properties.get("footer").get()));
	}
}