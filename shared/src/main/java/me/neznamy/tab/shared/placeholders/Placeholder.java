package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Representation of any placeholder
 */
public abstract class Placeholder {

	private static final String[] EMPTY_ARRAY = new String[0];
	
	//refresh interval of the placeholder
	private int refresh;
	
	//placeholder identifier including %
	protected String identifier;
	
	protected PlaceholderReplacementPattern replacements;
	
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
		replacements = new PlaceholderReplacementPattern(TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements." + identifier));
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
		String originalvalue = getLastValue(p);
		String value = replacements.findReplacement(originalvalue);
		value = replace(value, "%value%", originalvalue);
		return replace(s, identifier, value);
	}
	
	public String[] getNestedPlaceholders(String output) {
		if (!output.contains("%")) return EMPTY_ARRAY;
		return TabAPI.getInstance().getPlaceholderManager().detectPlaceholders(output).toArray(EMPTY_ARRAY);
	}
	
	private String replace(String string, String original, String replacement) {
		if (!string.contains(original)) return string;
		if (string.equals(original)) return replacement;
		return string.replace(original, replacement);
	}

	/**
	 * Applies all placeholders from outputs
	 * @param text - replaced placeholder
	 * @param p - player to replace for
	 * @return text with replaced placeholders in output
	 */
	protected Object setPlaceholders(Object text, TabPlayer p) {
		if (!(text instanceof String) || identifier.equals(text)) return text;
		Object replaced = text;
		for (String s : getNestedPlaceholders((String) text)) {
			if (s.equals("%value%") || s.equals(identifier) || (identifier.startsWith("%sync:") && s.equals("%" + identifier.substring(6)))|| s.startsWith("%rel_")) continue;
			replaced = TAB.getInstance().getPlaceholderManager().getPlaceholder(s).set(replaced.toString(), p);
		}
		return replaced;
	}
	
	public PlaceholderReplacementPattern getReplacements() {
		return replacements;
	}

	/**
	 * Returns last known value for defined player
	 * @param p - player to check value for
	 * @return last known value
	 */
	public abstract String getLastValue(TabPlayer p);
}