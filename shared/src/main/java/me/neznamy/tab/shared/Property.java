package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.rgb.RGBUtils;

/**
 * A string with placeholders
 */
public class Property {

	//owner of the property
	private TabPlayer owner;
	
	//raw value
	private String rawValue;
	
	//value assigned via API
	private String temporaryValue;
	
	//last known output after placeholder replacement
	public String lastReplacedValue;
	
	//source of property's raw value
	private String source;

	//used placeholders in current raw value
	public List<String> placeholders;
	
	//used relational placeholders in current raw value
	public List<String> relPlaceholders;

	public Property(TabPlayer owner, String rawValue) {
		this(owner, rawValue, null);
	}
	
	public Property(TabPlayer owner, String rawValue, String source) {
		this.owner = owner;
		this.source = source;
		this.rawValue = RGBUtils.getInstance().applyFormats((rawValue == null ? "" : rawValue), true);
		analyze(this.rawValue);
		update();
	}

	/**
	 * Finds all placeholders used in the value
	 * @param value - raw value to be checked
	 */
	private void analyze(String value) {
		List<String> placeholders = new ArrayList<String>();
		List<String> relPlaceholders = new ArrayList<String>();
		for (String identifier : TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(value)) {
			if (identifier.startsWith("%rel_")) {
				relPlaceholders.add(identifier);
			} else {
				placeholders.add(identifier);
			}
		}
		//avoiding rare concurrent modification in #update
		this.placeholders = placeholders;
		this.relPlaceholders = relPlaceholders;
	}
	
	/**
	 * Temporarily overrides current raw value with an API call
	 * @param temporaryValue - temporary value to be assigned
	 */
	public void setTemporaryValue(String temporaryValue) {
		if (temporaryValue != null) {
			this.temporaryValue = RGBUtils.getInstance().applyFormats(temporaryValue, true);
			analyze(this.temporaryValue);
		} else {
			this.temporaryValue = null;
			analyze(rawValue);
		}
		update();
	}
	
	/**
	 * Changes raw value to new one
	 * @param newValue - new value to be used
	 */
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = RGBUtils.getInstance().applyFormats(newValue, true);
		if (temporaryValue == null) {
			analyze(rawValue);
			update();
		}
	}
	
	/**
	 * Returns temporary value (via API) if present, raw value otherwise
	 * @return current raw value
	 */
	public String getCurrentRawValue() {
		return temporaryValue != null ? temporaryValue : rawValue;
	}
	
	/**
	 * Returns current temporary value
	 * @return temporary value or null if not set
	 */
	public String getTemporaryValue() {
		return temporaryValue;
	}
	
	/**
	 * Returns original raw value ignoring API calls
	 * @return original raw value
	 */
	public String getOriginalRawValue() {
		return rawValue;
	}
	
	/**
	 * Returns source of this raw value or "API" if source is an API call
	 * @return source of the value
	 */
	public String getSource() {
		return temporaryValue == null ? source : "API";
	}
	
	/**
	 * Changes source value to new one
	 * @param source - new source
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	/**
	 * Replaces all placeholders in current raw value, colorizes it, removes remove-strings and returns it
	 * @return updated value
	 */
	public String updateAndGet() {
		update();
		return get();
	}
	
	/**
	 * Replaces all placeholders in current raw value, colorizes it, removes remove-strings and returns whether value changed or not
	 * @return if updating changed value or not
	 */
	public boolean update() {
		long time = System.nanoTime();
		String string = getCurrentRawValue();
		for (String identifier : placeholders) {
			Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
			if (pl != null) string = pl.set(string, owner);
		}
		string = TAB.getInstance().getPlaceholderManager().color(string);
		string = applyRemoveStrings(string);
		if (lastReplacedValue == null || !lastReplacedValue.equals(string)) {
			lastReplacedValue = string;
			TAB.getInstance().getCPUManager().addMethodTime("Property#update", System.nanoTime()-time);
			return true;
		}
		TAB.getInstance().getCPUManager().addMethodTime("Property#update", System.nanoTime()-time);
		return false;
	}
	
	private String applyRemoveStrings(String text) {
		String reformatted = text;
		for (String removed : TAB.getInstance().getConfiguration().removeStrings) {
			if (removed.startsWith("CONTAINS:") && reformatted.contains(removed.substring(9))) return "";
			if (removed.startsWith("STARTS:") && reformatted.startsWith(removed.substring(7))) return "";
			if (removed.startsWith("ENDS:") && reformatted.endsWith(removed.substring(5))) return "";
			if (reformatted.contains(removed)) reformatted = reformatted.replace(removed, "");
		}
		return reformatted;
	}
	
	/**
	 * Returns last known value
	 * @return last known value
	 */
	public String get() {
		return lastReplacedValue;
	}
	
	/**
	 * Returns value for defined viewer by applying relational placeholders to last known value
	 * @param viewer - the viewer
	 * @return format for the viewer
	 */
	public String getFormat(TabPlayer viewer) {
		if (viewer == null) return lastReplacedValue;
		String format = lastReplacedValue;
		for (String identifier : relPlaceholders) {
			RelationalPlaceholder pl = (RelationalPlaceholder) TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
			if (pl != null) format = format.replace(pl.getIdentifier(), pl.getLastValue(viewer, owner));
		}
		return format;
	}
}