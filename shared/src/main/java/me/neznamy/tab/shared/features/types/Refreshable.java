package me.neznamy.tab.shared.features.types;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;

/**
 * Classes implementing this interface will receive refresh call when a placeholder changes it's value
 */
public interface Refreshable extends Feature {

	/**
	 * Performs refresh of specified player
	 * @param refreshed - player to refresh
	 * @param force - if refresh should be forced despite refresh seemingly not needed
	 */
	public void refresh(TabPlayer refreshed, boolean force);
	
	/**
	 * Returns list of all used placeholders in this feature
	 * @return list of all used placeholders in this feature
	 */
	public List<String> getUsedPlaceholders();
	
	/**
	 * Refreshes list of used placeholders in this feature
	 */
	public void refreshUsedPlaceholders();
}