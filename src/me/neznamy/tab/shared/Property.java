package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

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
		if (rawValue.length() == 0 && ifEmpty != null) this.rawValue = ifEmpty;
		placeholders = Placeholders.detect(rawValue);
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setTemporaryValue(String temporaryValue) {
		this.temporaryValue = temporaryValue;
		placeholders = Placeholders.detect(temporaryValue);
	}
	public void removeTemporaryValue() {
		setTemporaryValue(null);
	}
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = newValue;
		if (temporaryValue == null) placeholders = Placeholders.detect(rawValue);
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