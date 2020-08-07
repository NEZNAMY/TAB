package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;

public interface PrefixSuffixProvider {

	public String getPrefix(ITabPlayer p);
	public String getSuffix(ITabPlayer p);
}