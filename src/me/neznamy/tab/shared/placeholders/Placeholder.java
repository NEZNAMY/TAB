package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class Placeholder {

	protected int cooldown;
	protected String identifier;
	protected String cpuDisplay;
	
	public Placeholder(String identifier, int cooldown, String cpuDisplay) {
		this.identifier = identifier;
		this.cooldown = cooldown;
		this.cpuDisplay = cpuDisplay;
	}
	public String getIdentifier() {
		return identifier;
	}
	public String[] getChilds(){
		return new String[0];
	}
	public String set(String s, ITabPlayer p) {
		try {
			return s.replace(identifier, getValue(p));
		} catch (Throwable t) {
			return Shared.error(s, "An error occured when setting placeholder \"" + identifier + "\"" + p == null ? "" : (" for " + p.getName()), t);
		}
	}
	public abstract String getValue(ITabPlayer p);
}
