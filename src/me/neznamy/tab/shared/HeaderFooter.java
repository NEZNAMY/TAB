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
				for (ITabPlayer p : Shared.getPlayers()) refreshHeaderFooter(p);
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
		String[] hf = Placeholders.replaceMultiple(p, p.getRawHeader(), p.getRawFooter());
		String header = hf[0];
		String footer = hf[1];
		if (header.equals(p.getLastHeader()) && footer.equals(p.getLastFooter())) return;
		new PacketPlayOutPlayerListHeaderFooter(header, footer).send(p);
		p.setLastHeader(header);
		p.setLastFooter(footer);
	}
}