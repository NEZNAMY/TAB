package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class Property {

	private ITabPlayer owner;
	private String rawValue;
	private String temporaryValue;
	private String lastReplacedValue;

	private List<Placeholder> placeholders = new ArrayList<Placeholder>();
	private boolean placeholderapiPlaceholdersPresent;
	private boolean hasRelationalPlaceholders;
	private long lastUpdate;
	private boolean Static;

	public Property(ITabPlayer owner, String rawValue) {
		if (rawValue == null) rawValue = "";
		this.owner = owner;
		this.rawValue = rawValue;
		analyze(rawValue);
	}
	private void analyze(String value) {
		placeholders = detectPlaceholders(value, owner != null);
		hasRelationalPlaceholders = value.contains("%rel_");
		placeholderapiPlaceholdersPresent = value.indexOf("%") != value.lastIndexOf("%"); //two or more %
		if (placeholders.isEmpty() && !placeholderapiPlaceholdersPresent && !hasRelationalPlaceholders) {
			//no placeholders, this is a static string
			//performing final changes before saving it
			for (String removed : Configs.removeStrings) {
				if (value.contains(removed)) {
					value = value.replace(removed, "");
				}
			}
			lastReplacedValue = Placeholders.color(value);
			Static = true;
		} else {
			lastReplacedValue = null;
			Static = false;
		}
	}
	public void setTemporaryValue(String temporaryValue) {
		this.temporaryValue = temporaryValue;
		if (temporaryValue != null) {
			analyze(temporaryValue);
		} else {
			analyze(rawValue);
		}
	}
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = newValue;
		if (temporaryValue == null) {
			analyze(newValue);
		}
	}
	public String get() {
		if (lastReplacedValue == null) isUpdateNeeded();
		return lastReplacedValue;
	}
	public String getCurrentRawValue() {
		return temporaryValue != null ? temporaryValue : rawValue;
	}
	public String getTemporaryValue() {
		return temporaryValue;
	}
	public String getOriginalRawValue() {
		return rawValue;
	}
	public boolean isUpdateNeeded() {
		if (Static) return false;
		String string = getCurrentRawValue();

		//own placeholders
		for (Placeholder pl : placeholders) {
			string = pl.set(string, owner);
		}
		//placeholderapi
		if (placeholderapiPlaceholdersPresent && PluginHooks.placeholderAPI) {
			string = PluginHooks.PlaceholderAPI_setPlaceholders(owner, string);
		}

		//removing strings
		for (String removed : Configs.removeStrings) {
			if (string.contains(removed)) string = string.replace(removed, "");
		}

		//colors
		string = Placeholders.color(string);

		if (lastReplacedValue == null || !string.equals(lastReplacedValue) || (hasRelationalPlaceholders() && System.currentTimeMillis()-lastUpdate > Configs.SECRET_relational_placeholders_refresh *1000)) {
			lastReplacedValue = string;
			lastUpdate = System.currentTimeMillis();
			return true;
		} else {
			return false;
		}
	}
	public boolean hasRelationalPlaceholders() {
		return hasRelationalPlaceholders && PluginHooks.placeholderAPI;
	}
	public boolean isStatic() {
		return Static;
	}
	public static List<Placeholder> detectPlaceholders(String rawValue, boolean playerPlaceholders) {
		if (!rawValue.contains("%") && !rawValue.contains("{")) return new ArrayList<Placeholder>();
		List<Placeholder> placeholdersTotal = new ArrayList<Placeholder>();
		for (Placeholder placeholder : playerPlaceholders ? Placeholders.getAll() : Placeholders.serverPlaceholders) {
			if (rawValue.contains(placeholder.getIdentifier())) {
				if (!placeholdersTotal.contains(placeholder)) placeholdersTotal.add(placeholder);
				for (String child : placeholder.getChilds()) {
					for (Placeholder p : detectPlaceholders(child, playerPlaceholders)) {
						if (!placeholdersTotal.contains(p)) placeholdersTotal.add(p);
					}
				}
			}
		}
		return placeholdersTotal;
	}
}