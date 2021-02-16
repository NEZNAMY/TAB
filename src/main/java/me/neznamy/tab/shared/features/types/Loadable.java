package me.neznamy.tab.shared.features.types;

/**
 * Classes implementing this interface require load/unload to be called
 */
public interface Loadable extends Feature {

	public void load();
	public void unload();
}
