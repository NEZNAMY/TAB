package me.neznamy.tab.api.placeholder;

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
		Object obj = get();
		String newValue = obj == null ? identifier : String.valueOf(setPlaceholders(obj, null));
		
		//make invalid placeholders return identifier instead of nothing
		if (identifier.equals(newValue) && lastValue == null) {
			lastValue = identifier;
		}
		if (!"ERROR".equals(newValue) && !identifier.equals(newValue) && (lastValue == null || !lastValue.equals(newValue))) {
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
	public abstract Object get();
}