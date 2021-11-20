package me.neznamy.tab.api.placeholder;

public interface ServerPlaceholder extends Placeholder {

	public void updateValue(Object value);
	
	public Object request();
}