package me.neznamy.tab.api.bossbar;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;

public interface BossBarManager {

	/**
	 * Creates bossbar with specified parameters and registers it to bossbar manager with specified name
	 * @param name - internal name of bossbar
	 * @param title - title
	 * @param progress - progress (0-1)
	 * @param color - color
	 * @param style - style
	 * @return the bossbar
	 */
	public BossBar createBossBar(String name, String title, float progress, BarColor color, BarStyle style);

	/**
	 * Creates bossbar with specified parameters as strings to allow placeholder support and registers it to bossbar manager with specified name
	 * @param name - internal name of bossbar
	 * @param title - title
	 * @param progress - progress
	 * @param color - color
	 * @param style - style
	 * @return the bossbar
	 */
	public BossBar createBossBar(String name, String title, String progress, String color, String style);
	
	/**
	 * Returns registered bossbar with given name. Returns null if no such bossbar exists.
	 * @param name - name to return bossbar by
	 * @return bossbar with specified name
	 */
	public BossBar getBossBar(String name);
	
	/**
	 * Returns registered bossbar with given uuid. Returns null if no such bossbar exists.
	 * @param id - uuid to return bossbar by
	 * @return bossbar with specified uuid
	 */
	public BossBar getBossBar(UUID id);
	
	/**
	 * Returns a map of all registered bossbars. This includes bossbars from config and those registered via API.
	 * @return all registered bossbars
	 */
	public Map<String, BossBar> getRegisteredBossBars();
	
	/**
	 * Toggles bossbar for this player and sends toggle message if set to true
	 * @param player - player to toggle bossbar of
	 * @param sendToggleMessage - true if toggle message should be sent, false if not
	 */
	public void toggleBossBar(TabPlayer player, boolean sendToggleMessage);
	
	/**
	 * Return true if player can see bossbars, false if toggled
	 * @param player - player to check toggle status of
	 * @return true if visible, false if disabled (toggled)
	 */
	public boolean hasBossBarVisible(TabPlayer player);
	
	/**
	 * Sets bossbar visibility of player to set value. If value did not change, nothing happens.
	 * @param player - player to set bossbar visibility of
	 * @param visible - true if should be visible, false if not
	 * @param sendToggleMessage - true if toggle message should be sent if value changed, false if not
	 */
	public void setBossBarVisible(TabPlayer player, boolean visible, boolean sendToggleMessage);
	
	/**
	 * Temporarily displays registered bossbar to player for specified amount of time
	 * @param player - player to show bossbar to
	 * @param bossbar - name of registered bossbar
	 * @param duration - duration in milliseconds
	 */
	public void sendBossBarTemporarily(TabPlayer player, String bossbar, int duration);
	
	/**
	 * Temporarily displays registered bossbar to all players for specified amount of time
	 * @param bossbar - name of registered bossbar
	 * @param duration - duration in milliseconds
	 */
	public void announceBossBar(String bossbar, int duration);
	
	/**
	 * Returns set of currently active bossbar announcements
	 * @return set of currently active bossbar announcements
	 */
	public Set<BossBar> getAnnouncedBossBars();
}