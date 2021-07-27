package me.neznamy.tab.platforms.bukkit.features;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.platforms.bukkit.BukkitPlatform;
import me.neznamy.tab.shared.PropertyImpl;
import me.neznamy.tab.shared.TAB;

/**
 * TAB's expansion for PlaceholderAPI
 */
public class TabExpansion extends PlaceholderExpansion {

	//plugin to take plugin.yml data from
	private JavaPlugin plugin;
	
	
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
	public String getAuthor(){
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier(){
		return "tab";
	}

	@Override
	public String getVersion(){
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier){
		if (player == null) return "";
		TabPlayer p = TAB.getInstance().getPlayer(player.getUniqueId());
		if (identifier.equals("scoreboard_visible")) {
			return translate(hasScoreboardVisible(p));
		}
		if (identifier.equals("bossbar_visible")) {
			return translate(hasBossBarVisible(p));
		}
		if (identifier.equals("ntpreview")) {
			return translate(p.isPreviewingNametag());
		}
		if (identifier.startsWith("replace_")) {
			return findReplacement("%" + identifier.substring(8) + "%", player);
		}
		if (identifier.startsWith("placeholder_")) {
			//using Property function for fast & easy handling of nested placeholders and different placeholder types
			return new PropertyImpl(null, p, "%" + identifier.substring(12) + "%").get();
		}
		return getProperty(identifier, p);
	}
	
	private boolean hasBossBarVisible(TabPlayer p) {
		BossBarManager boss = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (boss == null) return false;
		return boss.hasBossBarVisible(p);
	}
	
	private boolean hasScoreboardVisible(TabPlayer p) {
		ScoreboardManager scoreboard = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (scoreboard == null) return false;
		return scoreboard.hasScoreboardVisible(p);
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
		String output = ((BukkitPlatform) TAB.getInstance().getPlatform()).setPlaceholders(player, placeholder);
		Map<Object, String> replacements = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements." + placeholder);
		return TAB.getInstance().getPlaceholderManager().findReplacement(replacements, output).toString().replace("%value%", output);
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
