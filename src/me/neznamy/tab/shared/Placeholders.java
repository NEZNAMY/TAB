package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

import me.clip.placeholderapi.PlaceholderAPI;

public class Placeholders {

	public static List<Placeholder> list;
	public static ConcurrentHashMap<String, Integer> online = new ConcurrentHashMap<String, Integer>();
	public static boolean placeholderAPI;

	public static void recalculateOnlineVersions() {
		online.put("1-14-x", 0);
		online.put("1-13-x", 0);
		online.put("1-12-x", 0);
		online.put("1-11-x", 0);
		online.put("1-10-x", 0);
		online.put("1-9-x", 0);
		online.put("1-8-x", 0);
		online.put("1-7-x", 0);
		online.put("1-6-x", 0);
		online.put("1-5-x", 0);
		online.put("other", 0);
		for (ITabPlayer p : Shared.getPlayers()){
			int version = p.getVersion().getNumber();
			if (version == 60 || version == 61) online.put("1-5-x", online.get("1-5-x")+1);
			else if (version >= 73 && version <= 78) online.put("1-6-x", online.get("1-6-x")+1);
			else if (version <= 0) online.put("other", online.get("other")+1);
			else if (version <= 5) online.put("1-7-x", online.get("1-7-x")+1);
			else if (version <= 47) online.put("1-8-x", online.get("1-8-x")+1);
			else if (version <= 110) online.put("1-9-x", online.get("1-9-x")+1);
			else if (version <= 210) online.put("1-10-x", online.get("1-10-x")+1);
			else if (version <= 316) online.put("1-11-x", online.get("1-11-x")+1);
			else if (version <= 340) online.put("1-12-x", online.get("1-12-x")+1);
			else if (version <= 404) online.put("1-13-x", online.get("1-13-x")+1);
			else if (version <= 498) online.put("1-14-x", online.get("1-14-x")+1);
			else online.put("1-14-x", online.get("1-14-x")+1); //current newest one
		}
	}
	//code taken from bukkit, so it can work on bungee too
	public static String color(String textToTranslate){
		if (!textToTranslate.contains("&")) return textToTranslate;
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[(i + 1)]) > -1)){
				b[i] = '§';
				b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
			}
		}
		return new String(b);
	}
	//code taken from bukkit, so it can work on bungee too
	public static String getLastColors(String input) {
		String result = "";
		int length = input.length();
		for (int index = length - 1; index > -1; index--){
			char section = input.charAt(index);
			if ((section == '§') && (index < length - 1)){
				char c = input.charAt(index + 1);
				if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(c+"")) {
					result = "§" + c + result;
					if ("0123456789AaBbCcDdEeFfRr".contains(c+"")) {
						break;
					}
				}
			}
		}
		return result;
	}
	public static String replaceAllPlaceholders(String string, ITabPlayer p) {
		return set(string, list, p);
	}
	public static String set(String string, List<Placeholder> placeholders, ITabPlayer p) {
		for (Placeholder pl : placeholders) {
			if (string.contains(pl.getIdentifier())) string = pl.set(string, p);
		}
		string = setPlaceholderAPIPlaceholders(string, p);
		string = color(string);
		for (String removed : Configs.removeStrings) {
			string = string.replace(removed, "");
		}
		return string;
	}
	public static String setPlaceholderAPIPlaceholders(String s, ITabPlayer p) {
		try {
			if (!placeholderAPI) return s;
			return PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), s);
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.error("PlaceholderAPI replace task failed.");
				Shared.error("PlaceholderAPI version: " + papi.getDescription().getVersion());
				Shared.error("String to parse: " + s);
				Shared.error("Please send this error to the FIRST author whose name or plugin name you see here:", t);
			} //else now we know why it failed
		}
		return s;
	}
	public static List<Placeholder> detect(String rawValue) {
		if (!rawValue.contains("%") && !rawValue.contains("{")) return Lists.newArrayList();
		List<Placeholder> placeholdersTotal = new ArrayList<Placeholder>();
		String testString = rawValue;
		boolean changed;
		for (int i=0; i<10; i++) { //detecting placeholder chains
			changed = false;
			for (Placeholder pl : Placeholders.list) {
				if (testString.contains(pl.getIdentifier())) {
//					testString = pl.set(testString, owner);
					if (!placeholdersTotal.contains(pl)) placeholdersTotal.add(pl);
					changed = true;
					for (String child : pl.getChilds()) {
						List<Placeholder> placeholders = detect(child);
						for (Placeholder p : placeholders) {
							if (!placeholdersTotal.contains(p)) placeholdersTotal.add(p);
							changed = true;
						}
					}
				}
			}
			if (!changed) break; //no more placeholders found
		}
		return placeholdersTotal;
	}
}