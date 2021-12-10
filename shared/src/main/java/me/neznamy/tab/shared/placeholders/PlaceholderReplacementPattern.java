package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.chat.EnumChatFormat;

public class PlaceholderReplacementPattern {

	private final Map<String, String> replacements = new HashMap<>();
	private final String[] numberIntervalKeys;
	private final Map<String, float[]> numberIntervals = new HashMap<>();

	public PlaceholderReplacementPattern(Map<Object, Object> map) {
		for (Entry<Object, Object> entry : map.entrySet()) {
			String key = String.valueOf(entry.getKey());
			String value = String.valueOf(entry.getValue());
			replacements.put(EnumChatFormat.color(key), EnumChatFormat.color(value));
			//snakeyaml converts yes & no to booleans, making them not work when used without "
			if ("true".equals(key)) {
				replacements.put("yes", value);
			} else if ("false".equals(key)) {
				replacements.put("no", value);
			} else if (key.contains("-")) {
				try {
					numberIntervals.put(key, new float[]{Float.parseFloat(key.split("-")[0]), Float.parseFloat(key.split("-")[1])});
				} catch (NumberFormatException e) {
					//not a valid number interval
				}
			}
		}
		numberIntervalKeys = numberIntervals.keySet().toArray(new String[0]);
	}
	
	public String findReplacement(String output) {
		String replacement = findReplacement0(output);
		if (replacement == null) return "";
		if (replacement.contains("%value%")) {
			replacement = replacement.replace("%value%", output);
		}
		return replacement;
	}
	
	private String findReplacement0(String output) {
		//skipping check if no replacements are defined
		if (replacements.isEmpty()) return output;
		
		//exact output
		if (replacements.containsKey(output)) {
			return replacements.get(output);
		}
		
		//number interval
		if (numberIntervalKeys.length > 0) {
			try {
				float value = Float.parseFloat(output.contains(",") ? output.replace(",", "") : output); //supporting placeholders with fancy output using "," every 3 digits
				for (String key : numberIntervalKeys) {
					if (numberIntervals.get(key)[0] <= value && value <= numberIntervals.get(key)[1]) return replacements.get(key);
				}
			} catch (NumberFormatException e) {
				//not a number
			}
		}

		//else
		if (replacements.containsKey("else")) return replacements.get("else");
		
		//nothing found
		return output;
	}
}