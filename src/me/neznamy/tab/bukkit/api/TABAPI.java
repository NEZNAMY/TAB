package me.neznamy.tab.bukkit.api;

import org.bukkit.entity.Player;

@Deprecated
public class TABAPI {

	public static boolean isUnlimitedNameTagModeEnabled() {
		return me.neznamy.tab.api.TABAPI.isUnlimitedNameTagModeEnabled();
	}
	public static void enableUnlimitedNameTagModePermanently() {
		me.neznamy.tab.api.TABAPI.enableUnlimitedNameTagModePermanently();
	}
	public static void setCustomTabNameTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setCustomTabNameTemporarily(p.getUniqueId(), value);
	}
	public static void setCustomTagNameTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setCustomTagNameTemporarily(p.getUniqueId(), value);
	}
	public static void setTabPrefixTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTabPrefixTemporarily(p.getUniqueId(), value);
	}
	public static void setTabSuffixTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTabSuffixTemporarily(p.getUniqueId(), value);
	}
	public static void setTagPrefixTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTagPrefixTemporarily(p.getUniqueId(), value);
	}
	public static void setTagSuffixTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTagSuffixTemporarily(p.getUniqueId(), value);
	}
	
	public static void setCustomTabNamePermanently(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setCustomTabNamePermanently(p.getUniqueId(), value);
	}
	public static void setCustomTagNamePermanently(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setCustomTagNamePermanently(p.getUniqueId(), value);
	}
	public static void setTabPrefixPermanently(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTabPrefixPermanently(p.getUniqueId(), value);
	}
	public static void setTabSuffixPermanently(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTabSuffixPermanently(p.getUniqueId(), value);
	}
	public static void setTagPrefixPermanently(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTagPrefixPermanently(p.getUniqueId(), value);
	}
	public static void setTagSuffixPermanently(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setTagSuffixPermanently(p.getUniqueId(), value);
	}
	
	public static String getTemporaryCustomTabName(Player p) {
		return me.neznamy.tab.api.TABAPI.getTemporaryCustomTabName(p.getUniqueId());
	}
	public static String getTemporaryCustomTagName(Player p) {
		return me.neznamy.tab.api.TABAPI.getTemporaryCustomTagName(p.getUniqueId());
	}
	public static String getTemporaryTabPrefix(Player p) {
		return me.neznamy.tab.api.TABAPI.getTemporaryTabPrefix(p.getUniqueId());
	}
	public static String getTemporaryTabSuffix(Player p) {
		return me.neznamy.tab.api.TABAPI.getTemporaryTabSuffix(p.getUniqueId());
	}
	public static String getTemporaryTagPrefix(Player p) {
		return me.neznamy.tab.api.TABAPI.getTemporaryTagPrefix(p.getUniqueId());
	}
	public static String getTemporaryTagSuffix(Player p) {
		return me.neznamy.tab.api.TABAPI.getTemporaryTagSuffix(p.getUniqueId());
	}

	public static boolean hasTemporaryCustomTabName(Player p) {
		return getTemporaryCustomTabName(p) != null;
	}
	public static boolean hasTemporaryCustomTagName(Player p) {
		return getTemporaryCustomTagName(p) != null;
	}
	public static boolean hasTemporaryTabPrefix(Player p) {
		return getTemporaryTabPrefix(p) != null;
	}
	public static boolean hasTemporaryTabSuffix(Player p) {
		return getTemporaryTabSuffix(p) != null;
	}
	public static boolean hasTemporaryTagPrefix(Player p) {
		return getTemporaryTagPrefix(p) != null;
	}
	public static boolean hasTemporaryTagSuffix(Player p) {
		return getTemporaryTagSuffix(p) != null;
	}
	
	public static void removeTemporaryCustomTabName(Player p) {
		me.neznamy.tab.api.TABAPI.removeTemporaryCustomTabName(p.getUniqueId());
	}
	public static void removeTemporaryCustomTagName(Player p) {
		me.neznamy.tab.api.TABAPI.removeTemporaryCustomTagName(p.getUniqueId());
	}
	public static void removeTemporaryTabPrefix(Player p) {
		me.neznamy.tab.api.TABAPI.removeTemporaryTabPrefix(p.getUniqueId());
	}
	public static void removeTemporaryTabSuffix(Player p) {
		me.neznamy.tab.api.TABAPI.removeTemporaryTabSuffix(p.getUniqueId());
	}
	public static void removeTemporaryTagPrefix(Player p) {
		me.neznamy.tab.api.TABAPI.removeTemporaryTagPrefix(p.getUniqueId());
	}
	public static void removeTemporaryTagSuffix(Player p) {
		me.neznamy.tab.api.TABAPI.removeTemporaryTagSuffix(p.getUniqueId());
	}
	
	public static void setAboveNameTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setAboveNameTemporarily(p.getUniqueId(), value);
	}
	public static void setBelowNameTemporarily(Player p, String value) {
		me.neznamy.tab.api.TABAPI.setBelowNameTemporarily(p.getUniqueId(), value);
	}
	public static void sendHeaderFooter(Player p, String header, String footer) {
		me.neznamy.tab.api.TABAPI.sendHeaderFooter(p.getUniqueId(), header, footer);
	}
	public static void refreshHeaderFooter(Player p) {
		me.neznamy.tab.api.TABAPI.refreshHeaderFooter(p.getUniqueId());
	}
	public static void clearHeaderFooter(Player p) {
		me.neznamy.tab.api.TABAPI.clearHeaderFooter(p.getUniqueId());
	}
	public static String getOriginalTabPrefix(Player p) {
		return me.neznamy.tab.api.TABAPI.getOriginalTabPrefix(p.getUniqueId());
	}
	public static String getOriginalTagPrefix(Player p) {
		return me.neznamy.tab.api.TABAPI.getOriginalTagPrefix(p.getUniqueId());
	}
	public static String getOriginalTabSuffix(Player p) {
		return me.neznamy.tab.api.TABAPI.getOriginalTabSuffix(p.getUniqueId());
	}
	public static String getOriginalTagSuffix(Player p) {
		return me.neznamy.tab.api.TABAPI.getOriginalTagSuffix(p.getUniqueId());
	}
	public static String getOriginalCustomTagName(Player p) {
		return me.neznamy.tab.api.TABAPI.getOriginalCustomTagName(p.getUniqueId());
	}
	public static String getOriginalCustomTabName(Player p) {
		return me.neznamy.tab.api.TABAPI.getOriginalCustomTabName(p.getUniqueId());
	}
	public static void hideNametag(Player p) {
		me.neznamy.tab.api.TABAPI.hideNametag(p.getUniqueId());
	}
	public static void showNametag(Player p) {
		me.neznamy.tab.api.TABAPI.showNametag(p.getUniqueId());
	}
	public static boolean hasHiddenNametag(Player p) {
		return me.neznamy.tab.api.TABAPI.hasHiddenNametag(p.getUniqueId());
	}
}