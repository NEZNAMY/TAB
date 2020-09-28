package me.neznamy.tab.platforms.bukkit.features;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.PluginHooks;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholder;

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
		TabPlayer p = Shared.getPlayer(player.getUniqueId());
		if (p == null) return "";
		for (EnumProperty property : EnumProperty.values()) {
			if (identifier.equals(property.toString())) {
				return p.getProperty(property.toString()).lastReplacedValue;
			}
			if (identifier.equals(property.toString() + "_raw")) {
				return p.getProperty(property.toString()).getCurrentRawValue();
			}
		}
		if (identifier.equals("scoreboard_visible")) {
			return p.isScoreboardVisible() ? "Enabled" : "Disabled";
		}
		if (identifier.equals("bossbar_visible")) {
			return p.hasBossbarVisible() ? "Enabled" : "Disabled";
		}
		if (identifier.startsWith("replace_") && Premium.is()) {
			String placeholder = "%" + identifier.substring(8) + "%";
			String output = PluginHooks.setPlaceholders(player, placeholder);
			Map<Object, Object> replacements = Premium.premiumconfig.getConfigurationSection("placeholder-output-replacements." + placeholder);
			return Placeholder.findReplacement(replacements, output);
		}
		return null;
	}
}