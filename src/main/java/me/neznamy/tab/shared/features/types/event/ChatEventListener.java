package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive player chat event
 */
public interface ChatEventListener extends Feature {

	/**
	 * Processes player chat event
	 * @param sender - message sender
	 * @param message - message sent
	 * @param cancelled - true if event is cancelled already, false if not
	 * @return true if event should be cancelled, false if not
	 */
	public boolean onChat(TabPlayer sender, String message, boolean cancelled);
}