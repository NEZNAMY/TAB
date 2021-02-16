package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive player commands
 */
public interface CommandListener extends Feature {

	public boolean onCommand(TabPlayer sender, String message);
}
