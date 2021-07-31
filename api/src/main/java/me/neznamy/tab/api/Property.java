package me.neznamy.tab.api;

public interface Property {

	/**
	 * Changes raw value to new one
	 * @param newValue - new value to be used
	 */
	public void changeRawValue(String rawValue);

	/**
	 * Returns original raw value ignoring API calls
	 * @return original raw value
	 */
	public String getOriginalRawValue();

	/**
	 * Temporarily overrides current raw value with an API call
	 * @param temporaryValue - temporary value to be assigned
	 */
	public void setTemporaryValue(String temporaryValue);
	
	/**
	 * Returns current temporary value
	 * @return temporary value or null if not set
	 */
	public String getTemporaryValue();

	/**
	 * Returns temporary value (via API) if present, raw value otherwise
	 * @return current raw value
	 */
	public String getCurrentRawValue();

	/**
	 * Returns last known value
	 * @return last known value
	 */
	public String get();

	/**
	 * Replaces all placeholders in current raw value, colorizes it, removes remove-strings and returns whether value changed or not
	 * @return if updating changed value or not
	 */
	public boolean update();
	
	/**
	 * Replaces all placeholders in current raw value, colorizes it, removes remove-strings and returns it.
	 * Equal to calling update() and then get().
	 * @return updated value
	 */
	public String updateAndGet();

	/**
	 * Returns value for defined viewer by applying relational placeholders to last known value
	 * @param viewer - the viewer
	 * @return format for the viewer
	 */
	public String getFormat(TabPlayer viewer);
	
	/**
	 * Adds listener to this property's refreshing which will then receive .refresh() if a placeholder changes value
	 * @param listener
	 */
	public void addListener(TabFeature listener);
}