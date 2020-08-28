package me.neznamy.tab.api;

import java.util.UUID;

/**
 * An interface representing a player
 */
public interface TabPlayer {

	/**
	 * Changes the requested property of a player temporarily (until next restart, reload or /tab reload)
	 * @param type Type of property
	 * @param value The value to be used
	 * @since 2.8.3
	 */
	public void setValueTemporarily(EnumProperty type, String value);
	
	
	/**
	 * Changes the requested property of a player permanently (saved into config too)
	 * @param type Type of property
	 * @param value The value to be used
	 * @since 2.8.3
	 */
	public void setValuePermanently(EnumProperty type, String value);
	
	
	/**
	 * Returns temporary value of player's property or null if not set
	 * @param type Type of property
	 * @return Temporary value of player's property or null if not set
	 * @see hasTemporaryValue
	 * @see setValueTemporarily
	 * @since 2.8.3
	 */
	public String getTemporaryValue(EnumProperty type);
	
	
	/**
	 * Returns Whether player has temporary value or not
	 * @param type Type of property
	 * @return Whether player has temporary value or not
	 * @since 2.8.3
	 */
	public boolean hasTemporaryValue(EnumProperty type);
	
	
	/**
	 * Removes temporary value from player if set
	 * @param type Type of property
	 * @since 2.8.3
	 */
	public void removeTemporaryValue(EnumProperty type);
	
	
	/**
	 * Returns original value of property of player
	 * @param type Type of property
	 * @return Original value of property of player
	 * @since 2.8.3
	 */
	public String getOriginalValue(EnumProperty type);
	
	
	/**
	 * Sends requested header and footer to player
	 * @param header - Header
	 * @param footer - Footer
	 * @since 2.8.3
	 */
	public void sendHeaderFooter(String header, String footer);
	
	
	/**
	 * Makes player's nametag invisible until server restart/reload or /plugman reload tab
	 * @see showNametag 
	 * @see hasHiddenNametag
	 * @since 2.8.3
	 */
	public void hideNametag();
	
	
	/**
	 * Makes player's nametag visible again
	 * @see hideNametag
	 * @see hasHiddenNametag
	 * @since 2.8.3
	 */
	public void showNametag();
	
	
	/**
	 * Return whether player has hidden nametag or not
	 * @return Whether player has hidden nametag or not
	 * @since 2.8.3
	 * @see hideNametag
	 * @see showNametag
	 */
	public boolean hasHiddenNametag();
	
	
	/**
	 * Refreshes all visuals on the player
	 * @since 2.8.3
	 */
	public void forceRefresh();
	
	
	/**
	 * Displays a scoreboard created using TABAPI.createScoreboard method
	 * @see removeCustomScoreboard
	 * @since 2.8.3
	 */
	public void showScoreboard(Scoreboard scoreboard);
	
	
	/**
	 * Displays a scoreboard defined in premiumconfig.yml
	 * @see removeCustomScoreboard
	 * @since 2.8.3
	 */
	public void showScoreboard(String name);
	
	
	/**
	 * Removes forced scoreboard sent using one of the showScoreboard methods
	 * @see showScoreboard
	 * @since 2.8.3
	 */
	public void removeCustomScoreboard();
	
	
	/**
	 * Returns player's name
	 * @return Player's name
	 * @since 2.8.3
	 */
	public String getName();
	
	
	/**
	 * Returns player's UUID
	 * @return Player's UUID
	 * @since 2.8.3
	 */
	public UUID getUniqueId();
}
