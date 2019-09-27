package me.neznamy.tab.api;

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
