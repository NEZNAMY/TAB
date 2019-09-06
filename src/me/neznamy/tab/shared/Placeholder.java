package me.neznamy.tab.shared;

public abstract class Placeholder {

	public String identifier;
	
	public Placeholder(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}
	public String set(String s, ITabPlayer p) {
		return s.replace(identifier, get(p));
	}
	public abstract String get(ITabPlayer p);
	
	public String[] getChilds(){
		return new String[0];
	}
}