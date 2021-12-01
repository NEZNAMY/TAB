package me.neznamy.tab.api.placeholder;

public interface ServerPlaceholder extends Placeholder {

	void updateValue(Object value);
	
	Object request();
}