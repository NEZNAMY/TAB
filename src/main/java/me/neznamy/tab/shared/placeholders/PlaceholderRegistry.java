package me.neznamy.tab.shared.placeholders;

import java.util.List;

/**
 * An interface to be implemented by classes which offer internal placeholders that can be registered into the system
 */
public interface PlaceholderRegistry {

	/**
	 * Returns list of all placeholders that can be registered
	 * @return list of all placeholders that can be registered
	 */
	public List<Placeholder> registerPlaceholders();
}