package me.neznamy.tab.api.placeholder;

public interface Placeholder {

	public int getRefresh();

	public String getIdentifier();

	public boolean isTriggerMode();

	public void enableTriggerMode();

	public void enableTriggerMode(Runnable onActivation, Runnable onDisable);

	public void unload();
}