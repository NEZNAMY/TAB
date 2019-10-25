package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;

public abstract class Placeholder {

	protected String identifier;
	
	public Placeholder(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}
	public String[] getChilds(){
		return new String[0];
	}
	public abstract String set(String s, ITabPlayer p);
}
