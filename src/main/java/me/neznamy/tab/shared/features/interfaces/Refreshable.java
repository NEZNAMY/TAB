package me.neznamy.tab.shared.features.interfaces;

import java.util.Set;

import me.neznamy.tab.api.TabPlayer;

/**
 * Classes implementing this interface will receive refresh call when a placeholder changes it's value
 */
public interface Refreshable extends Feature {

	public void refresh(TabPlayer refreshed, boolean force);
	public Set<String> getUsedPlaceholders();
	public void refreshUsedPlaceholders();
}
