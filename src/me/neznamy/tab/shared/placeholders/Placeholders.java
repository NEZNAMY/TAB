package me.neznamy.tab.shared.placeholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class Placeholders {

	//my registered placeholders
	public static Map<String, PlayerPlaceholder> myPlayerPlaceholders;
	public static Map<String, ServerPlaceholder> myServerPlaceholders;
	public static Map<String, Constant> myServerConstants;
	
	//my used placeholders + used papi placeholders
	public static Map<String, PlayerPlaceholder> usedPlayerPlaceholders;
	public static Map<String, ServerPlaceholder> usedServerPlaceholders;
	public static Map<String, Constant> usedServerConstants;
	public static List<String> usedPlaceholders;

	public static ConcurrentHashMap<String, Integer> online = new ConcurrentHashMap<String, Integer>();

	static {
		online.put("other", 0);
		for (int i=5; i<=15; i++) online.put("1-" + i + "-x", 0);
	}
	public static void clearAll() {
		myPlayerPlaceholders = new HashMap<String, PlayerPlaceholder>();
		myServerPlaceholders = new HashMap<String, ServerPlaceholder>();
		myServerConstants = new HashMap<String, Constant>();
		
		usedPlayerPlaceholders = new HashMap<String, PlayerPlaceholder>();
		usedServerPlaceholders = new HashMap<String, ServerPlaceholder>();
		usedServerConstants = new HashMap<String, Constant>();
		usedPlaceholders = new ArrayList<String>();
	}
	public static List<Placeholder> getAllUsed(){
		List<Placeholder> usedPlaceholders = new ArrayList<Placeholder>();
		usedPlaceholders.addAll(usedPlayerPlaceholders.values());
		usedPlaceholders.addAll(usedServerPlaceholders.values());
		return usedPlaceholders;
	}
	public static List<String> detectAll(String s){
		List<String> list = new ArrayList<String>();
		if (s == null) return list;
		while (s.contains("%")) {
			s = s.substring(s.indexOf("%")+1, s.length());
			if (s.contains("%")) {
				String placeholder = s.substring(0, s.indexOf("%"));
				s = s.substring(s.indexOf("%")+1, s.length());
				list.add("%" + placeholder + "%");
			}
		}
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
		for (Placeholder pl : detectPlaceholders(string, true)) {
			if (string.contains(pl.getIdentifier())) string = pl.set(string, p);
		}
		for (Constant c : myServerConstants.values()) {
			if (string.contains(c.getIdentifier())) string = string.replace(c.getIdentifier(), c.get());
		}
		for (String removed : Configs.removeStrings) {
			string = string.replace(removed, "");
		}
		string = color(string);
		return string;
	}
	public static List<Placeholder> detectPlaceholders(String rawValue, boolean playerPlaceholders) {
		if (rawValue == null || (!rawValue.contains("%") && !rawValue.contains("{"))) return new ArrayList<Placeholder>();
		List<Placeholder> placeholdersTotal = new ArrayList<Placeholder>();
		for (Placeholder placeholder : playerPlaceholders ? getAllUsed() : usedServerPlaceholders.values()) {
			if (rawValue.contains(placeholder.getIdentifier())) {
				placeholdersTotal.add(placeholder);
				for (String child : placeholder.getChilds()) {
					for (Placeholder p : detectPlaceholders(child, playerPlaceholders)) {
						if (!placeholdersTotal.contains(p)) placeholdersTotal.add(p);
					}
				}
			}
		}
		return placeholdersTotal;
	}
}