package me.neznamy.tab.shared.features.interfaces;

/**
 * Classes implementing this interface require load/unload to be called
 */
public interface Loadable {

	public void load();
	public void unload();
}
