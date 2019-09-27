package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class Property {

	private ITabPlayer owner;
	private String rawValue;
	private String lastReplacedValue;
	private List<Placeholder> placeholders = new ArrayList<Placeholder>();
	private String temporaryValue;
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
		Static = false;
		placeholders = detectPlaceholders(value, owner);
		hasRelationalPlaceholders = value.contains("%rel_");
		if (placeholders.isEmpty()) {
			if (value.length() == 0) {
				lastReplacedValue = value;
				Static = true;
			} else if (!value.contains("%")) { //not killing placeholderapi
				for (String removed : Configs.removeStrings) {
					if (value.contains(removed)) {
						Static = false;
						return;
					}
				}
				lastReplacedValue = Placeholders.color(value);
				Static = true;
			}
		}
	}
	public void setTemporaryValue(String temporaryValue) {
		this.temporaryValue = temporaryValue;
		if (temporaryValue != null) {
			//assigning value
			analyze(temporaryValue);
		} else {
			//removing temporary value
			analyze(rawValue);
		}
	}
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = newValue;
		if (temporaryValue == null) {
			analyze(newValue);
			lastReplacedValue = Static ? Placeholders.color(newValue) : null;
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
		for (Placeholder pl : placeholders) {
			string = pl.set(string, owner);
		}
		if (Placeholders.placeholderAPI) string = Placeholders.setPlaceholderAPIPlaceholders(string, owner);
		for (String removed : Configs.removeStrings) {
			if (string.contains(removed)) string = string.replace(removed, "");
		}
		string = Placeholders.color(string);
		if (lastReplacedValue == null || !string.equals(lastReplacedValue) || (hasRelationalPlaceholders() && System.currentTimeMillis()-lastUpdate > 30000)) {
			lastReplacedValue = string;
			lastUpdate = System.currentTimeMillis();
			return true;
		} else {
			return false;
		}
	}
	public boolean hasRelationalPlaceholders() {
		return hasRelationalPlaceholders && Placeholders.placeholderAPI;
	}
	public static List<Placeholder> detectPlaceholders(String rawValue, ITabPlayer player) {
		if (!rawValue.contains("%") && !rawValue.contains("{")) return Lists.newArrayList();
		List<Placeholder> placeholdersTotal = new ArrayList<Placeholder>();
		for (int i=0; i<10; i++) { //detecting placeholder chains
			boolean changed = false;
			for (Placeholder placeholder : player == null ? Placeholders.serverPlaceholders : Placeholders.getAll()) {
				if (rawValue.contains(placeholder.getIdentifier())) {
					if (!placeholdersTotal.contains(placeholder)) placeholdersTotal.add(placeholder);
					changed = true;
					for (String child : placeholder.getChilds()) {
						for (Placeholder p : detectPlaceholders(child, player)) {
							if (!placeholdersTotal.contains(p)) placeholdersTotal.add(p);
							changed = true;
						}
					}
				}
			}
			if (!changed) break; //no more placeholders found
		}
		return placeholdersTotal;
	}
}