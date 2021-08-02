package me.neznamy.tab.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;

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
	
	/**
	 * Returns map of used placeholders in features where key is placeholder identifier and value is a set of features
	 * using this placeholder somewhere in properties
	 * @return placeholder usage map
	 */
	public Map<String, Set<TabFeature>> getPlaceholderUsage();
	
	/**
	 * Finds placeholder output replacement
	 * @param replacements - map of replacements from premiumconfig
	 * @param originalOutput - original output of the placeholder
	 * @return replaced placeholder output
	 */
	public String findReplacement(Map<Object, String> replacements, String output);
}
