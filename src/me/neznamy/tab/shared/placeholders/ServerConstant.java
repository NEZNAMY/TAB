package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;

public abstract class ServerConstant extends Placeholder{

	private String value;
	
	public ServerConstant(String identifier) {
		super(identifier, 0);
		value = get();
	}
	public String getIdentifier() {
		return identifier;
	}
	public abstract String get();
	
	@Override
	public String getValue(ITabPlayer p) {
		return value;
	}
}