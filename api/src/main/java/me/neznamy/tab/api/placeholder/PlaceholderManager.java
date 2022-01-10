package me.neznamy.tab.api.placeholder;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;

public interface PlaceholderManager {

	/**
	 * Registers a server placeholder (placeholder with same output for all players)
	 */
	ServerPlaceholder registerServerPlaceholder(String identifier, int refresh, Supplier<Object> supplier);
	
	/**
	 * Registers a player placeholder (placeholder with player-specific output)
	 */
	PlayerPlaceholder registerPlayerPlaceholder(String identifier, int refresh, Function<TabPlayer, Object> function);

	/**
	 * Registers a relational placeholder (different output for each player pair)
	 */
	RelationalPlaceholder registerRelationalPlaceholder(String identifier, int refresh, BiFunction<TabPlayer, TabPlayer, Object> function);
	
	/**
	 * Detects placeholders in text using %% pattern and returns list of all detected identifiers
	 * @param text - text to detect placeholders in
	 * @return list of detected identifiers
	 */
	List<String> detectPlaceholders(String text);

	/**
	 * Adds placeholder to list of used placeholders and assigns this feature as using it,
	 * which will then receive refresh() if values changes
	 * @param identifier - placeholder identifier
	 * @param feature - feature using the placeholder
	 */
	void addUsedPlaceholder(String identifier, TabFeature feature);
	
	String findReplacement(String placeholder, String output);

	List<String> getUsedPlaceholders();
}
