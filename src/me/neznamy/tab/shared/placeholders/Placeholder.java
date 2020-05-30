package me.neznamy.tab.shared.placeholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class Placeholder {

	protected int cooldown;
	protected String identifier;
	private Map<String, Object> replacements = new HashMap<String, Object>();
	private List<String> outputPlaceholders = new ArrayList<String>();
	
	@SuppressWarnings("unchecked")
	public Placeholder(String identifier, int cooldown) {
		this.identifier = identifier;
		this.cooldown = cooldown;
		if (Premium.is()) {
			replacements = Premium.premiumconfig.getConfigurationSection("placeholder-output-replacements." + identifier);
			Map<String, Object> colored = new HashMap<>();
			for (Entry<String, Object> entry : replacements.entrySet()) {
				colored.put(entry.getKey().replace('&', Placeholders.colorChar), entry.getValue());
				for (String id : Placeholders.detectAll(entry.getValue()+"")) {
					if (!outputPlaceholders.contains(id)) outputPlaceholders.add(id);
				}
			}
			replacements = colored;
		}
	}
	public String getIdentifier() {
		return identifier;
	}
	public String[] getChilds(){
		return new String[0];
	}
	public String set(String s, ITabPlayer p) {
		try {
			String value = getValue(p);
			if (value == null) value = "";
			value = Placeholders.color(value);
			String newValue = setPlaceholders(findReplacement(value, p), p);
			if (newValue.contains("%value%")) {
				newValue = newValue.replace("%value%", value);
			}
			return s.replace(identifier, newValue);
		} catch (Throwable t) {
			return Shared.errorManager.printError(s, "An error occurred when setting placeholder " + identifier + (p == null ? "" : " for " + p.getName()), t);
		}
	}
	public String findReplacement(String originalOutput, ITabPlayer p) {
		if (replacements.isEmpty()) return originalOutput;
		if (replacements.containsKey(originalOutput)) {
			return replacements.get(originalOutput).toString();
		}
		for (Entry<String, Object> entry : replacements.entrySet()) {
			String key = entry.getKey();
			if (key.contains("-")) {
				try {
					float low = Float.parseFloat(key.split("-")[0]);
					float high = Float.parseFloat(key.split("-")[1]);
					float actualValue = Float.parseFloat(originalOutput);
					if (low <= actualValue && actualValue <= high) return entry.getValue().toString();
				} catch (NumberFormatException e) {
					//nope
				}
			}
		}
		if (replacements.containsKey("else")) return replacements.get("else").toString();
		return originalOutput;
	}
	private String setPlaceholders(String text, ITabPlayer p) {
		for (String s : outputPlaceholders) {
			if (s.equals("%value%")) continue;
			Placeholder pl = Placeholders.getUsedPlaceholder(s);
			if (text.contains(pl.getIdentifier())) text = pl.set(text, p);
		}
		return text;
	}
	protected abstract String getValue(ITabPlayer p);
}