package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.api.TabPlayer;

/**
 * A server placeholder (output same for all players)
 */
public abstract class ServerPlaceholder extends Placeholder{

	//last known value of the placeholder
	private String lastValue;

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval
	 */
	protected ServerPlaceholder(String identifier, int refresh) {
		super(identifier, refresh);
	}
	
	/**
	 * Updates placeholder and returns true if value changed, false if not
	 * @return true if value changed, false if not
	 */
	public boolean update() {
		String newValue = get();
		if (newValue == null) newValue = "";
		
		//make invalid placeholders return identifier instead of nothing
		if (newValue.equals(identifier) && lastValue == null) {
			lastValue = identifier;
		}
		if (!newValue.equals("ERROR") && !newValue.equals(identifier) && (lastValue == null || !lastValue.equals(newValue))) {
			lastValue = newValue;
			return true;
		}
		return false;
	}

	@Override
	public String getLastValue(TabPlayer p) {
		if (lastValue == null) update();
		return lastValue;
	}

	/**
	 * Abstract method to be overridden by specific placeholders, returns new value of the placeholder
	 * @return new value
	 */
	public abstract String get();
}