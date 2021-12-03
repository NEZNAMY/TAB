package me.neznamy.tab.api;

public interface ArmorStand {

	/**
	 * Returns true if offset is static, false if not
	 * @return true if static, false if dynamic
	 */
	boolean hasStaticOffset();
	
	/**
	 * Changes offset of the armor stand
	 * @param offset - new offset
	 */
	void setOffset(double offset);

	/**
	 * Returns offset of the armor stand
	 * @return offset from base
	 */
	double getOffset();
	
	/**
	 * Returns property for armor stand's name
	 * @return property for armor stand's name
	 */
	Property getProperty();
	
	/**
	 * Teleports armor stand to owner's current location for everyone in range
	 */
	void teleport();
	
	/**
	 * Teleports armor stand to owner's current location for specified player
	 * @param viewer - player to send packet to
	 */
	void teleport(TabPlayer viewer);
	
	/**
	 * Changes sneaking flag and sends packets to everyone in range
	 * @param	sneaking
	 * 			new sneaking status
	 */
	void sneak(boolean sneaking);
	
	/**
	 * DeSpawns armor stand for everyone
	 */
	void destroy();
	
	/**
	 * DeSpawns armor stand for specified player
	 * @param viewer - player to send packet to
	 */
	void destroy(TabPlayer viewer);
	
	/**
	 * Updates armor stand's name if needed
	 */
	void refresh();
	
	/**
	 * Updates visibility if needed
	 * @param force - if refresh should be forced
	 */
	void updateVisibility(boolean force);
	
	/**
	 * Returns entity ID of this armor stand
	 * @return entity ID of this armor stand
	 */
	int getEntityId();
	
	/**
	 * Spawns armor stand for specified player
	 * @param viewer - player to spawn for
	 */
	void spawn(TabPlayer viewer);
	
	void respawn(TabPlayer viewer);
}
