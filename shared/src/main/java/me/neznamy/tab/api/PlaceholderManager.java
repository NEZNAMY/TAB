package me.neznamy.tab.api;

import java.util.List;

import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

public interface PlaceholderManager {

	/**
	 * Registers a player placeholder (placeholder with player-specific output)
	 * @param placeholder - Placeholder handler
	 */
	public void registerPlayerPlaceholder(PlayerPlaceholder placeholder);

	/**
	 * Registers a server placeholder (placeholder with same output for all players)
	 * @param placeholder - Placeholder handler
	 */
	public void registerServerPlaceholder(ServerPlaceholder placeholder);

	/**
	 * Registers a relational placeholder (different output for each player pair)
	 * @param placeholder - Placeholder handler
	 */
	public void registerRelationalPlaceholder(RelationalPlaceholder placeholder);
	
	/**
	 * Detects placeholders in text using %% pattern and returns list of all detected identifiers
	 * @param text - text to detect placeholders in
	 * @return list of detected identifiers
	 */
	public List<String> detectPlaceholders(String text);

	/**
	 * Returns placeholder from identifier. This is either internal or PAPI placeholder. If it's not registered, 
	 * this methods registers it as PAPI placeholder and returns it
	 * @param identifier - identifier to get placeholder by
	 * @return placeholder handler from identifier
	 */
	public Placeholder getPlaceholder(String identifier);
}
