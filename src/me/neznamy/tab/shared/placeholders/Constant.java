package me.neznamy.tab.shared.placeholders;

public abstract class Constant {

	protected String identifier;

	public Constant(String identifier) {
		this.identifier = identifier;
	}
	public String getIdentifier() {
		return identifier;
	}
	public abstract String get();
}
