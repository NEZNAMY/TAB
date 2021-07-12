package me.neznamy.tab.shared.placeholders;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.EnumChatFormat;

/**
 * Representation of any placeholder
 */
public abstract class Placeholder {

	//refresh interval of the placeholder
	private int refresh;
	
	//placeholder identifier including %
	protected String identifier;
	
	//premium replacements
	protected Map<Object, String> replacements = new HashMap<>();
	
	/**
	 * Constructs new instance with given parameters and loads placeholder output replacements
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in millieconds
	 */
	protected Placeholder(String identifier, int refresh) {
		if (refresh % 50 != 0) throw new IllegalArgumentException("Refresh interval must be divisible by 50");
		if (!identifier.startsWith("%") || !identifier.endsWith("%")) throw new IllegalArgumentException("Identifier must start and end with %");
		this.identifier = identifier;
		this.refresh = refresh;
		loadReplacements();
	}

	private void loadReplacements() {
		Map<Object, Object> original = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements." + identifier);
		for (Entry<Object, Object> entry : original.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			replacements.put(key.replace('&', '\u00a7'), value);
			//snakeyaml converts yes & no to booleans, making them not work when used without "
			if (key.equals("true")) {
				replacements.put("yes", value);
			}
			if (key.equals("false")) {
				replacements.put("no", value);
			}
			if (key.equals("else") && value.equals("%value%")) {
				TAB.getInstance().print('9', "Hint: Placeholder " + identifier + " has configured \"else\" replacement to %value%, which is default behavior already. You can remove it for cleaner configuration.");
			}
		}
	}
	
	/**
	 * Returns placeholder's identifier
	 * @return placeholder's identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Returns placeholder's refresh interval
	 * @return placeholder's refresh interval
	 */
	public int getRefresh() {
		return refresh;
	}

	/**
	 * Replaces this placeholder in given string and returns output
	 * @param s - string to replace this placeholder in
	 * @param p - player to set placeholder for
	 * @return string with this placeholder replaced
	 */
	public String set(String s, TabPlayer p) {
		try {
			String value = getLastValue(p);
			if (value == null) value = "";
			String newValue = setPlaceholders(findReplacement(replacements, EnumChatFormat.color(value)), p);
			if (newValue.contains("%value%")) {
				newValue = newValue.replace("%value%", value);
			}
			newValue = EnumChatFormat.color(newValue);
			if (s.equals(identifier)) {
				//small performance and memory save
				return newValue;
			} else {
				return s.replace(identifier, newValue);
			}
		} catch (Exception t) {
			return TAB.getInstance().getErrorManager().printError(s, "An error occurred when setting placeholder " + identifier + (p == null ? "" : " for " + p.getName()), t);
		}
	}
	
	/**
	 * Finds placeholder output replacement
	 * @param replacements - map of replacements from premiumconfig
	 * @param originalOutput - original output of the placeholder
	 * @return replaced placeholder output
	 */
	public static String findReplacement(Map<Object, String> replacements, String originalOutput) {
		if (replacements.isEmpty()) return originalOutput;
		if (replacements.containsKey(originalOutput)) {
			return replacements.get(originalOutput);
		}
		for (Entry<Object, String> entry : replacements.entrySet()) {
			String key = entry.getKey().toString();
			if (key.contains("-")) {
				try {
					float low = Float.parseFloat(key.split("-")[0]);
					float high = Float.parseFloat(key.split("-")[1]);
					float actualValue = Float.parseFloat(originalOutput.replace(",", ""));
					if (low <= actualValue && actualValue <= high) return entry.getValue();
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
					//nope
				}
			}
		}
		if (replacements.containsKey("else")) return replacements.get("else");
		return originalOutput;
	}
	
	/**
	 * Applies all placeholders from outputs
	 * @param text - replaced placeholder
	 * @param p - player to replace for
	 * @return text with replaced placeholders in output
	 */
	protected String setPlaceholders(String text, TabPlayer p) {
		if (!text.contains("%")) return text;
		String replaced = text;
		for (String s : TAB.getInstance().getPlaceholderManager().detectPlaceholders(replaced)) {
			if (s.equals("%value%") || s.equals(identifier) || s.startsWith("%rel_")) continue;
			replaced = TAB.getInstance().getPlaceholderManager().getPlaceholder(s).set(replaced, p);
		}
		return replaced;
	}
	
	/**
	 * Returns last known value for defined player
	 * @param p - player to check value for
	 * @return last known value
	 */
	public abstract String getLastValue(TabPlayer p);
}