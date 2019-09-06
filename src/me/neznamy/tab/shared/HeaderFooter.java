package me.neznamy.tab.shared;

import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

public class HeaderFooter {
	
	public static int refresh;
	public static boolean enable;
	
	public static void load() {
		if (!enable) return;
		Shared.scheduleRepeatingTask(refresh, "refreshing header/footer", Feature.HEADERFOOTER, new Runnable(){
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) if (p.fullyLoaded) refreshHeaderFooter(p);
			}
		});
	}
	public static void clearHeaderFooter(ITabPlayer p) {
		if (!p.disabledHeaderFooter) new PacketPlayOutPlayerListHeaderFooter("","").send(p);
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) clearHeaderFooter(p);
	}
	public static void playerJoin(ITabPlayer p) {
		if (enable) refreshHeaderFooter(p);
	}
	public static void refreshHeaderFooter(ITabPlayer p) {
		if (p.disabledHeaderFooter) return;
		boolean header = p.properties.get("header").isUpdateNeeded();
		boolean footer = p.properties.get("footer").isUpdateNeeded();
		if (header || footer) {
			new PacketPlayOutPlayerListHeaderFooter(p.properties.get("header").get(), p.properties.get("footer").get()).send(p);
		}
	}
}