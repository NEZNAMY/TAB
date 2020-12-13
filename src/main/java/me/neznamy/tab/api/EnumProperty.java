package me.neznamy.tab.api;

/**
 * An enum class representing basic properties applicable to a player or group
 */
public enum EnumProperty {

	TABPREFIX,
	CUSTOMTABNAME,
	TABSUFFIX,
	TAGPREFIX,
	CUSTOMTAGNAME,
	TAGSUFFIX,
	BELOWNAME,
	ABOVENAME;
	
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
