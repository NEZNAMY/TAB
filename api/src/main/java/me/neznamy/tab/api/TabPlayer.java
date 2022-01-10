package me.neznamy.tab.api;

import java.util.UUID;

import io.netty.channel.Channel;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.TabPacket;

/**
 * An interface representing a player
 */
public interface TabPlayer {

	/**
	 * Refreshes all visuals on the player
	 */
	void forceRefresh();

	/**
	 * Returns player's name
	 * @return Player's name
	 */
	String getName();

	/**
	 * Returns player's UUID
	 * @return Player's UUID
	 */
	UUID getUniqueId();

	/**
	 * Returns player's uuid used in TabList. This may only be different from real uuid if
	 * TAB is installed on velocity with some specific velocity setup
	 * @return player's uuid in TabList
	 */
	UUID getTablistUUID();

	/**
	 * Returns player's protocol version
	 * @return player's protocol version
	 */
	ProtocolVersion getVersion();

	/**
	 * Returns platform-specific entity
	 * @return an instance of bukkit/bungee player depending on platform
	 */
	Object getPlayer();

	/**
	 * Returns player's current world name (on BungeeCord this requires bridge installed)
	 * @return name of world where player is currently in, null on BungeeCord if bridge is not installed
	 */
	String getWorld();
	
	/**
	 * Returns player's current server name on BungeeCord (on bukkit this returns null)
	 * @return name of server where player is currently in, null on bukkit
	 */
	String getServer();

	/**
	 * Performs platform-specific API call to check for permission and returns the result
	 * @param permission - the permission to check for
	 * @return - true if player has permission, false if not
	 */
	boolean hasPermission(String permission);

	/**
	 * Sends the player a custom universal packet
	 * @param packet - packet to send
	 */
	void sendCustomPacket(TabPacket packet);

	/**
	 * Sends the player a custom universal packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - packet to send
	 * @param feature - feature to increment sent packet counter of 
	 */
	void sendCustomPacket(TabPacket packet, TabFeature feature);
	
	/**
	 * Sends the player a custom universal packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - packet to send
	 * @param feature - feature to increment sent packet counter of 
	 */
	void sendCustomPacket(TabPacket packet, String feature);

	/**
	 * Sends the player a platform-specific packet
	 * @param packet - an instance of packet depending on platform
	 */
	void sendPacket(Object packet);

	/**
	 * Sends the player a platform-specific packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - an instance of packet depending on platform
	 * @param feature - feature to increment sent packet counter of 
	 */
	void sendPacket(Object packet, TabFeature feature);

	/**
	 * Sends the player a platform-specific packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - an instance of packet depending on platform
	 * @param feature - feature to increment sent packet counter of
	 */
	void sendPacket(Object packet, String feature);

	/**
	 * Returns player's property by name
	 * @param name - name of property
	 * @return the property or null if not found
	 */
	Property getProperty(String name);

	/**
	 * Sends a message to the player
	 * @param message - message to be sent
	 * @param translateColors - whether colors should be translated or not
	 */
	void sendMessage(String message, boolean translateColors);

	/**
	 * Sends specified component as a chat message
	 * @param message - message to send
	 */
	void sendMessage(IChatBaseComponent message);

	/**
	 * Returns player's primary permission group
	 * @return player's group depending on configuration
	 */
	String getGroup();

	/**
	 * Toggles armor stands preview mode for the player
	 */
	void toggleNametagPreview();

	/**
	 * Returns whether player is previewing NameTag or not
	 * @return true if previewing, false if not
	 */
	boolean isPreviewingNametag();

	/**
	 * Returns player's channel or null if server is 1.7 or older
	 * @return player's channel
	 */
	Channel getChannel();

	/**
	 * Returns player's ping calculated by server
	 * @return player's ping
	 */
	int getPing();

	/**
	 * Returns player's platform-specific skin data
	 * @return player's skin
	 */
	Object getSkin();

	/**
	 * Returns true once the player is successfully loaded (onJoin method ran through all methods)
	 * @return true if player is fully loaded, false otherwise
	 */
	boolean isLoaded();

	/**
	 * Sets property with specified name to new value. If property did not exist before, it is
	 * created and true is returned. If it existed, it is overridden and true is returned. False is returned otherwise.
	 * @param feature - feature using this property to get placeholders registered
	 * @param identifier - property name
	 * @param rawValue - new raw value
	 * @return true if value changed / did not exist, false if value did not change
	 */
	boolean setProperty(TabFeature feature, String identifier, String rawValue);

	/**
	 * Loads property from config using standard property loading algorithm
	 * @param property - property name to load
	 * @return true if value did not exist or changed, false otherwise
	 */
	boolean loadPropertyFromConfig(TabFeature feature, String property);

	/**
	 * Loads property from config using standard property loading algorithm. If the property is
	 * not set in config, sets it to ifNotSet value
	 * @param property - property name to load
	 * @param ifNotSet - value to use if property is not defined in config
	 * @return true if value did not exist or changed, false otherwise
	 */
	boolean loadPropertyFromConfig(TabFeature feature, String property, String ifNotSet);

	/**
	 * Returns name of player's scoreboard team or null if NameTag feature is disabled
	 * @return name of player's team
	 */
	String getTeamName();

	/**
	 * Returns user-friendly explanation of team name
	 * @return explanation behind team name
	 */
	String getTeamNameNote();

	/**
	 * Returns true if player is disguised using LibsDisguises
	 * @return true if player is disguised, false if not
	 */
	boolean isDisguised();

	/**
	 * Returns true if player has invisibility potion, false if not. For bukkit, API is used, for BungeeCord
	 * bukkit bridge is used
	 * @return true if player has invisibility potion, false if not
	 */
	boolean hasInvisibilityPotion();

	/**
	 * Currently only PV hook on BungeeCord for global PlayerList function. Looking to expand this
	 * in the future
	 * @return true if vanished with PV on BungeeCord
	 */
	boolean isVanished();

	/**
	 * Returns true if player is online according to server
	 * @return true if online, false if not
	 */
	boolean isOnline();

	/**
	 * Returns player's armor stand manager if unlimited GameMode mode is enabled, null if disabled
	 * @return player's armor stand manager
	 */
	ArmorStandManager getArmorStandManager();

	/**
	 * Sets player's armor stands manager to new instance
	 * @param armorStandManager - new instance
	 */
	void setArmorStandManager(ArmorStandManager armorStandManager);
	
	/**
	 * Returns GameMode of the player (0 for survival, 1 creative, 2 adventure, 3 spectator)
	 * @return GameMode of the player
	 */
	int getGamemode();
	
	boolean isBedrockPlayer();

	void setTemporaryGroup(String group);

	boolean hasTemporaryGroup();

	void resetTemporaryGroup();
}
