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

import me.neznamy.tab.shared.Shared;

/**
 * Messy class to be moved into PlaceholderManager class
 */
public class Placeholders {

	public static final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	public static final char colorChar = '\u00a7';

	//all placeholders used in all configuration files + API, including invalid ones
	public static Set<String> allUsedPlaceholderIdentifiers = new HashSet<String>();

	//plugin internals + PAPI + API
	public static Map<String, Placeholder> registeredPlaceholders = new HashMap<String, Placeholder>();

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

	public static List<String> getUsedPlaceholderIdentifiersRecursive(String... strings){
		List<String> base = new ArrayList<String>();
		for (String string : strings) {
			for (String s : detectAll(string)) {
				if (!base.contains(s)) base.add(s);
			}
		}
		for (String placeholder : new HashSet<String>(base)) {
			Placeholder pl = getPlaceholder(placeholder);
			if (pl == null) continue;
			for (String nestedString : pl.getNestedStrings()) {
				base.addAll(getUsedPlaceholderIdentifiersRecursive(nestedString));
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
			if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[(i + 1)]) > -1)){
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
				allUsedPlaceholderIdentifiers.add(placeholder);
			}
		}
	}
	public static void categorizeUsedPlaceholder(String identifier) {
		if (registeredPlaceholders.containsKey(identifier)) {
			//internal placeholder
			return;
		}

		//placeholderapi or invalid
		Shared.platform.registerUnknownPlaceholder(identifier);
	}
	
	public static void registerPlaceholder(Placeholder placeholder) {
		registeredPlaceholders.put(placeholder.getIdentifier(), placeholder);
	}

	public static void checkForRegistration(String... texts) {
		for (String text : texts) {
			for (String identifier : detectAll(text)) {
				allUsedPlaceholderIdentifiers.add(identifier);
				categorizeUsedPlaceholder(identifier);
			}
		}
		Shared.featureManager.refreshUsedPlaceholders();
	}
}