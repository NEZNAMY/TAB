package me.neznamy.tab.platforms.proxy.bungee;

import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.GlobalPlayerlist;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * An addon for global playerlist to make PremiumVanish work properly
 */
public class PremiumVanishListener implements Listener {
	
	/**
	 * Listener to player hide event to hide from global playerlist
	 * @param e - hide event
	 */
	@EventHandler
	public void a(BungeePlayerHideEvent e) {
		GlobalPlayerlist list = (GlobalPlayerlist) Shared.featureManager.getFeature("globalplayerlist");
		if (list == null) return;
		TabPlayer vanished = Shared.getPlayer(e.getPlayer().getUniqueId());
		Object remove = list.getRemovePacket(vanished).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer all : Shared.getPlayers()) {
			if (all == vanished || all.hasPermission(list.PREMIUMVANISH_SEE_VANISHED_PERMISSION)) continue;
			all.sendPacket(remove);
		}
	}


	/**
	 * Listener to player show event to show in global playerlist
	 * @param e - show event
	 */
	@EventHandler
	public void a(BungeePlayerShowEvent e) {
		GlobalPlayerlist list = (GlobalPlayerlist) Shared.featureManager.getFeature("globalplayerlist");
		if (list == null) return;
		TabPlayer unvanished = Shared.getPlayer(e.getPlayer().getUniqueId());
		Object add = list.getAddPacket(unvanished).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer viewer : Shared.getPlayers()) {
			if (list.shouldSee(viewer, unvanished)) viewer.sendPacket(add);
		}
	}
}