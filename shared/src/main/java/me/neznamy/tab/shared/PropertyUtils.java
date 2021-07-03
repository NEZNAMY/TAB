package me.neznamy.tab.shared;

public class PropertyUtils {

	public static final String HEADER = "header";
	public static final String FOOTER = "footer";
	
	public static final String TABPREFIX = "tabprefix";
	public static final String CUSTOMTABNAME = "customtabname";
	public static final String TABSUFFIX = "tabsuffix";
	
	public static final String TAGPREFIX = "tagprefix";
	public static final String CUSTOMTAGNAME = "customtagname";
	public static final String TAGSUFFIX = "tagsuffix";
	
	public static final String ABOVENAME = "abovename";
	public static final String NAMETAG = "nametag";
	public static final String BELOWNAME = "belowname";
	
	private PropertyUtils() {
	}
	
	public static String bossbarTitle(String name) {
		return "bossbar-title-" + name;
	}
	
	public static String bossbarProgress(String name) {
		return "bossbar-progress-" + name;
	}
	
	public static String bossbarColor(String name) {
		return "bossbar-color-" + name;
	}
	
	public static String bossbarStyle(String name) {
		return "bossbar-style-" + name;
	}
}
