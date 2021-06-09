package me.neznamy.tab.api;

import java.util.Set;
import java.util.UUID;

import io.netty.channel.Channel;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

/**
 * An interface representing a player
 */
public interface TabPlayer {

	/**
	 * Changes the requested property of a player temporarily (until next restart, reload or /tab reload)
	 * @param type Type of property
	 * @param value The value to be used
	 */
	public void setValueTemporarily(EnumProperty type, String value);

	/**
	 * Changes the requested property of a player permanently (saved into config too)
	 * @param type Type of property
	 * @param value The value to be used
	 */
	public void setValuePermanently(EnumProperty type, String value);

	/**
	 * Returns temporary value of player's property or null if not set
	 * @param type Type of property
	 * @return Temporary value of player's property or null if not set
	 * @see hasTemporaryValue
	 * @see setValueTemporarily
	 */
	public String getTemporaryValue(EnumProperty type);

	/**
	 * Returns Whether player has temporary value or not
	 * @param type Type of property
	 * @return Whether player has temporary value or not
	 */
	public boolean hasTemporaryValue(EnumProperty type);

	/**
	 * Removes temporary value from player if set
	 * @param type Type of property
	 */
	public void removeTemporaryValue(EnumProperty type);

	/**
	 * Returns original value of property of player
	 * @param type Type of property
	 * @return Original value of property of player
	 */
	public String getOriginalValue(EnumProperty type);

	/**
	 * Makes player's nametag globally invisible
	 * @see showNametag 
	 * @see hasHiddenNametag
	 */
	public void hideNametag();

	/**
	 * Hides player's nametag for specified player until it's shown again
	 * @param viewer - player to hide nametag for
	 */
	public void hideNametag(UUID viewer);

	/**
	 * Makes player's nametag visible again
	 * @see hideNametag
	 * @see hasHiddenNametag
	 */
	public void showNametag();

	/**
	 * Shows player's nametag for specified viewer if it was hidden before
	 * @param viewer - player to show nametag back for
	 */
	public void showNametag(UUID viewer);


	/**
	 * Return whether player has hidden nametag or not
	 * @return Whether player has hidden nametag or not
	 * @see hideNametag
	 * @see showNametag
	 */
	public boolean hasHiddenNametag();

	/**
	 * Returns true if nametag is hidden for specified viewer, false if not
	 * @param viewer - player to check visibility status for
	 * @return true if hidden, false if not
	 */
	public boolean hasHiddenNametag(UUID viewer);


	/**
	 * Refreshes all visuals on the player
	 */
	public void forceRefresh();


	/**
	 * Displays a scoreboard created using TABAPI.createScoreboard method and disables 
	 * automatic scoreboard assigning until this is reverted using removeCustomScoreboard()
	 * @see removeCustomScoreboard
	 */
	public void showScoreboard(Scoreboard scoreboard);


	/**
	 * Displays a scoreboard defined in premiumconfig.yml and disabled automatic
	 * scoreboard assignment until this is reverted using removeCustomScoreboard()
	 * @see removeCustomScoreboard
	 */
	public void showScoreboard(String name);


	/**
	 * Removes forced scoreboard sent using one of the showScoreboard methods
	 * @see showScoreboard
	 */
	public void removeCustomScoreboard();


	/**
	 * Returns player's name
	 * @return Player's name
	 */
	public String getName();


	/**
	 * Returns player's UUID
	 * @return Player's UUID
	 */
	public UUID getUniqueId();

	/**
	 * Returns player's uuid used in tablist. This may only be different than real uuid if
	 * TAB is installed on velocity with some specific velocity setup
	 * @return player's uuid in tablist
	 */
	public UUID getTablistUUID();

	/**
	 * Returns player's protocol version
	 * @return player's protocol version
	 */
	public ProtocolVersion getVersion();

	/**
	 * Returns platform-specific entity
	 * @return an instance of bukkit/bungee/velocity player depending on platform
	 */
	public Object getPlayer();

	/**
	 * Returns player's current world name (server on bungeecord)
	 * @return name of world (server on bungeecord) where player is currently in
	 */
	public String getWorldName();

	/**
	 * Performs platform-specific API call to check for permission and returns the result
	 * @param permission - the permission to check for
	 * @return - true if has, false if not
	 */
	public boolean hasPermission(String permission);

	/**
	 * Sends the player a custom universal packet
	 * @param packet - packet to send
	 */
	public void sendCustomPacket(UniversalPacketPlayOut packet);

	/**
	 * Sends the player a custom universal packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - packet to send
	 * @param feature - feature to increment sent packet counter of 
	 */
	public void sendCustomPacket(UniversalPacketPlayOut packet, TabFeature feature);

	/**
	 * Sends the player a platform-specific packet
	 * @param packet - an instance of packet depending on platform
	 */
	public void sendPacket(Object packet);

	/**
	 * Sends the player a platform-specific packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - an instance of packet depending on platform
	 * @param feature - feature to increment sent packet counter of 
	 */
	public void sendPacket(Object packet, TabFeature feature);

	/**
	 * Returns player's property by name
	 * @param name - name of property
	 * @return the property or null if not found
	 */
	public Property getProperty(String name);

	/**
	 * Sends a message to the player
	 * @param message - message to be sent
	 * @param translateColors - whether colors should be translated or not
	 */
	public void sendMessage(String message, boolean translateColors);

	/**
	 * Sends specified component as a chat message
	 * @param message - message to send
	 */
	public void sendMessage(IChatBaseComponent message);

	/**
	 * Returns player's primary permission group
	 * @return player's group depending on configuration
	 */
	public String getGroup();

	/**
	 * Toggles armor stands preview mode for the player
	 * @throws IllegalStateException - if unlimited nametag mode is not enabled
	 */
	public void toggleNametagPreview();

	/**
	 * Returns whether player is previewing nametag or not
	 * @return true if previewing, false if not
	 */
	public boolean isPreviewingNametag();

	/**
	 * Returns whether player has tab.staff permission or not
	 * @return whether player is staff or not
	 */
	public boolean isStaff();

	/**
	 * Returns player's channel or null if server is 1.7 or older
	 * @return player's channel
	 */
	public Channel getChannel();

	/**
	 * Returns player's ping calculated by server
	 * @return player's ping
	 */
	public long getPing();

	/**
	 * Returns player's platform-specific skin data
	 * @return player's skin
	 */
	public Object getSkin();

	/**
	 * Returns true once the player is successfully loaded (onJoin method ran through all methods)
	 * @return true if player is fully loaded, false otherwise
	 */
	public boolean isLoaded();

	/**
	 * Returns true if player has bossbar visible, false if player hid bossbar(s) using bossbar
	 * toggle command
	 * @return true if visible, false if toggled
	 */
	public boolean hasBossbarVisible();

	/**
	 * Sets bossbar visibility for the player and performs all packet sending if new value is
	 * different than previous value (true->false, false->true)
	 * @param visible - whether bossbar should be visible or not
	 * @param sendToggleMessage - if toggle message should be sent or not
	 */
	public void setBossbarVisible(boolean visible, boolean sendToggleMessage);

	/**
	 * Returns list of all bossbars player can currently see
	 * @return list of all bossbars player can see
	 */
	public Set<BossBar> getActiveBossBars();

	/**
	 * Sets property with specified name to new value. If property did no exist before, it is
	 * created. If it existed, it is overridden
	 * @param identifier - property name
	 * @param rawValue - new raw value
	 */
	public void setProperty(String identifier, String rawValue);

	/**
	 * Loads property from config using standard property loading algorithm
	 * @param property - property name to load
	 */
	public void loadPropertyFromConfig(String property);

	/**
	 * Loads property from config using standard property loading algorithm. If the property is
	 * not set in config, sets it to ifNotSet value
	 * @param property - property name to load
	 * @param ifNotSet - value to use if property is not defined in config
	 */
	public void loadPropertyFromConfig(String property, String ifNotSet);

	/**
	 * Returns name of player's scoreboard team or null if nametag feature is disabled
	 * @return name of player's team
	 */
	public String getTeamName();

	/**
	 * Returns user-friendly explanation of team name
	 * @return explanation behind team name
	 */
	public String getTeamNameNote();

	/**
	 * Forces collision rule for the player. Setting it to null will remove forced value
	 * @param collision - forced collision rule
	 */
	public void setCollisionRule(Boolean collision);

	/**
	 * Returns forced collision rule or null if collision is not forced using setCollisionRule
	 * @return forced value or null if not forced
	 */
	public Boolean getCollisionRule();

	/**
	 * Returns true if player is disguised using iDisguide or LibsDisguises on bukkit, LibsDisguises 
	 * only on bungeecord via bukkit bridge
	 * @return true if player is disguised, false if not
	 */
	public boolean isDisguised();

	/**
	 * Sets scoreboard visibility and sends packets if value changed. Sends toggle message configured
	 * in premiumconfig if value changed and sendToggleMessage is true
	 * @param visible - new visibility value
	 * @param sendToggleMessage - whether toggle message should be sent or not
	 */
	public void setScoreboardVisible(boolean visible, boolean sendToggleMessage);

	/**
	 * Toggles scoreboard visibility and sends toggle message if sendToggleMessage is true
	 * @param sendToggleMessage - whether toggle message should be sent or not
	 */
	public void toggleScoreboard(boolean sendToggleMessage);

	/**
	 * Returns true if player can see scoreboard, false if toggled
	 * @return true if visible, false if not
	 */
	public boolean isScoreboardVisible();

	/**
	 * Returns true if player has invisiblity potion, false if not. For bukkit, API is used, for bungeecord
	 * bukkit bridge is used
	 * @return true if has invisiblity potion, false if not
	 */
	public boolean hasInvisibilityPotion();

	/**
	 * Returns true if scoreboard is forced using API method, false if not
	 * @return true if forced via API, false if not
	 */
	public boolean hasForcedScoreboard();

	/**
	 * Currently only PV hook on bungeecord for global playerlist function. Looking to expand this
	 * in the future
	 * @return true if vanished with PV on bungeecord
	 */
	public boolean isVanished();

	/**
	 * Shows specificed bossbar to the player
	 * @param bossbar - bossbar to show
	 */
	public void showBossBar(BossBar bossbar);

	/**
	 * Hides specified bossbar if previously shown using showBossBar method
	 * @param bossbar - bossbar to hide back
	 */
	public void removeBossBar(BossBar bossbar);

	/**
	 * Internal method for bukkit bridge data collection (disguise status, invisiblity status)
	 * @param attribute - attribute name
	 * @param value - attribute value
	 */
	public void setAttribute(String attribute, String value);

	/**
	 * Returns true if player is online according to server
	 * @return true if online, false if not
	 */
	public boolean isOnline();
	
	/**
	 * Returns player's armor stand manager if unlimited nametag mode is enabled, null if disabled
	 * @return player's armor stand manager
	 */
	public ArmorStandManager getArmorStandManager();

	/**
	 * Sets player's armor stands manager to new instance
	 * @param armorStandManager - new instance
	 */
	public void setArmorStandManager(ArmorStandManager armorStandManager);

	/**
	 * Internal method that returns player's active scoreboard
	 * @return player's active scoreboard
	 */
	public Scoreboard getActiveScoreboard();
	
	/**
	 * Forces new team name for the player until this method is called again with null argument and 
	 * performs all actions to change player's team name
	 * @param name - forced team name
	 */
	public void forceTeamName(String name);
	
	/**
	 * Returns forced team name or null if not forced
	 * @return forced team name or null if not forced
	 */
	public String getForcedTeamName();
	
	/**
	 * Returns gamemode of the player (0 for survival, 1 creative, 2 adventure, 3 spectator)
	 * @return gamemode of the player
	 */
	public int getGamemode();
	
	/**
	 * Unregisters player's team and no longer handles it, as well as disables anti-override for teams.
	 * This can be resumed using resumeTeamHandling(). If team handling was paused already, nothing happens.
	 */
	public void pauseTeamHandling();
	
	/**
	 * Resumes team handling if it was before paused using pauseTeamHandling(), if not, nothing happens
	 */
	public void resumeTeamHandling();
	
	/**
	 * Returns true if team handling is paused for this player using pauseTeamHandling(), false if not or 
	 * it was resumed already using resumeTeamHandling
	 * @return true if paused, false if not
	 */
	public boolean hasTeamHandlingPaused();
}