package me.neznamy.tab.platforms.bukkit;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class PlaceholderAPIExpansion{

	private static PlaceholderExpansion exp;
	private static final String[] properties = {"tabprefix", "tagprefix", "tabsuffix", "tagsuffix", "abovename", "belowname", "customtabname", "customtagname"};

	public static void register() {
		exp = new PlaceholderExpansion() {

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
				return Main.instance.getDescription().getAuthors().toString();
			}
			@Override
			public String getIdentifier(){
				return "tab";
			}
			@Override
			public String getVersion(){
				return Main.instance.getDescription().getVersion();
			}
			@Override
			public String onPlaceholderRequest(Player player, String identifier){
				if (player == null) return "";
				ITabPlayer p = Shared.getPlayer(player.getUniqueId());
				if (p == null) return "";

				for (String property : properties) {
					if (identifier.equals(property)) {
						return p.properties.get(property).get();
					}
					if (identifier.equals(property + "_raw")) {
						return p.properties.get(property).getCurrentRawValue();
					}
				}
				return null;
			}
		};
		exp.register();
	}
	public static void unregister() {
		if (exp != null) PlaceholderAPI.unregisterExpansion(exp);
	}
}