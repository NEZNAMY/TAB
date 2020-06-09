package me.neznamy.tab.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.level1.PlayerCommand;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

public class TABAPI {

	public static List<UUID> hiddenNametag = new ArrayList<UUID>();
	
	
	/**
	 * Returns true if enabled, false if disabled
	 * @return Whether unlimited nametag mode is enabled or not
	 * @see enableUnlimitedNameTagModePermanently
	 * @since 2.4.12
	 */
	public static boolean isUnlimitedNameTagModeEnabled() {
		return Shared.features.containsKey("nametagx");
	}
	
	
	/**
	 * Enables unlimited nametag mode permanently in config
	 * @throws IllegalStateException if called from a proxy
	 * @see isUnlimitedNameTagModeEnabled
	 * @since 2.4.12
	 */
	public static void enableUnlimitedNameTagModePermanently() {
		if (Shared.mainClass instanceof me.neznamy.tab.platforms.bukkit.Main) {
			Configs.config.set("change-nametag-prefix-suffix", true);
			Configs.config.set("unlimited-nametag-prefix-suffix-mode.enabled", true);
			Shared.unload();
			Shared.load(false);
		} else throw new IllegalStateException("Unlimited nametag mode is only supported on bukkit");
	}
	
	
	/**
	 * Changes the requested property of a player temporarily (until next restart, reload or /tab reload)
	 * @param player UUID of player
	 * @param type Type of property
	 * @param value The value to be used
	 * @since 2.5.3
	 */
	public static void setValueTemporarily(UUID player, EnumProperty type, String value) {
		ITabPlayer p = Shared.getPlayer(player);
		if (p == null) return;
		Placeholders.checkForRegistration(value);
		p.properties.get(type.toString()).setTemporaryValue(value);
		if (Shared.features.containsKey("nametagx") && type.toString().contains("tag")) {
			p.setProperty("nametag", p.properties.get("tagprefix").getCurrentRawValue() + p.properties.get("customtagname").getCurrentRawValue() + p.properties.get("tagsuffix").getCurrentRawValue(), null);
		}
		p.forceUpdateDisplay();
	}
	
	
	/**
	 * Changes the requested property of a player permanently (saved into config too)
	 * @param player UUID of player
	 * @param type Type of property
	 * @param value The value to be used
	 * @since 2.5.3
	 */
	public static void setValuePermanently(UUID player, EnumProperty type, String value) {
		ITabPlayer p = Shared.getPlayer(player);
		if (p == null) return;
		Placeholders.checkForRegistration(value);
		p.properties.get(type.toString()).changeRawValue(value);
		PlayerCommand.savePlayer(null, p.getName(), type.toString(), value);
		if (Shared.features.containsKey("nametagx") && type.toString().contains("tag")) {
			p.setProperty("nametag", p.properties.get("tagprefix").getCurrentRawValue() + p.properties.get("customtagname").getCurrentRawValue() + p.properties.get("tagsuffix").getCurrentRawValue(), null);
		}
		p.forceUpdateDisplay();
	}
	
	
	/**
	 * Returns temporary value of player's property or null if not set
	 * @param player UUID of player
	 * @param type Type of property
	 * @return Temporary value of player's property or null if not set
	 * @see hasTemporaryValue
	 * @see setValueTemporarily
	 * @since 2.5.3
	 */
	public static String getTemporaryValue(UUID player, EnumProperty type) {
		return Shared.getPlayer(player).properties.get(type.toString()).getTemporaryValue();
	}
	
	
	/**
	 * Returns Whether player has temporary value or not
	 * @param player UUID of player
	 * @param type Type of property
	 * @return Whether player has temporary value or not
	 * @since 2.5.3
	 */
	public static boolean hasTemporaryValue(UUID player, EnumProperty type) {
		return getTemporaryValue(player, type) != null;
	}
	
	
	/**
	 * Removes temporary value from player if set
	 * @param player UUID of player
	 * @param type Type of property
	 * @since 2.5.3
	 */
	public static void removeTemporaryValue(UUID player, EnumProperty type) {
		setValueTemporarily(player, type, null);
	}
	
	
	/**
	 * Returns original value of property of player
	 * @param player UUID of player
	 * @param type Type of property
	 * @return Original value of property of player
	 * @since 2.5.3
	 */
	public static String getOriginalValue(UUID player, EnumProperty type) {
		return Shared.getPlayer(player).properties.get(type.toString()).getOriginalRawValue();
	}
	
	
	/**
	 * Sends requested header and footer to player
	 * @param player UUID of player
	 * @param header Header
	 * @param footer Footer
	 * @since 2.4.12
	 */
	public static void sendHeaderFooter(UUID player, String header, String footer) {
		Shared.getPlayer(player).sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(header, footer));
	}
	
	
	/**
	 * Sends empty header/footer to a player
	 * @param player UUID of player
	 * @since 2.4.12
	 */
	public static void clearHeaderFooter(UUID player) {
		sendHeaderFooter(player, "", "");
	}
	
	
	/**
	 * Makes player's nametag invisible until server restart/reload or /plugman reload tab
	 * @param player UUID of player
	 * @see showNametag 
	 * @see hasHiddenNametag
	 * @since 2.4.12
	 */
	public static void hideNametag(UUID player) {
		hiddenNametag.add(player);
		Shared.getPlayer(player).updateTeamData(true);
	}
	
	
	/**
	 * Makes player's nametag visible again
	 * @param player UUID of player
	 * @see hideNametag
	 * @see hasHiddenNametag
	 * @since 2.4.12
	 */
	public static void showNametag(UUID player) {
		hiddenNametag.remove(player);
		Shared.getPlayer(player).updateTeamData(true);
	}
	
	
	/**
	 * Return whether player has hidden nametag or not
	 * @param player UUID of player
	 * @return Whether player has hidden nametag or not
	 * @since 2.4.12
	 * @see hideNametag
	 * @see showNametag
	 */
	public static boolean hasHiddenNametag(UUID player) {
		return hiddenNametag.contains(player);
	}
	
	
	/**
	 * Registers a player placeholder (placeholder with player-specific output)
	 * @param placeholder - Placeholder handler
	 * @since 2.6.5
	 * @see registerServerPlaceholder
	 * @see registerServerConstant
	 */
	public static void registerPlayerPlaceholder(PlayerPlaceholder placeholder) {
		Placeholders.registerPlaceholder(placeholder);
	}
	
	
	/**
	 * Registers a server placeholder (placeholder with same output for all players)
	 * @param placeholder - Placeholder handler
	 * @since 2.6.5
	 * @see registerPlayerPlaceholder
	 * @see registerServerConstant
	 */
	public static void registerServerPlaceholder(ServerPlaceholder placeholder) {
		Placeholders.registerPlaceholder(placeholder);
	}
	
	
	/**
	 * Registers a server constant (constant with same output for all players)
	 * @param constant - Constant handler
	 * @since 2.7.0
	 * @see registerPlayerPlaceholder
	 * @see registerServerPlaceholder
	 */
	public static void registerServerConstant(ServerConstant constant) {
		Placeholders.registerPlaceholder(constant);
	}
	
	public static Scoreboard createScoreboard(String title, List<String> lines) {
		if (!Premium.is()) throw new IllegalArgumentException("Not supported in free version");
		for (String line : lines) {
			Placeholders.checkForRegistration(line);
		}
		ScoreboardManager sbm = (ScoreboardManager) Shared.features.get("scoreboard");
		Scoreboard sb = new me.neznamy.tab.premium.Scoreboard("API", title, lines);
		sbm.APIscoreboards.add((me.neznamy.tab.premium.Scoreboard) sb);
		return sb;
	}
	
	@Deprecated
	public static void setCustomTabNameTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.CUSTOMTABNAME, value);
	}
	@Deprecated
	public static void setCustomTagNameTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.CUSTOMTAGNAME, value);
	}
	@Deprecated
	public static void setTabPrefixTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.TABPREFIX, value);
	}
	@Deprecated
	public static void setTabSuffixTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.TABSUFFIX, value);
	}
	@Deprecated
	public static void setTagPrefixTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.TAGPREFIX, value);
	}
	@Deprecated
	public static void setTagSuffixTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.TAGSUFFIX, value);
	}
	@Deprecated
	public static void setAboveNameTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.ABOVENAME, value);
	}
	@Deprecated
	public static void setBelowNameTemporarily(UUID uniqueId, String value) {
		setValueTemporarily(uniqueId, EnumProperty.BELOWNAME, value);
	}
	
	@Deprecated
	public static void setCustomTabNamePermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.CUSTOMTABNAME, value);
	}
	@Deprecated
	public static void setCustomTagNamePermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.CUSTOMTAGNAME, value);
	}
	@Deprecated
	public static void setTabPrefixPermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.TABPREFIX, value);
	}
	@Deprecated
	public static void setTabSuffixPermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.TABSUFFIX, value);
	}
	@Deprecated
	public static void setTagPrefixPermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.TAGPREFIX, value);
	}
	@Deprecated
	public static void setTagSuffixPermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.TAGSUFFIX, value);
	}
	@Deprecated
	public static void setAboveNamePermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.ABOVENAME, value);
	}
	@Deprecated
	public static void setBelowNamePermanently(UUID uniqueId, String value) {
		setValuePermanently(uniqueId, EnumProperty.BELOWNAME, value);
	}
	
	@Deprecated
	public static String getTemporaryCustomTabName(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.CUSTOMTABNAME);
	}
	@Deprecated
	public static String getTemporaryCustomTagName(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.CUSTOMTAGNAME);
	}
	@Deprecated
	public static String getTemporaryTabPrefix(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.TABPREFIX);
	}
	@Deprecated
	public static String getTemporaryTabSuffix(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.TABSUFFIX);
	}
	@Deprecated
	public static String getTemporaryTagPrefix(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.TAGPREFIX);
	}
	@Deprecated
	public static String getTemporaryTagSuffix(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.TAGSUFFIX);
	}
	@Deprecated
	public static String getTemporaryAboveName(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.ABOVENAME);
	}
	@Deprecated
	public static String getTemporaryBelowName(UUID uniqueId) {
		return getTemporaryValue(uniqueId, EnumProperty.BELOWNAME);
	}
	
	@Deprecated
	public static boolean hasTemporaryCustomTabName(UUID uniqueId) {
		return getTemporaryCustomTabName(uniqueId) != null;
	}
	@Deprecated
	public static boolean hasTemporaryCustomTagName(UUID uniqueId) {
		return getTemporaryCustomTagName(uniqueId) != null;
	}
	@Deprecated
	public static boolean hasTemporaryTabPrefix(UUID uniqueId) {
		return getTemporaryTabPrefix(uniqueId) != null;
	}
	@Deprecated
	public static boolean hasTemporaryTabSuffix(UUID uniqueId) {
		return getTemporaryTabSuffix(uniqueId) != null;
	}
	@Deprecated
	public static boolean hasTemporaryTagPrefix(UUID uniqueId) {
		return getTemporaryTagPrefix(uniqueId) != null;
	}
	@Deprecated
	public static boolean hasTemporaryTagSuffix(UUID uniqueId) {
		return getTemporaryTagSuffix(uniqueId) != null;
	}
	@Deprecated
	public static boolean hasTemporaryAboveName(UUID uniqueId) {
		return getTemporaryAboveName(uniqueId) != null;
	}
	@Deprecated
	public static boolean hasTemporaryBelowName(UUID uniqueId) {
		return getTemporaryBelowName(uniqueId) != null;
	}
	
	@Deprecated
	public static void removeTemporaryCustomTabName(UUID uniqueId) {
		setCustomTabNameTemporarily(uniqueId, null);
	}
	@Deprecated
	public static void removeTemporaryCustomTagName(UUID uniqueId) {
		setCustomTagNameTemporarily(uniqueId, null);
	}
	@Deprecated
	public static void removeTemporaryTabPrefix(UUID uniqueId) {
		setTabPrefixTemporarily(uniqueId, null);
	}
	@Deprecated
	public static void removeTemporaryTabSuffix(UUID uniqueId) {
		setTabSuffixTemporarily(uniqueId, null);
	}
	@Deprecated
	public static void removeTemporaryTagPrefix(UUID uniqueId) {
		setTagPrefixTemporarily(uniqueId, null);
	}
	@Deprecated
	public static void removeTemporaryTagSuffix(UUID uniqueId) {
		setTagSuffixTemporarily(uniqueId, null);
	}
	@Deprecated
	public static void removeTemporaryAboveName(UUID uniqueId) {
		setAboveNameTemporarily(uniqueId, null);
	}
	@Deprecated
	public static void removeTemporaryBelowName(UUID uniqueId) {
		setBelowNameTemporarily(uniqueId, null);
	}
	
	@Deprecated
	public static String getOriginalCustomTabName(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.CUSTOMTABNAME);
	}
	@Deprecated
	public static String getOriginalCustomTagName(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.CUSTOMTAGNAME);
	}
	@Deprecated
	public static String getOriginalTabPrefix(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.TABPREFIX);
	}
	@Deprecated
	public static String getOriginalTagPrefix(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.TAGPREFIX);
	}
	@Deprecated
	public static String getOriginalTabSuffix(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.TABSUFFIX);
	}
	@Deprecated
	public static String getOriginalTagSuffix(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.TAGSUFFIX);
	}
	@Deprecated
	public static String getOriginalAboveName(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.ABOVENAME);
	}
	@Deprecated
	public static String getOriginalBelowName(UUID uniqueId) {
		return getOriginalValue(uniqueId, EnumProperty.BELOWNAME);
	}
	
}