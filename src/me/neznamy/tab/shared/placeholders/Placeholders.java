package me.neznamy.tab.shared.placeholders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;

public class Placeholders {

	public static List<PlayerPlaceholder> playerPlaceholders;
	public static List<ServerPlaceholder> serverPlaceholders;
	public static List<Constant> constants;
	public static List<String> usedPAPIPlaceholders;
	public static ConcurrentHashMap<String, Integer> online = new ConcurrentHashMap<String, Integer>();

	static {
		online.put("other", 0);
		for (int i=5; i<=15; i++) online.put("1-" + i + "-x", 0);
	}
	public static List<Placeholder> getAll(){
		List<Placeholder> list = new ArrayList<Placeholder>();
		list.addAll(playerPlaceholders);
		list.addAll(serverPlaceholders);
		return list;
	}
	public static void recalculateOnlineVersions() {
		online.put("other", 0);
		for (int i=5; i<=15; i++) online.put("1-" + i + "-x", 0);
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
				b[i] = Shared.COLOR;
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
			if ((section == Shared.COLOR) && (index < length - 1)){
				char c = input.charAt(index + 1);
				if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(c+"")) {
					result = Shared.COLOR + "" + c + result;
					if ("0123456789AaBbCcDdEeFfRr".contains(c+"")) {
						break;
					}
				}
			}
		}
		return result;
	}
	public static String replaceAllPlaceholders(String string, ITabPlayer p) {
		for (Placeholder pl : Property.detectPlaceholders(string, true)) {
			if (string.contains(pl.getIdentifier())) string = pl.set(string, p);
		}
		for (Constant c : Placeholders.constants) {
			if (string.contains(c.getIdentifier())) string = string.replace(c.getIdentifier(), c.get());
		}
		string = PluginHooks.PlaceholderAPI_setPlaceholders(p, string);
		for (String removed : Configs.removeStrings) {
			string = string.replace(removed, "");
		}
		string = color(string);
		return string;
	}
}