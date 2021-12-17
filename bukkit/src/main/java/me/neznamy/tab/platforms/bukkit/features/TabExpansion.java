package me.neznamy.tab.platforms.bukkit.features;

import me.neznamy.tab.shared.TabConstants;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

/**
 * TAB's expansion for PlaceholderAPI
 */
public class TabExpansion extends PlaceholderExpansion {

	//plugin to take plugin.yml data from
	private final JavaPlugin plugin;
	
	/**
	 * Constructs new instance of the class and registers it
	 * @param plugin - plugin to take plugin.yml data from
	 */
	public TabExpansion(JavaPlugin plugin) {
		this.plugin = plugin;
		register();
	}
	
	@Override
	public boolean persist(){
		return true;
	}

	@Override
	public boolean canRegister(){
		return true;
	}

	@Override
	public @NotNull String getAuthor(){
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public @NotNull String getIdentifier(){
		return "tab";
	}

	@Override
	public @NotNull String getVersion(){
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String identifier){
		if (player == null) return "";
		TabPlayer p = TAB.getInstance().getPlayer(player.getUniqueId());
		if ("scoreboard_visible".equals(identifier)) {
			return translate(hasScoreboardVisible(p));
		}
		if ("scoreboard_name".equals(identifier)) {
			return getActiveScoreboard(p);
		}
		if ("bossbar_visible".equals(identifier)) {
			return translate(hasBossBarVisible(p));
		}
		if ("ntpreview".equals(identifier)) {
			return translate(p.isPreviewingNametag());
		}
		if (identifier.startsWith("replace_")) {
			return findReplacement("%" + identifier.substring(8) + "%", player);
		}
		if (identifier.startsWith("placeholder_")) {
			return TAB.getInstance().getPlaceholderManager().getPlaceholder("%" + identifier.substring(12) + "%").getLastValue(p);
		}
		return getProperty(identifier, p);
	}
	
	private boolean hasBossBarVisible(TabPlayer p) {
		BossBarManager boss = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
		if (boss == null) return false;
		return boss.hasBossBarVisible(p);
	}
	
	private boolean hasScoreboardVisible(TabPlayer p) {
		ScoreboardManager scoreboard = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
		if (scoreboard == null) return false;
		return scoreboard.hasScoreboardVisible(p);
	}
	
	private String getActiveScoreboard(TabPlayer p) {
		ScoreboardManager sb = TabAPI.getInstance().getScoreboardManager();
		if (sb == null) return "";
		Scoreboard active = sb.getActiveScoreboard(p);
		if (active == null) return "";
		return active.getName();
	}
	
	private String translate(boolean b) {
		return b ? "Enabled" : "Disabled";
	}
	
	/**
	 * Find replacement for specified placeholder
	 * @param placeholder - placeholder to find replacement for
	 * @param player - player to set placeholder for
	 * @return replacement
	 */
	private String findReplacement(String placeholder, Player player) {
		String output = PlaceholderAPI.setPlaceholders(player, placeholder);
		return TAB.getInstance().getPlaceholderManager().findReplacement(placeholder, output).replace("%value%", output);
	}
	
	/**
	 * Returns value of defined property if found
	 * @param name - property name
	 * @param player - player to get property of
	 * @return value of specified property
	 */
	private String getProperty(String name, TabPlayer player) {
		String propName = name.replace("_raw", "");
		Property prop = player.getProperty(propName);
		if (prop != null) {
		    if (name.endsWith("_raw")) {
		    	return prop.getCurrentRawValue();
		    }
		    return prop.get();
		}
		return null;
	}
}
