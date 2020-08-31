package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;

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
	public List<Placeholder> placeholders = new ArrayList<Placeholder>();
	
	//used relational placeholders in current raw value
	public List<RelationalPlaceholder> relPlaceholders = new ArrayList<RelationalPlaceholder>();

	public Property(TabPlayer owner, String rawValue, String source) {
		if (rawValue == null) rawValue = "";
		this.owner = owner;
		this.source = source;
		this.rawValue = rawValue;
		analyze(rawValue);
		update();
	}
	
	/**
	 * Finds all placeholders used in the value
	 * @param value - raw value to be checked
	 */
	private void analyze(String value) {
		placeholders = Placeholders.detectPlaceholders(value);
		relPlaceholders = Placeholders.detectRelationalPlaceholders(value);
	}
	
	/**
	 * Temporarily overrides current raw value with an API call
	 * @param temporaryValue - temporary value to be assigned
	 */
	public void setTemporaryValue(String temporaryValue) {
		this.temporaryValue = temporaryValue;
		if (temporaryValue != null) {
			analyze(temporaryValue);
		} else {
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
		rawValue = newValue;
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
		String string = getCurrentRawValue();
		for (Placeholder pl : placeholders) {
			string = pl.set(string, (ITabPlayer) owner);
		}
		string = Placeholders.color(string);
		for (String removed : Configs.removeStrings) {
			if (string.contains(removed)) string = string.replace(removed, "");
		}
		if (lastReplacedValue == null || !lastReplacedValue.equals(string)) {
			lastReplacedValue = string;
			return true;
		}
		return false;
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
		for (RelationalPlaceholder pl : relPlaceholders) {
			format = format.replace(pl.identifier, pl.getLastValue((ITabPlayer) viewer, (ITabPlayer) owner));
		}
		return format;
	}
}