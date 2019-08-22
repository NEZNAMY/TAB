package me.neznamy.tab.shared;

public abstract class Placeholder {

	public String identifier;
	
	public Placeholder(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}
	public abstract String set(String s, ITabPlayer p);
	
	public String[] getChilds(){
		return new String[0];
	}
}