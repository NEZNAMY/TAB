package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;

public interface RedisSupport {

	public void updateTabFormat(TabPlayer p, String format);
	
	public void updateNameTag(TabPlayer p, String tagprefix, String tagsuffix);
	
	public void updateBelowname(TabPlayer p, String value);
	
	public void updateYellowNumber(TabPlayer p, String value);
}