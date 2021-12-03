package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;

public interface RedisSupport {

	void updateTabFormat(TabPlayer p, String format);
	
	void updateNameTag(TabPlayer p, String tagPrefix, String tagSuffix);
	
	void updateBelowName(TabPlayer p, String value);
	
	void updateYellowNumber(TabPlayer p, String value);
	
	void updateTeamName(TabPlayer p, String to);
}