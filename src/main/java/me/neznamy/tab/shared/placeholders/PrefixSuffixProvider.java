package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * An interface to be implemented by classes which offer prefix/suffix retrieving from other plugins
 */
public interface PrefixSuffixProvider {

	public String getPrefix(ITabPlayer p);
	public String getSuffix(ITabPlayer p);
}