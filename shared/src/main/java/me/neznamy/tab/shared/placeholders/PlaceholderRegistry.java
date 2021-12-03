package me.neznamy.tab.shared.placeholders;

import me.neznamy.tab.api.placeholder.PlaceholderManager;

/**
 * An interface to be implemented by classes which offer internal placeholders that can be registered into the system
 */
public interface PlaceholderRegistry {

	/**
	 * Registers all placeholders into placeholder manager
	 * @param manager - placeholder manager to register placeholders to
	 */
	void registerPlaceholders(PlaceholderManager manager);
}