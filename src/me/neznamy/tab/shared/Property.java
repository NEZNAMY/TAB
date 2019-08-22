package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class Property {

	private ITabPlayer owner;
	private String identifier;
	private String rawValue;
	private String lastReplacedValue;
	private List<Placeholder> placeholders = new ArrayList<Placeholder>();
	private String temporaryValue;
	private String ifEmpty;

	public Property(ITabPlayer owner, String identifier, String rawValue) {
		this(owner, identifier, rawValue, null);
	}
	public Property(ITabPlayer owner, String identifier, String rawValue, String ifEmpty) {
		this.owner = owner;
		this.identifier = identifier;
		this.rawValue = rawValue;
		this.ifEmpty = ifEmpty;
		placeholders = detectPlaceholders(rawValue);
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setTemporaryValue(String temporaryValue) {
		this.temporaryValue = temporaryValue;
		placeholders = detectPlaceholders(temporaryValue);
	}
	public void removeTemporaryValue() {
		setTemporaryValue(null);
	}
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = newValue;
		if (temporaryValue == null) placeholders = detectPlaceholders(rawValue);
	}
	public List<Placeholder> detectPlaceholders(String rawValue) {
		if (!rawValue.contains("%") && !rawValue.contains("{")) return Lists.newArrayList();
		List<Placeholder> placeholdersTotal = new ArrayList<Placeholder>();
		String testString = rawValue;
		boolean changed;
		for (int i=0; i<10; i++) { //detecting placeholder chains
			changed = false;
			for (Placeholder pl : Placeholders.list) {
				if (testString.contains(pl.getIdentifier())) {
					testString = pl.set(testString, owner);
					if (!placeholdersTotal.contains(pl)) placeholdersTotal.add(pl);
					changed = true;
					for (String child : pl.getChilds()) {
						List<Placeholder> placeholders = detectPlaceholders(child);
						for (Placeholder p : placeholders) {
							if (!placeholdersTotal.contains(p)) placeholdersTotal.add(p);
						}
					}
				}
			}
			if (!changed) break; //no more placeholders found
		}
		return placeholdersTotal;
	}
	public String get() {
		if (lastReplacedValue == null) isUpdateNeeded();
		String Return = lastReplacedValue;
		if (Return.length() == 0 && ifEmpty != null) Return = ifEmpty;
		return Return;
	}
	public String getRaw() {
		return temporaryValue != null ? temporaryValue : rawValue;
	}
	public String getTemporaryValue() {
		return temporaryValue;
	}
	public String getOriginalRawValue() {
		return rawValue;
	}
	public boolean isUpdateNeeded() {
		String string = getRaw();
		for (Placeholder pl : placeholders) {
			string = pl.set(string, owner);
		}
		string = Placeholders.setPlaceholderAPIPlaceholders(string, owner);
		string = Placeholders.color(string);
		for (String removed : Configs.removeStrings) {
			string = string.replace(removed, "");
		}
		if (lastReplacedValue != null && string.equals(lastReplacedValue) && !string.contains("%rel_")) {
			return false;
		} else {
			lastReplacedValue = string;
			return true;
		}
	}
}