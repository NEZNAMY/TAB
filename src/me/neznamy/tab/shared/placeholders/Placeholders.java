package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.Animation;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Shared;

public class Placeholders {

	public static final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	public static final char colorChar = '\u00a7';
	
	//all placeholders used in all configuration files, including invalid ones
	public static List<String> allUsedPlaceholderIdentifiers = new ArrayList<String>();
	
	//plugin internals + PAPI + API
	public static Map<String, Placeholder> registeredPlaceholders = new HashMap<String, Placeholder>();
	public static Map<String, RelationalPlaceholder> registeredRelationalPlaceholders = new HashMap<String, RelationalPlaceholder>();

	public static List<Placeholder> usedPlaceholders = new ArrayList<Placeholder>();
	
	public static Collection<Placeholder> getAllPlaceholders(){
		return registeredPlaceholders.values();
	}
	public static Placeholder getPlaceholder(String identifier) {
		return registeredPlaceholders.get(identifier);
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

	public static Set<String> getUsedPlaceholderIdentifiersRecursive(String line){
		Set<String> base = new HashSet<String>(detectAll(line));
		for (String placeholder : base.toArray(new String[0])) {
			List<Placeholder> pl = detectPlaceholders(placeholder);
			for (Placeholder p : pl) {
				base.add(p.getIdentifier());
			}
		}
		return base;
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
		for (Placeholder placeholder : getAllPlaceholders()) {
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
	public static List<RelationalPlaceholder> detectRelationalPlaceholders(String rawValue) {
		if (rawValue == null || !rawValue.contains("%")) return new ArrayList<RelationalPlaceholder>();
		List<RelationalPlaceholder> placeholders = new ArrayList<RelationalPlaceholder>();
		for (String identifier : registeredRelationalPlaceholders.keySet()) {
			if (rawValue.contains(identifier)) {
				placeholders.add(registeredRelationalPlaceholders.get(identifier));
			}
		}
		return placeholders;
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
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
			}
		});
		registerPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
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
		if (PluginHooks.luckPerms) {
			registerPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 500) {
				public String get(ITabPlayer p) {
					return PluginHooks.LuckPerms_getPrefix(p);
				}
			});
			registerPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 500) {
				public String get(ITabPlayer p) {
					return PluginHooks.LuckPerms_getSuffix(p);
				}
			});
		}
		for (String placeholder : allUsedPlaceholderIdentifiers) {
			categorizeUsedPlaceholder(placeholder);
		}
	}
	public static void categorizeUsedPlaceholder(String identifier) {
		if (identifier.startsWith("%rel_")) {
			if (registeredRelationalPlaceholders.containsKey(identifier)) return;
			Shared.mainClass.registerUnknownPlaceholder(identifier);
		}

		if (registeredPlaceholders.containsKey(identifier)) {
			if (!(registeredPlaceholders.get(identifier) instanceof ServerConstant)) usedPlaceholders.add(registeredPlaceholders.get(identifier));
			return;
		}
		
		if (identifier.contains("animation:")) {
			//animation
			String animationName = identifier.substring(11, identifier.length()-1);
			for (Animation a : Configs.animations) {
				if (a.getName().equalsIgnoreCase(animationName)) {
					registerPlaceholder(new ServerPlaceholder("%animation:" + animationName + "%", a.getInterval()) {
						public String get() {
							return a.getMessage();
						}
						@Override
						public String[] getChilds(){
							return a.getAllMessages();
						}
					}, true);
					return;
				}
			}
			Shared.errorManager.startupWarn("Unknown animation &e\"" + animationName + "\"&c used in configuration. You need to define it in animations.yml");
			return;
		}
		//placeholderapi or invalid
		Shared.mainClass.registerUnknownPlaceholder(identifier);
	}
	public static void registerPlaceholder(Placeholder placeholder) {
		registerPlaceholder(placeholder, false);
	}
	public static void registerPlaceholder(Placeholder placeholder, boolean addToUsed) {
		registeredPlaceholders.put(placeholder.getIdentifier(), placeholder);
		if (!(placeholder instanceof ServerConstant) && addToUsed) usedPlaceholders.add(placeholder);
	}
	public static void registerPlaceholder(RelationalPlaceholder placeholder) {
		registeredRelationalPlaceholders.put(placeholder.identifier, placeholder);
	}
	public static void checkForRegistration(String text) {
		for (String identifier : detectAll(text)) {
			if (!allUsedPlaceholderIdentifiers.contains(identifier)) allUsedPlaceholderIdentifiers.add(identifier);
			categorizeUsedPlaceholder(identifier);
		}
	}
}