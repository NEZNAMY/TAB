package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;

/**
 * Classes implementing this interface will receive player commands
 */
public interface CommandListener extends Feature {

	public boolean onCommand(TabPlayer sender, String message);
}
