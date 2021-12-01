package me.neznamy.tab.api;

public interface FeatureManager {

	/**
	 * Registers a feature, which will start receiving events
	 * 
	 * @param name - name of feature
	 * @param feature - the handler
	 */
	void registerFeature(String name, TabFeature feature);
	
	/**
	 * Unregisters feature making it no longer receive events. This does not run unload method nor cancel
	 * tasks created by the feature
	 * @param name - feature name defined in registerFeature
	 */
	void unregisterFeature(String name);
	
	/**
	 * Returns whether a feature with said name is registered or not
	 * 
	 * @param name - name of feature defined in registerFeature method
	 * @return true if feature exists, false if not
	 */
	boolean isFeatureEnabled(String name);

	/**
	 * Returns feature handler by its name
	 * 
	 * @param name - name of feature defined in registerFeature method
	 * @return the feature or null if feature does not exist
	 */
	TabFeature getFeature(String name);
}