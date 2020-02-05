package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class Placeholder {

	protected int cooldown;
	protected String identifier;
	
	public Placeholder(String identifier, int cooldown) {
		this.identifier = identifier;
		this.cooldown = cooldown;
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
			return s.replace(identifier, value);
		} catch (Throwable t) {
			return Shared.error(s, "An error occurred when setting placeholder " + identifier + (p == null ? "" : " for " + p.getName()), t);
		}
	}
	public abstract String getValue(ITabPlayer p);
}