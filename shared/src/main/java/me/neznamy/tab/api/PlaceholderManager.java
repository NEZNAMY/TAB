package me.neznamy.tab.api;

import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

public interface PlaceholderManager {

	/**
	 * Registers a player placeholder (placeholder with player-specific output)
	 * @param placeholder - Placeholder handler
	 * @see registerServerPlaceholder
	 * @see registerServerConstant
	 */
	public void registerPlayerPlaceholder(PlayerPlaceholder placeholder);

	/**
	 * Registers a server placeholder (placeholder with same output for all players)
	 * @param placeholder - Placeholder handler
	 * @see registerPlayerPlaceholder
	 * @see registerServerConstant
	 */
	public void registerServerPlaceholder(ServerPlaceholder placeholder);

	/**
	 * Registers a relational placeholder (different output for each player pair)
	 * @param placeholder - Placeholder handler
	 */
	public void registerRelationalPlaceholder(RelationalPlaceholder placeholder);
}
