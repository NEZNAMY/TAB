package me.neznamy.tab.platforms.bungee;

import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
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
		GlobalPlayerlist list = (GlobalPlayerlist) TAB.getInstance().getFeatureManager().getFeature("globalplayerlist");
		if (list == null) return;
		TabPlayer vanished = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		for (TabPlayer all : TAB.getInstance().getPlayers()) {
			if (all == vanished || all.hasPermission(list.PREMIUMVANISH_SEE_VANISHED_PERMISSION)) continue;
			all.sendCustomPacket(list.getRemovePacket(vanished));
		}
	}


	/**
	 * Listener to player show event to show in global playerlist
	 * @param e - show event
	 */
	@EventHandler
	public void a(BungeePlayerShowEvent e) {
		GlobalPlayerlist list = (GlobalPlayerlist) TAB.getInstance().getFeatureManager().getFeature("globalplayerlist");
		if (list == null) return;
		TabPlayer unvanished = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
		for (TabPlayer viewer : TAB.getInstance().getPlayers()) {
			if (list.shouldSee(viewer, unvanished)) viewer.sendCustomPacket(list.getAddPacket(unvanished));
		}
	}
}