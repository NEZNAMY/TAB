package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of any placeholder
 */
public abstract class TabPlaceholder implements Placeholder {

	private static final String[] EMPTY_ARRAY = new String[0];
	
	//refresh interval of the placeholder
	private final int refresh;
	
	//placeholder identifier including %
	protected final String identifier;
	
	protected final PlaceholderReplacementPattern replacements;
	
	private boolean active;
	private boolean triggerMode;
	private Runnable onActivation;
	private Runnable onDisable;

	protected final List<String> parents = new ArrayList<>();
	
	/**
	 * Constructs new instance with given parameters and loads placeholder output replacements
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	protected TabPlaceholder(String identifier, int refresh) {
		if (refresh % 50 != 0 && refresh != -1) throw new IllegalArgumentException("Refresh interval must be divisible by 50");
		if (!identifier.startsWith("%") || !identifier.endsWith("%")) throw new IllegalArgumentException("Identifier must start and end with %");
		this.identifier = identifier;
		this.refresh = refresh;
		replacements = new PlaceholderReplacementPattern(TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements." + identifier));
		for (String nested : getNestedPlaceholders("")) {
			TAB.getInstance().getPlaceholderManager().getPlaceholder(nested).addParent(identifier);
		}
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
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
		return replace(s, identifier, setPlaceholders(getLastValue(p), p));
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
	protected String setPlaceholders(String text, TabPlayer p) {
		if (identifier.equals(text)) return text;
		String replaced = text;
		for (String s : getNestedPlaceholders(text)) {
			if (s.equals(identifier) || (identifier.startsWith("%sync:") && ("%" + identifier.substring(6)).equals(s)) || s.startsWith("%rel_")) continue;
			replaced = TAB.getInstance().getPlaceholderManager().getPlaceholder(s).set(replaced, p);
		}
		return replaced;
	}
	
	public PlaceholderReplacementPattern getReplacements() {
		return replacements;
	}
	
	@Override
	public void enableTriggerMode() {
		triggerMode = true;
	}
	
	@Override
	public void enableTriggerMode(Runnable onActivation, Runnable onDisable) {
		triggerMode = true;
		this.onActivation = onActivation;
		this.onDisable = onDisable;
		if (active && onActivation != null) onActivation.run();
	}
	
	public void markAsUsed() {
		if (active) return;
		active = true;
		if (onActivation != null) onActivation.run();
	}

	@Override
	public boolean isTriggerMode() {
		return triggerMode;
	}
	
	@Override
	public void unload() {
		if (onDisable != null && active) onDisable.run();
	}

	public abstract void updateFromNested(TabPlayer player);

	/**
	 * Returns last known value for defined player
	 * @param p - player to check value for
	 * @return last known value
	 */
	public abstract String getLastValue(TabPlayer p);

	public void addParent(String parent) {
		if (!parents.contains(parent)) parents.add(parent);
	}
}