package me.neznamy.tab.platforms.bukkit.features;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.EnumProperty;
import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.interfaces.Loadable;

public class TabExpansion implements Loadable{

	private PlaceholderExpansion exp;

	@Override
	public void load() {
		exp = new PlaceholderExpansion() {

			public boolean persist(){
				return true;
			}
			public boolean canRegister(){
				return true;
			}
			public String getAuthor(){
				return Main.instance.getDescription().getAuthors().toString();
			}
			public String getIdentifier(){
				return "tab";
			}
			public String getVersion(){
				return Main.instance.getDescription().getVersion();
			}
			public String onPlaceholderRequest(Player player, String identifier){
				if (player == null) return "";
				ITabPlayer p = Shared.getPlayer(player.getUniqueId());
				if (p == null) return "";
				if (identifier.equals("error")) throw new RuntimeException("Printing Stack Trace");
				for (EnumProperty property : EnumProperty.values()) {
					if (identifier.equals(property.toString())) {
						return p.properties.get(property.toString()).lastReplacedValue;
					}
					if (identifier.equals(property.toString() + "_raw")) {
						return p.properties.get(property.toString()).getCurrentRawValue();
					}
				}
				return null;
			}
		};
		exp.register();
	}
	@Override
	public void unload() {
		try {
			PlaceholderAPI.unregisterExpansion(exp);
		} catch (Exception ExpansionUnregisterEventMayOnlyBeTriggeredSynchronously) {
			// java.lang.IllegalStateException: ExpansionUnregisterEvent may only be triggered synchronously.
		}
	}
}