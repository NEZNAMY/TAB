package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface to be implemented by classes which offer prefix/suffix retrieving from other plugins
 */
public interface PrefixSuffixProvider {

	public String getPrefix(TabPlayer p);
	public String getSuffix(TabPlayer p);
}