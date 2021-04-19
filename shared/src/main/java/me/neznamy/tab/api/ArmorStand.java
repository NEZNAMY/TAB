package me.neznamy.tab.api;

import java.util.Set;

import me.neznamy.tab.shared.Property;

public interface ArmorStand {

	/**
	 * Returns true if offset is static, false if not
	 * @return true if static, false if dynamic
	 */
	public boolean hasStaticOffset();
	
	/**
	 * Changes offset of the armor stand
	 * @param offset - new offset
	 */
	public void setOffset(double offset);
	
	/**
	 * Returns offset of the armor stand
	 * @return offset from base
	 */
	public double getOffset();
	
	/**
	 * Returns property for armor stand's name
	 * @return property for armor stand's name
	 */
	public Property getProperty();
	
	/**
	 * Teleports armor stand to owner's current location for everyone in range
	 */
	public void teleport();
	
	/**
	 * Teleports armor stand to owner's current location for specified player
	 * @param viewer - player to send packet to
	 */
	public void teleport(TabPlayer viewer);
	
	/**
	 * Changes sneaking flag and sends packets to everyone in range
	 * @param sneaking
	 */
	public void sneak(boolean sneaking);
	
	/**
	 * Despawns armor stand for everyone
	 */
	public void destroy();
	
	/**
	 * Despawns armor stand for specified player
	 * @param viewer - player to send packet to
	 */
	public void destroy(TabPlayer viewer);
	
	/**
	 * Updates armor stand's name if needed
	 */
	public void refresh();
	
	/**
	 * Updates visibility if needed
	 * @param force - if refresh should be forced
	 */
	public void updateVisibility(boolean force);
	
	/**
	 * Removes specified player from list of players in range
	 * @param viewer - player to remove
	 */
	public void removeFromRegistered(TabPlayer viewer);
	
	/**
	 * Returns entity ID of this armor stand
	 * @return entity ID of this armor stand
	 */
	public int getEntityId();
	
	/**
	 * Spawns armor stand for specified player
	 * @param viewer - player to spawn for
	 * @param addToRegistered - if player should be added to players in range
	 */
	public void spawn(TabPlayer viewer);
	
	/**
	 * Returns list of players in entity tracking range (48 blocks)
	 * @return list of nearby players
	 */
	public Set<TabPlayer> getNearbyPlayers();
}
