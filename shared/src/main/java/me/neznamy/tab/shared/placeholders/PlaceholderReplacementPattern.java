package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.chat.EnumChatFormat;

public class PlaceholderReplacementPattern {

	private Map<String, String> replacements = new HashMap<>();
	private String[] numberIntervalKeys;
	private Map<String, float[]> numberIntervals = new HashMap<>();

	public PlaceholderReplacementPattern(Map<Object, Object> map) {
		for (Entry<Object, Object> entry : map.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			replacements.put(EnumChatFormat.color(key), EnumChatFormat.color(value));
			//snakeyaml converts yes & no to booleans, making them not work when used without "
			if ("true".equals(key)) {
				replacements.put("yes", value);
			} else if ("false".equals(key)) {
				replacements.put("no", value);
			} else if (key.contains("-")) {
				float[] interval = new float[2];
				try {
					interval[0] = Float.parseFloat(key.split("-")[0]);
					interval[1] = Float.parseFloat(key.split("-")[1]);
					numberIntervals.put(key, interval);
				} catch (NumberFormatException e) {
					//not a valid number interval
				}
			}
		}
		numberIntervalKeys = numberIntervals.keySet().toArray(new String[0]);
	}
	
	public String findReplacement(String output) {
		String replacement = findReplacement0(output);
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
		float actualValue = 0; //only trying to parse if something actually uses numbers intervals
		boolean valueSet = false;
		for (String key : numberIntervalKeys) {
			if (!valueSet) {
				actualValue = Float.parseFloat(output.contains(",") ? output.replace(",", "") : output); //supporting placeholders with fancy output using "," every 3 digits
				valueSet = true;
			}
			if (numberIntervals.get(key)[0] <= actualValue && actualValue <= numberIntervals.get(key)[1]) return replacements.get(key);
		}

		//else
		if (replacements.containsKey("else")) return replacements.get("else");
		
		//nothing found
		return output;
	}
}