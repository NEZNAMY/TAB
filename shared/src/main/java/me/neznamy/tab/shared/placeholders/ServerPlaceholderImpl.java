package me.neznamy.tab.shared.placeholders;

import java.util.function.Supplier;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;

/**
 * A server placeholder (output same for all players)
 */
public class ServerPlaceholderImpl extends TabPlaceholder implements ServerPlaceholder {

	private final Supplier<Object> supplier;
	
	//last known value of the placeholder
	private String lastValue;

	/**
	 * Constructs new instance with given parameters
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval
	 */
	public ServerPlaceholderImpl(String identifier, int refresh, Supplier<Object> supplier) {
		super(identifier, refresh);
		this.supplier = supplier;
	}
	
	/**
	 * Updates placeholder and returns true if value changed, false if not
	 * @return true if value changed, false if not
	 */
	public boolean update() {
		String obj = String.valueOf(request());
		String newValue = obj == null ? identifier : setPlaceholders(obj, null);
		
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
	@Override
	public Object request() {
		try {
			return supplier.get();
		} catch (Throwable t) {
			TAB.getInstance().getErrorManager().placeholderError("Server placeholder " + identifier + " generated an error", t);
			return "ERROR";
		}
	}

	@Override
	public void updateValue(Object value) {
		if (value.equals(lastValue)) return;
		lastValue = String.valueOf(value);
		for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
			for (TabFeature f : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier)) {
				long time = System.nanoTime();
				f.refresh(player, false);
				TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
			}
		}
	}
}