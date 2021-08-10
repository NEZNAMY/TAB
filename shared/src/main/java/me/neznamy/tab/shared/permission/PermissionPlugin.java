package me.neznamy.tab.shared.permission;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface representing permission plugin hook
 */
public interface PermissionPlugin {

	/**
	 * Returns primary permission group of player
	 * @param p - player to get group of
	 * @return player's primary permission group
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public String getPrimaryGroup(TabPlayer p) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException;
	
	/**
	 * Returns list of all groups players is in
	 * @param p - player to check groups of
	 * @return list of all groups of player
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public String[] getAllGroups(TabPlayer p) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException;
	
	/**
	 * Returns version of the permission plugin
	 * @return version of the permission plugin
	 */
	public String getVersion();
	
	/**
	 * Returns name of the permission plugin
	 * @return name of the permission plugin
	 */
	public default String getName() {
		return getClass().getSimpleName();
	}
}