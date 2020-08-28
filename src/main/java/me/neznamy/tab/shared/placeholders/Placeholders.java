package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.conditions.Condition;
import me.neznamy.tab.shared.Animation;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * Messy class to be moved into PlaceholderManager class
 */
public class Placeholders {

	public static final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	public static final char colorChar = '\u00a7';

	//all placeholders used in all configuration files, including invalid ones
	public static List<String> allUsedPlaceholderIdentifiers = new ArrayList<String>();

	//plugin internals + PAPI + API
	public static Map<String, Placeholder> registeredPlaceholders = new HashMap<String, Placeholder>();
	public static Map<String, RelationalPlaceholder> registeredRelationalPlaceholders = new HashMap<String, RelationalPlaceholder>();

	public static Set<Placeholder> usedPlaceholders = new HashSet<Placeholder>();

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

	public static Set<String> getUsedPlaceholderIdentifiersRecursive(String... strings){
		Set<String> base = new HashSet<String>();
		for (String string : strings) {
			base.addAll(detectAll(string));
		}
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
						placeholdersTotal.add(p);
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
	public static void categorizeUsedPlaceholder(String identifier) {
		if (identifier.startsWith("%rel_")) {
			if (registeredRelationalPlaceholders.containsKey(identifier)) return;
			Shared.platform.registerUnknownPlaceholder(identifier);
			return;
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
		if (identifier.contains("condition:")) {
			//animation
			String conditionName = identifier.substring(11, identifier.length()-1);
			for (Condition c : Premium.conditions.values()) {
				if (c.getName().equalsIgnoreCase(conditionName)) {
					registerPlaceholder(new PlayerPlaceholder("%condition:" + conditionName + "%", PlaceholderManager.getInstance().defaultRefresh) {

						@Override
						public String get(ITabPlayer p) {
							return c.getText(p);
						}
						
						@Override
						public String[] getChilds(){
							return new String[] {c.yes, c.no};
						}
						
					}, true);
					return;
				}
			}
			Shared.errorManager.startupWarn("Unknown condition &e\"" + conditionName + "\"&c used in configuration. You need to define it in premiumconfig.yml");
			return;
		}
		//placeholderapi or invalid
		Shared.platform.registerUnknownPlaceholder(identifier);
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
		Shared.refreshables.forEach(r -> r.refreshUsedPlaceholders());
	}
}