package me.neznamy.tab.platforms.bungee;

import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * An addon for global playerlist to make PremiumVanish work properly
 */
public class PremiumVanishListener implements Listener {

	private final String PREMIUMVANISH_SEE_VANISHED_PERMISSION = "pv.see";
	
	@EventHandler
	public void a(BungeePlayerHideEvent e) {
		if (!Shared.features.containsKey("globalplayerlist")) return;
		ITabPlayer vanished = Shared.getPlayer(e.getPlayer().getUniqueId());
		Object remove = getRemovePacket(vanished).build(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == vanished || all.hasPermission(PREMIUMVANISH_SEE_VANISHED_PERMISSION)) continue;
			all.sendPacket(remove);
		}
	}
	
	public PacketPlayOutPlayerInfo getRemovePacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(p.getName(), p.getUniqueId(), null, 0, null, null));
	}
	
	public PacketPlayOutPlayerInfo getAddPacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getUniqueId(), p.getSkin(), (int)p.getPing(), EnumGamemode.CREATIVE, null));
	}
	
	@EventHandler
	public void a(BungeePlayerShowEvent e) {
		if (!Shared.features.containsKey("globalplayerlist")) return;
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		Object add = getAddPacket(p).build(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer all : Shared.getPlayers()) {
			all.sendPacket(add);
		}
	}
}