package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.chat.EnumChatFormat;

/**
 * Placeholder replacement pattern class for placeholder output replacements
 * feature.
 */
public class PlaceholderReplacementPattern {

	/** Full replacement map with both keys and values colored */
	private final Map<String, String> replacements = new HashMap<>();

	/**
	 * Map of number intervals where key is a 2-dimensional array
	 * with first value being minimum and second value maximum and value
	 * being output to replace to.
	 * */
	private final Map<float[], String> numberIntervals = new HashMap<>();

	/**
	 * Constructs new instance from given replacement map from config
	 * @param	map
	 * 			replacement map from config
	 */
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
					numberIntervals.put(new float[]{Float.parseFloat(key.split("-")[0]),
							Float.parseFloat(key.split("-")[1])}, value);
				} catch (NumberFormatException e) {
					//not a valid number interval
				}
			}
		}
	}

	/**
	 * Finds replacement using provided output as well as applying
	 * %value% placeholder for original output inside replacements.
	 * @param	output
	 * 			placeholder's output
	 * @return	replacement or {@code output} if no pattern is matching
	 */
	public String findReplacement(String output) {
		String replacement = findReplacement0(output);
		if (replacement == null) return "";
		if (replacement.contains("%value%")) {
			replacement = replacement.replace("%value%", output);
		}
		return replacement;
	}

	/**
	 * Internal method that returns value based on provided
	 * placeholder output and configured replacements.
	 * @param	output
	 * 			placeholder's output
	 * @return	replacement or {@code output} if no pattern is matching
	 */
	private String findReplacement0(String output) {
		//skipping check if no replacements are defined
		if (replacements.isEmpty()) return output;
		
		//exact output
		if (replacements.containsKey(output)) {
			return replacements.get(output);
		}
		
		//number interval
		if (numberIntervals.size() > 0) {
			try {
				//supporting placeholders with fancy output using "," every 3 digits
				String cleanValue = output.contains(",") ? output.replace(",", "") : output;
				float value = Float.parseFloat(cleanValue);
				for (float[] interval : numberIntervals.keySet()) {
					if (interval[0] <= value && value <= interval[1]) return numberIntervals.get(interval);
				}
			} catch (NumberFormatException e) {
				//placeholder output is not a number
			}
		}

		//else
		if (replacements.containsKey("else")) return replacements.get("else");
		
		//nothing was found
		return output;
	}
}