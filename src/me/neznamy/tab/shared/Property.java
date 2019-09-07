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
	private String ifEmpty;
	private boolean hasRelationalPlaceholders;
	private long lastUpdate;
	private boolean Static;

	public Property(ITabPlayer owner, String rawValue) {
		this(owner, rawValue, null);
	}
	public Property(ITabPlayer owner, String rawValue, String ifEmpty) {
		this.owner = owner;
		this.rawValue = rawValue;
		this.ifEmpty = ifEmpty;
		if (rawValue.length() == 0 && ifEmpty != null) this.rawValue = ifEmpty;
		analyze(rawValue);
	}
	private void analyze(String value) {
		placeholders = detectPlaceholders(value);
		hasRelationalPlaceholders = value.contains("%rel_");
		if (value.length() == 0) {
			lastReplacedValue = "";
			Static = true;
		} else if (!value.contains("%") && !value.contains("&")){
			for (String removed : Configs.removeStrings) {
				if (value.contains(removed)) {
					Static = false;
					return;
				}
			}
			lastReplacedValue = rawValue;
			Static = true;
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
	public void removeTemporaryValue() {
		setTemporaryValue(null);
	}
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = newValue;
		if (temporaryValue == null) {
			analyze(newValue);
			lastReplacedValue = Static ? newValue : null;
		}
	}
	public String get() {
		if (lastReplacedValue == null) isUpdateNeeded();
		String Return = lastReplacedValue;
		if (Return.length() == 0 && ifEmpty != null) Return = ifEmpty;
		return Return;
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
		string = Placeholders.color(string);
		for (String removed : Configs.removeStrings) {
			if (string.contains(removed)) string = string.replace(removed, "");
		}
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
	public boolean isStatic() {
		return Static;
	}
	public static List<Placeholder> detectPlaceholders(String rawValue) {
		if (!rawValue.contains("%") && !rawValue.contains("{")) return Lists.newArrayList();
		List<Placeholder> placeholdersTotal = new ArrayList<Placeholder>();
		boolean changed;
		for (int i=0; i<10; i++) { //detecting placeholder chains
			changed = false;
			for (Placeholder placeholder : Placeholders.list) {
				if (rawValue.contains(placeholder.getIdentifier())) {
					if (!placeholdersTotal.contains(placeholder)) placeholdersTotal.add(placeholder);
					changed = true;
					for (String child : placeholder.getChilds()) {
						List<Placeholder> placeholders = detectPlaceholders(child);
						for (Placeholder p : placeholders) {
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