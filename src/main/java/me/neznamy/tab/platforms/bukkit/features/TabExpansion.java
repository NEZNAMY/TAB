package me.neznamy.tab.platforms.bukkit.features;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

/**
 * TAB's expansion for PlaceholderAPI
 */
public class TabExpansion extends PlaceholderExpansion {

	private JavaPlugin plugin;
	
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
		ITabPlayer p = Shared.getPlayer(player.getUniqueId());
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
			return p.hiddenScoreboard ? "Disabled" : "Enabled";
		}
		if (identifier.equals("bossbar_visible")) {
			return p.bossbarVisible ? "Enabled" : "Disabled";
		}
		return null;
	}
}