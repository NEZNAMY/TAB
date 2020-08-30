package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Classes implementing this interface will receive join event
 */
public interface JoinEventListener extends Feature {

	public void onJoin(ITabPlayer connectedPlayer);
}
