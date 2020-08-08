package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.shared.ITabPlayer;

public abstract class ServerConstant extends Placeholder{
	
	private String value;
	
	public ServerConstant(String identifier) {
		super(identifier, 999999);
		value = get();
	}
	public String get(ITabPlayer p) {
		return value;
	}
	public String getLastValue(ITabPlayer p) {
		return value;
	}
	public abstract String get();
}