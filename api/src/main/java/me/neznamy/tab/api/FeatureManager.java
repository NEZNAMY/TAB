package me.neznamy.tab.api;

public interface FeatureManager {

	/**
	 * Registers a feature, which will start recieiving events
	 * 
	 * @param featureName - name of feature
	 * @param featureHandler - the handler
	 */
	public void registerFeature(String name, TabFeature feature);
	
	/**
	 * Unregisters feature making it no longer receive events. This does not run unload method nor cancel
	 * tasks created by the feature
	 * @param name - feature name defined in registerFeature
	 */
	public void unregisterFeature(String name);
	
	/**
	 * Returns whether a feature with said name is registered or not
	 * 
	 * @param name - name of feature defined in registerFeature method
	 * @return true if feature exists, false if not
	 */
	public boolean isFeatureEnabled(String name);

	/**
	 * Returns feature handler by it's name
	 * 
	 * @param name - name of feature defined in registerFeature method
	 * @return the feature or null if feature does not exist
	 */
	public TabFeature getFeature(String name);
}