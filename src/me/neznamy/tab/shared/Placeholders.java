package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.TabPlayer;

public class Placeholders {

	public static List<Placeholder> playerPlaceholders;
	public static List<Placeholder> serverPlaceholders;
	public static ConcurrentHashMap<String, Integer> online = new ConcurrentHashMap<String, Integer>();
	public static boolean placeholderAPI;

	static {
		online.put("other", 0);
		for (int i=5; i<=14; i++) online.put("1-" + i + "-x", 0);
	}
	public static List<Placeholder> getAll(){
		List<Placeholder> list = new ArrayList<Placeholder>();
		list.addAll(playerPlaceholders);
		list.addAll(serverPlaceholders);
		return list;
	}
	public static void recalculateOnlineVersions() {
		online.put("other", 0);
		for (int i=5; i<=14; i++) online.put("1-" + i + "-x", 0);
		for (ITabPlayer p : Shared.getPlayers()){
			String group = "1-"+p.getVersion().getMinorVersion()+"-x";
			if (online.containsKey(group)) {
				online.put(group, online.get(group)+1);
			} else {
				online.put("other", online.get("other")+1);
			}
		}
	}
	//code taken from bukkit, so it can work on bungee too
	public static String color(String textToTranslate){
		if (textToTranslate == null) return null;
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
		return set(string, Property.detectPlaceholders(string, p), p);
	}
	public static String set(String string, List<Placeholder> placeholders, ITabPlayer p) {
		for (Placeholder pl : placeholders) {
			if (string.contains(pl.getIdentifier())) string = pl.set(string, p);
		}
		string = setPlaceholderAPIPlaceholders(string, p);
		for (String removed : Configs.removeStrings) {
			string = string.replace(removed, "");
		}
		string = color(string);
		return string;
	}
	public static String setPlaceholderAPIPlaceholders(String s, ITabPlayer p) {
		try {
			if (placeholderAPI) return PlaceholderAPI.setPlaceholders(((TabPlayer)p).player, s);
		} catch (Throwable t) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) {
				Shared.error("PlaceholderAPI replace task failed. PlaceholderAPI version: " + papi.getDescription().getVersion());
				Shared.error("String to replace: " + s);
				Shared.error("Please send this error to the FIRST author whose name or plugin name you see here:", t);
			} else {
				//thats why it failed
				placeholderAPI = false;
			}
		}
		return s;
	}
	public static String setRelational(ITabPlayer one, ITabPlayer two, String format) {
		return placeholderAPI ? PlaceholderAPI.setRelationalPlaceholders(((TabPlayer)one).player, ((TabPlayer)two).player, format) : format;
	}
}