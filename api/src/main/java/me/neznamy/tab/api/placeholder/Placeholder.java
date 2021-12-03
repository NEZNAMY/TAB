package me.neznamy.tab.api.placeholder;

public interface Placeholder {

	int getRefresh();

	String getIdentifier();

	boolean isTriggerMode();

	void enableTriggerMode();

	void enableTriggerMode(Runnable onActivation, Runnable onDisable);

	void unload();
}