package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.*;

public class Placeholders {

	public static final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	public static final char colorChar = '\u00a7';
	
	public static Map<String, Placeholder> registeredInternalPlaceholders;
	
	//used internal placeholders + used placeholderapi placeholders
	public static Map<String, Placeholder> usedPlaceholders;
	
	//all placeholders used in all configuration files, including invalid ones
	public static List<String> allUsedPlaceholderIdentifiers;
	
	//placeholders registered using the API, so they survive /tab reload
	public static List<Placeholder> APIPlaceholders = new ArrayList<Placeholder>();

	public static void clearAll() {
		registeredInternalPlaceholders = new HashMap<String, Placeholder>();
		usedPlaceholders = new HashMap<String, Placeholder>();
		allUsedPlaceholderIdentifiers = new ArrayList<String>();
	}
	public static Collection<Placeholder> getAllPlaceholders(){
		List<Placeholder> list = new ArrayList<>();
		list.addAll(registeredInternalPlaceholders.values());
		list.addAll(APIPlaceholders);
		list.addAll(usedPlaceholders.values());
		return list;
	}
	public static Placeholder getUsedPlaceholder(String identifier) {
		return usedPlaceholders.get(identifier);
	}
	public static Collection<Placeholder> getAllUsed(){
		return usedPlaceholders.values();
	}
	public static List<String> detectAll(String text){
		List<String> placeholders = new ArrayList<>();
		if (text == null) return placeholders;
		Matcher m = placeholderPattern.matcher(text);
		while (m.find()) {
			placeholders.add(m.group());
		}
		return placeholders;
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
	public static void findAllUsed(Object object) {
		if (object instanceof Map) {
			for (Object value : ((Map<String, Object>) object).values()) {
				findAllUsed(value);
			}
		}
		if (object instanceof List) {
			for (Object line : (List<Object>)object) {
				findAllUsed(line);
			}
		}
		if (object instanceof String) {
			for (String placeholder : detectAll((String) object)) {
				if (!allUsedPlaceholderIdentifiers.contains(placeholder)) allUsedPlaceholderIdentifiers.add(placeholder);
			}
		}
	}
	public static void registerUniversalPlaceholders() {
		registerInternalPlaceholder(new PlayerPlaceholder("%rank%", 1000) {
			public String get(ITabPlayer p) {
				return p.getRank();
			}
			@Override
			public String[] getChilds(){
				return Configs.rankAliases.values().toArray(new String[0]);
			}
		});
		registerInternalPlaceholder(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		registerInternalPlaceholder(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int var = Shared.getPlayers().size();
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var--;
				}
				return var+"";
			}
		});
		registerInternalPlaceholder(new PlayerPlaceholder("%"+Shared.separatorType+"%", 1000) {
			public String get(ITabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		registerInternalPlaceholder(new PlayerPlaceholder("%"+Shared.separatorType+"online%", 1000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});
		registerInternalPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		registerInternalPlaceholder(new ServerConstant("%memory-max%") {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		registerInternalPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		registerInternalPlaceholder(new ServerConstant("%memory-max-gb%") {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
		registerInternalPlaceholder(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getName();
			}
		});
		registerInternalPlaceholder(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		registerInternalPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
			}
		});
		registerInternalPlaceholder(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return Shared.getPlayers().size()+"";
			}
		});
		registerInternalPlaceholder(new PlayerPlaceholder("%ping%", 2000) {
			public String get(ITabPlayer p) {
				return p.getPing()+"";
			}
		});
		registerInternalPlaceholder(new PlayerPlaceholder("%player-version%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		for (int i=5; i<=15; i++) {
			final int version = i;
			registerInternalPlaceholder(new ServerPlaceholder("%version-group:1-" + version + "-x%", 1000) {
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
			registerInternalPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 500) {
				public String get(ITabPlayer p) {
					return PluginHooks.LuckPerms_getPrefix(p);
				}
			});
			registerInternalPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 500) {
				public String get(ITabPlayer p) {
					return PluginHooks.LuckPerms_getSuffix(p);
				}
			});
		}
		for (Placeholder placeholder : APIPlaceholders) {
			usedPlaceholders.put(placeholder.getIdentifier(), placeholder);
		}
		for (String placeholder : allUsedPlaceholderIdentifiers) {
			if (!usedPlaceholders.containsKey(placeholder)) {
				categorizeUsedPlaceholder(placeholder);
			}
		}
	}
	public static void categorizeUsedPlaceholder(String placeholder) {
		if (placeholder.contains("%rel_")) return; //relational placeholders are something else

		if (registeredInternalPlaceholders.containsKey(placeholder)) return;
		
		if (placeholder.contains("animation:")) {
			//animation
			String animationName = placeholder.substring(11, placeholder.length()-1);
			for (Animation a : Configs.animations) {
				if (a.getName().equalsIgnoreCase(animationName)) {
					registerInternalPlaceholder(new ServerPlaceholder("%animation:" + animationName + "%", a.getInterval()-1) {
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
	public static void registerInternalPlaceholder(Placeholder placeholder) {
		registeredInternalPlaceholders.put(placeholder.getIdentifier(), placeholder);
		if (allUsedPlaceholderIdentifiers.contains(placeholder.getIdentifier())) {
			usedPlaceholders.put(placeholder.getIdentifier(), placeholder);
		}
	}
	public static void registerPAPIPlaceholder(Placeholder placeholder) {
		usedPlaceholders.put(placeholder.getIdentifier(), placeholder);
	}
	public static void registerAPIPlaceholder(Placeholder placeholder) {
		APIPlaceholders.add(placeholder);
		usedPlaceholders.put(placeholder.getIdentifier(), placeholder);
	}
	
	public static void checkForRegistration(String text) {
		for (String identifier : Placeholders.detectAll(text)) {
			if (Placeholders.usedPlaceholders.containsKey(identifier)) continue;
			if (!Placeholders.allUsedPlaceholderIdentifiers.contains(identifier)) Placeholders.allUsedPlaceholderIdentifiers.add(identifier);
			Placeholders.categorizeUsedPlaceholder(identifier);
		}
	}
}