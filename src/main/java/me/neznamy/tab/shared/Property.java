package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;

public class Property {

	private ITabPlayer owner;
	private String rawValue;
	private String temporaryValue;
	public String lastReplacedValue;
	private String source;

	public List<Placeholder> placeholders = new ArrayList<Placeholder>();
	public List<RelationalPlaceholder> relPlaceholders = new ArrayList<RelationalPlaceholder>();

	public Property(ITabPlayer owner, String rawValue, String source) {
		if (rawValue == null) rawValue = "";
		this.owner = owner;
		this.source = source;
		this.rawValue = analyze(rawValue);
		update();
	}
	private String analyze(String value) {
		for (Placeholder c : Placeholders.getAllPlaceholders()) {
			if (c instanceof ServerConstant && value.contains(c.getIdentifier())) {
				value = value.replace(c.getIdentifier(), ((ServerConstant)c).get());
			}
		}
		placeholders = Placeholders.detectPlaceholders(value);
		relPlaceholders = Placeholders.detectRelationalPlaceholders(value);
		return value;
	}
	public void setTemporaryValue(String temporaryValue) {
		this.temporaryValue = temporaryValue;
		if (temporaryValue != null) {
			temporaryValue = analyze(temporaryValue);
		} else {
			rawValue = analyze(rawValue);
		}
		update();
	}
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = newValue;
		if (temporaryValue == null) {
			rawValue = analyze(rawValue);
			update();
		}
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
	public String getSource() {
		return temporaryValue == null ? source : "API";
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String updateAndGet() {
		update();
		return get();
	}
	public boolean update() {
		String string = getCurrentRawValue();
		for (Placeholder pl : placeholders) {
			string = pl.set(string, owner);
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
	public String get() {
		return lastReplacedValue;
	}
	public String getFormat(ITabPlayer viewer) {
		if (viewer == null) return lastReplacedValue;
		String format = lastReplacedValue;
		for (RelationalPlaceholder pl : relPlaceholders) {
			format = format.replace(pl.identifier, pl.get(viewer, owner));
		}
		return format;
	}
}