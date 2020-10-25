package me.neznamy.tab.platforms.proxy.bungee;

import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import me.neznamy.tab.api.TabPlayer;
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

	//permission to see vanished players
	private final String PREMIUMVANISH_SEE_VANISHED_PERMISSION = "pv.see";
	
	/**
	 * Listener to player hide event to hide from global playerlist
	 * @param e - hide event
	 */
	@EventHandler
	public void a(BungeePlayerHideEvent e) {
		if (!Shared.featureManager.isFeatureEnabled("globalplayerlist")) return;
		TabPlayer vanished = Shared.getPlayer(e.getPlayer().getUniqueId());
		Object remove = getRemovePacket(vanished).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer all : Shared.getPlayers()) {
			if (all == vanished || all.hasPermission(PREMIUMVANISH_SEE_VANISHED_PERMISSION)) continue;
			all.sendPacket(remove);
		}
	}
	
	/**
	 * Returns remove packet for specified player
	 * @param p - player to remove
	 * @return removing packet
	 */
	private PacketPlayOutPlayerInfo getRemovePacket(TabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(p.getName(), p.getUniqueId(), null, 0, null, null));
	}
	
	/**
	 * Returns add packet for specified player
	 * @param p - player to add
	 * @return adding packet
	 */
	private PacketPlayOutPlayerInfo getAddPacket(TabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getUniqueId(), p.getSkin(), (int)p.getPing(), EnumGamemode.CREATIVE, null));
	}
	
	/**
	 * Listener to player show event to show in global playerlist
	 * @param e - show event
	 */
	@EventHandler
	public void a(BungeePlayerShowEvent e) {
		if (!Shared.featureManager.isFeatureEnabled("globalplayerlist")) return;
		TabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		Object add = getAddPacket(p).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer all : Shared.getPlayers()) {
			all.sendPacket(add);
		}
	}
}