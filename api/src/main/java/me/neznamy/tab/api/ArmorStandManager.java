package me.neznamy.tab.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class for easy management of armor stands of a player
 */
public class ArmorStandManager {

	//map of registered armor stands
	private final Map<String, ArmorStand> armorStands = new LinkedHashMap<>();
	
	//players in entity tracking range
	private final List<TabPlayer> nearbyPlayers = new ArrayList<>();
	
	//array to iterate over to avoid concurrent modification and slightly boost performance & memory
	private ArmorStand[] armorStandArray = new ArmorStand[0];
	
	private TabPlayer[] nearbyPlayerArray = new TabPlayer[0];
	
	/**
	 * Adds armor stand into list
	 * @param name - key name of the armor stand
	 * @param as - armor stand to add
	 */
	public void addArmorStand(String name, ArmorStand as) {
		armorStands.put(name, as);
		armorStandArray = armorStands.values().toArray(new ArmorStand[0]);
		for (TabPlayer p : nearbyPlayerArray) as.spawn(p);
	}
	
	/**
	 * Returns armor stand with given name or null if not present
	 * @param name - name of armor stand registered with
	 * @return armor stand with given name
	 */
	public ArmorStand getArmorStand(String name) {
		return armorStands.get(name);
	}

	/**
	 * Removes armor stand from list. It is not automatically destroyed for players
	 * @param name - name of armor stand to remove
	 */
	public void removeArmorStand(String name) {
		armorStands.get(name).destroy();
		armorStands.remove(name);
		armorStandArray = armorStands.values().toArray(new ArmorStand[0]);
	}

	/**
	 * Spawns all armor stands for specified viewer and adds them into nearby players
	 * @param viewer - player to spawn armor stands for
	 */
	public void spawn(TabPlayer viewer) {
		nearbyPlayers.add(viewer);
		nearbyPlayerArray = nearbyPlayers.toArray(new TabPlayer[0]);
		if (viewer.getVersion().getMinorVersion() < 8) return;
		for (ArmorStand a : armorStandArray) a.spawn(viewer);
	}

	/**
	 * Sets sneak value of armor stands to specified value
	 * @param sneaking - new sneaking status
	 */
	public void sneak(boolean sneaking) {
		for (ArmorStand a : armorStandArray) a.sneak(sneaking);
	}

	/**
	 * Teleports armor stands to player's current location for all nearby players
	 */
	public void teleport() {
		for (ArmorStand a : armorStandArray) a.teleport();
	}

	/**
	 * Teleports armor stands to player's current location for specified viewer
	 * @param viewer - player to teleport armor stands for
	 */
	public void teleport(TabPlayer viewer) {
		for (ArmorStand a : armorStandArray) a.teleport(viewer);
	}

	/**
	 * Refreshes name of all armor stands for all nearby players
	 */
	public void refresh() {
		for (ArmorStand a : armorStandArray) a.refresh();
	}

	/**
	 * Updates armor stand visibility for all armor stands
	 * @param force - true if packets should be sent despite seemingly not needed
	 */
	public void updateVisibility(boolean force) {
		for (ArmorStand a : armorStandArray) a.updateVisibility(force);
	}

	/**
	 * Removes specified player from list of nearby players
	 * @param viewer - player to remove
	 */
	public void unregisterPlayer(TabPlayer viewer) {
		if (nearbyPlayers.remove(viewer)) nearbyPlayerArray = nearbyPlayers.toArray(new TabPlayer[0]);
	}

	/**
	 * Sends destroy packet of all armor stands to everyone and clears nearby players list
	 */
	public void destroy() {
		for (ArmorStand a : armorStandArray) a.destroy();
		nearbyPlayers.clear();
		nearbyPlayerArray = new TabPlayer[0];
	}

	/**
	 * Sends destroy packet of all armor stands to specified player and removes them from nearby players list
	 * @param viewer - player to destroy armor stands for
	 */
	public void destroy(TabPlayer viewer) {
		for (ArmorStand a : armorStandArray) a.destroy(viewer);
		unregisterPlayer(viewer);
	}

	/**
	 * Returns true if manager contains armor stand with specified entity id, false if not
	 * @param entityId - entity id
	 * @return true if armor stand with specified entity id exists, false if not
	 */
	public boolean hasArmorStandWithID(int entityId) {
		for (ArmorStand a : armorStandArray) {
			if (a.getEntityId() == entityId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns array of all registered armor stands
	 * @return armor stand array
	 */
	public ArmorStand[] getArmorStands(){
		return armorStandArray;
	}

	/**
	 * Returns array of nearby players
	 * @return array of nearby players
	 */
	public TabPlayer[] getNearbyPlayers(){
		return nearbyPlayerArray;
	}
	
	public boolean isNearby(TabPlayer viewer) {
		return nearbyPlayers.contains(viewer);
	}
	
	public void respawn() {
		for (ArmorStand a : armorStandArray) {
			for (TabPlayer viewer : nearbyPlayerArray) {
				a.respawn(viewer);
			}
		}
	}
}