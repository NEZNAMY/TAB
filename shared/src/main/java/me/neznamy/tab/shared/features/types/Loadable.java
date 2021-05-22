package me.neznamy.tab.shared.features.types;

/**
 * Classes implementing this interface require load/unload to be called
 */
public interface Loadable extends Feature {

	/**
	 * Loads all players and sends packets
	 */
	public void load();
	
	/**
	 * Unloads all players and sends clear packets
	 */
	public void unload();
	
	/**
	 * Default override to the method in Feature interface to not need to define it in 
	 * classes implementing only this interface as load/unload methods alone do not use this
	 */
	public default Object getFeatureType() {
		return null;
	}
}