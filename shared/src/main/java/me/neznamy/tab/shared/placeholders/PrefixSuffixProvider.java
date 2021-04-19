package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface to be implemented by classes which offer prefix/suffix retrieving from other plugins
 */
public interface PrefixSuffixProvider {

	/**
	 * Returns prefix for given player
	 * @param p - player
	 * @return prefix from permission plugin
	 */
	public String getPrefix(TabPlayer p);
	
	/**
	 * Returns suffix for given player
	 * @param p - player
	 * @return suffix from permission plugin
	 */
	public String getSuffix(TabPlayer p);
}