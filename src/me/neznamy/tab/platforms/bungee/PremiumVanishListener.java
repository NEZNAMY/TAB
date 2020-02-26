package me.neznamy.tab.platforms.bungee;

import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.GlobalPlayerlist;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PremiumVanishListener implements Listener {

	@EventHandler
	public void a(BungeePlayerHideEvent e) {
		if (!Shared.features.containsKey("globalplayerlist")) return;
		((GlobalPlayerlist)Shared.features.get("globalplayerlist")).onQuit(Shared.getPlayer(e.getPlayer().getUniqueId()));
	}
	@EventHandler
	public void a(BungeePlayerShowEvent e) {
		if (!Shared.features.containsKey("globalplayerlist")) return;
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		PacketPlayOutPlayerInfo add = ((GlobalPlayerlist)Shared.features.get("globalplayerlist")).getAddPacket(p);
		for (ITabPlayer all : Shared.getPlayers()) {
			all.sendCustomPacket(add);
		}
	}
}