package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import me.neznamy.tab.shared.*;

public class Placeholders {

	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	public static final char colorChar = '\u00a7';
	
	//internal placeholders + used placeholderapi placeholders
	public static Map<String, Placeholder> myPlaceholders;
	
	//used internal placeholders + used placeholderapi placeholders
	public static Map<String, Placeholder> usedPlaceholders;
	
	//all placeholders used in all configuration files, including invalid ones
	public static List<String> allUsedPlaceholders;
	
	//placeholders registered using the API, so they survive /tab reload
	public static List<Placeholder> permanentPlaceholders = new ArrayList<Placeholder>();

	public static void clearAll() {
		myPlaceholders = new HashMap<String, Placeholder>();
		usedPlaceholders = new HashMap<String, Placeholder>();
		allUsedPlaceholders = new ArrayList<String>();
	}
	public static Collection<Placeholder> getAllPlaceholders(){
		return myPlaceholders.values();
	}
	public static Collection<Placeholder> getAllUsed(){
		return usedPlaceholders.values();
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

	//code taken from bukkit, so it can work on bungee too
	public static String color(String textToTranslate){
		if (textToTranslate == null) return null;
		if (!textToTranslate.contains("&")) return textToTranslate;
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[(i + 1)]) > -1)){
				b[i] = colorChar;
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
			if ((section == colorChar) && (index < length - 1)){
				char c = input.charAt(index + 1);
				if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(c+"")) {
					result = colorChar + "" + c + result;
					if ("0123456789AaBbCcDdEeFfRr".contains(c+"")) {
						break;
					}
				}
			}
		}
		return result;
	}
	public static List<Placeholder> detectPlaceholders(String rawValue) {
		if (rawValue == null || !rawValue.contains("%")) return new ArrayList<Placeholder>();
		List<Placeholder> placeholdersTotal = new ArrayList<Placeholder>();
		for (Placeholder placeholder : getAllUsed()) {
			if (rawValue.contains(placeholder.getIdentifier())) {
				placeholdersTotal.add(placeholder);
				for (String child : placeholder.getChilds()) {
					for (Placeholder p : detectPlaceholders(child)) {
						if (!placeholdersTotal.contains(p)) placeholdersTotal.add(p);
					}
				}
			}
		}
		return placeholdersTotal;
	}
	@SuppressWarnings("unchecked")
	public static void findAllUsed(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				for (String placeholder : detectAll((String) value)) {
					if (!allUsedPlaceholders.contains(placeholder)) allUsedPlaceholders.add(placeholder);
				}
			}
			if (value instanceof Map) findAllUsed((Map<String, Object>) value);
			if (value instanceof List) {
				for (Object line : (List<Object>)value) {
					for (String placeholder : detectAll(line+"")) {
						if (!allUsedPlaceholders.contains(placeholder)) allUsedPlaceholders.add(placeholder);
					}
				}
			}
		}
	}
	public static void registerUniversalPlaceholders() {
		registerPlaceholder(new PlayerPlaceholder("%rank%", 1000) {
			public String get(ITabPlayer p) {
				return p.getRank();
			}
			@Override
			public String[] getChilds(){
				return Configs.rankAliases.values().toArray(new String[0]);
			}
		});
		registerPlaceholder(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		registerPlaceholder(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int var = Shared.getPlayers().size();
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var--;
				}
				return var+"";
			}
		});
		registerPlaceholder(new PlayerPlaceholder("%"+Shared.separatorType+"%", 1000) {
			public String get(ITabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		registerPlaceholder(new PlayerPlaceholder("%"+Shared.separatorType+"online%", 1000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});
		registerPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		registerPlaceholder(new ServerConstant("%memory-max%") {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		registerPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		registerPlaceholder(new ServerConstant("%memory-max-gb%") {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
		registerPlaceholder(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getName();
			}
		});
		registerPlaceholder(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		registerPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		registerPlaceholder(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return Shared.getPlayers().size()+"";
			}
		});
		registerPlaceholder(new PlayerPlaceholder("%ping%", 2000) {
			public String get(ITabPlayer p) {
				return p.getPing()+"";
			}
		});
		registerPlaceholder(new PlayerPlaceholder("%player-version%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		for (int i=5; i<=15; i++) {
			final int version = i;
			registerPlaceholder(new ServerPlaceholder("%version-group:1-" + version + "-x%", 1000) {
				public String get() {
					int count = 0;
					for (ITabPlayer p : Shared.getPlayers()) {
						if (p.getVersion().getMinorVersion() == version) count++;
					}
					return count+"";
				}
			});
		}
		if (PluginHooks.luckPerms) {
			registerPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 49) {
				public String get(ITabPlayer p) {
					return PluginHooks.LuckPerms_getPrefix(p);
				}
			});
			registerPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 49) {
				public String get(ITabPlayer p) {
					return PluginHooks.LuckPerms_getSuffix(p);
				}
			});
		}
		for (Placeholder pl : permanentPlaceholders) {
			registerPlaceholder(pl);
		}
		for (String placeholder : allUsedPlaceholders) {
			if (!usedPlaceholders.containsKey(placeholder)){
				categorizeUsedPlaceholder(placeholder);
			}
		}
	}
	public static void categorizeUsedPlaceholder(String placeholder) {
		if (placeholder.contains("%rel_")) return; //relational placeholders are something else

		if (myPlaceholders.containsKey(placeholder)) {
			//internal placeholder
			usedPlaceholders.put(placeholder, myPlaceholders.get(placeholder));
			return;
		}
		if (placeholder.contains("animation:")) {
			//animation
			String animationName = placeholder.substring(11, placeholder.length()-1);
			for (Animation a : Configs.animations) {
				if (a.getName().equalsIgnoreCase(animationName)) {
					registerPlaceholder(new ServerPlaceholder("%animation:" + animationName + "%", a.getInterval()-1) {
						public String get() {
							return a.getMessage();
						}
						@Override
						public String[] getChilds(){
							return a.getAllMessages();
						}
					});
					return;
				}
			}
			Shared.errorManager.startupWarn("Unknown animation &e\"" + animationName + "\"&c used in configuration. You need to define it in animations.yml");
			return;
		}
		//placeholderapi or invalid
		Shared.mainClass.registerUnknownPlaceholder(placeholder);
	}
	public static void registerPlaceholder(Placeholder placeholder) {
		registerPlaceholder(placeholder, false);
	}
	public static void registerPlaceholder(Placeholder placeholder, boolean viaAPI) {
		myPlaceholders.put(placeholder.getIdentifier(), placeholder);
		if (viaAPI || allUsedPlaceholders.contains(placeholder.getIdentifier())) usedPlaceholders.put(placeholder.getIdentifier(), placeholder);
		if (viaAPI) permanentPlaceholders.add(placeholder);
	}
}