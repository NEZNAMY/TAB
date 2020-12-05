package me.neznamy.tab.platforms.bukkit.features;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitMethods;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Property;
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
		Bukkit.getScheduler().runTask(plugin, () -> register());
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
		if (identifier.equals("scoreboard_visible")) {
			return p.isScoreboardVisible() ? "Enabled" : "Disabled";
		}
		if (identifier.equals("bossbar_visible")) {
			return p.hasBossbarVisible() ? "Enabled" : "Disabled";
		}
		if (identifier.equals("ntpreview")) {
			return p.isPreviewingNametag() ? "Enabled" : "Disabled";
		}
		if (identifier.startsWith("replace_") && Premium.is()) {
			String placeholder = "%" + identifier.substring(8) + "%";
			String output = ((BukkitMethods) Shared.platform).setPlaceholders(player, placeholder);
			Map<Object, String> replacements = Premium.premiumconfig.getConfigurationSection("placeholder-output-replacements." + placeholder);
			String replacement = Placeholder.findReplacement(replacements, output);
			if (replacement.contains("%value%")) {
				replacement = replacement.replace("%value%", output);
			}
			return replacement;
		}
		if (identifier.startsWith("placeholder_")) {
			String placeholder = "%" + identifier.substring(12) + "%";
			//using Property function for fast & easy handling of nested placeholders and different placeholder types
			return new Property(p, placeholder).get();
		}
		String placeholder = identifier.replace("_raw", "");
		Property prop = p.getProperty(placeholder);
		if (prop != null) {
		    if (identifier.endsWith("_raw")) {
		    	return prop.getCurrentRawValue();
		    }
		    return prop.lastReplacedValue;
		}
		return null;
	}
}
