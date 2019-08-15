package me.neznamy.tab.bukkit;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

	private Main plugin;

	public PlaceholderAPIExpansion(Main plugin){
		this.plugin = plugin;
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
	
	private static final String[] properties = {"tabprefix", "tagprefix", "tabsuffix", "tagsuffix", "abovename", "belowname", "customtabname", "customtagname"};
	
	@Override
	public String onPlaceholderRequest(Player player, String identifier){
		if (player == null) return "";
		ITabPlayer p = Shared.getPlayer(player.getUniqueId());
		if (p == null) return "";

		for (String property : properties) {
			if (identifier.equals(property)) {
				return me.neznamy.tab.shared.Placeholders.replace(p.getActiveProperty(property), p);
			}
			if (identifier.equals(property + "_raw")) {
				return p.getActiveProperty(property);
			}
		}
		return null;
	}
}